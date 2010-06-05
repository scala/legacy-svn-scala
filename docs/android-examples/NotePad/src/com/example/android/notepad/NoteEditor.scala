/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad

import com.example.android.notepad.NotePad.Notes

import android.app.Activity
import android.app.Activity._
import android.content.{ComponentName, ContentValues, Context, Intent}
import android.database.Cursor
import android.graphics.{Canvas, Paint, Rect}
import android.net.Uri
import android.os.Bundle
import android.util.{AttributeSet, Log}
import android.view.{Menu, MenuItem}
import android.widget.EditText

object NoteEditor {

  private final val TAG = "Notes"

  /** Standard projection for the interesting columns of a normal note. */
  private final val PROJECTION = Array(Notes._ID, Notes.NOTE)

  /** The index of the note column */
  private final val COLUMN_INDEX_NOTE = 1

  // This is our state data that is stored when freezing.
  private final val ORIGINAL_CONTENT = "origContent"

  // Identifiers for our menu items.
  private final val REVERT_ID = Menu.FIRST
  private final val DISCARD_ID = Menu.FIRST + 1
  private final val DELETE_ID = Menu.FIRST + 2

  // The different distinct states the activity can be run in.
  private final val STATE_EDIT = 0
  private final val STATE_INSERT = 1

  /** A custom <code>EditText</code> that draws lines between each line
   *  of text that is displayed.
   */
  class LinedEditText(context: Context, attrs: AttributeSet)
  extends EditText(context, attrs) {
    private var mRect = new Rect
    private var mPaint = new Paint
    mPaint setStyle Paint.Style.STROKE
    mPaint setColor 0x800000FF
        
    override protected def onDraw(canvas: Canvas) {
      val count = getLineCount
      val r = mRect
      val paint = mPaint

      for (i <- 0 until count) {
        val baseline = getLineBounds(i, r)

        canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint)
      }

      super.onDraw(canvas)
    }
  }

}

/** A generic activity for editing a note in a database.  This can be used
 *  either to simply view a note {@link Intent#ACTION_VIEW}, view and edit a note
 *  {@link Intent#ACTION_EDIT}, or create a new note {@link Intent#ACTION_INSERT}.  
 */
class NoteEditor extends Activity {
  import NoteEditor._ // companion object

  private var mState: Int = _
  private var mNoteOnly = false
  private var mUri: Uri = _
  private var mCursor: Cursor = _
  private var mText: EditText = _
  private var mOriginalContent: String = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val intent = getIntent

    // Do some setup based on the action being performed.

    val action = intent.getAction
    if (Intent.ACTION_EDIT equals action) {
      // Requested to edit: set that state, and the data being edited.
      mState = STATE_EDIT
      mUri = intent.getData
    }
    else if (Intent.ACTION_INSERT equals action) {
      // Requested to insert: set that state, and create a new entry
      // in the container.
      mState = STATE_INSERT
      mUri = getContentResolver.insert(intent.getData, null)

      // If we were unable to create a new note, then just finish
      // this activity.  A RESULT_CANCELED will be sent back to the
      // original activity if they requested a result.
      if (mUri == null) {
        Log.e(TAG, "Failed to insert new note into " + intent.getData)
        finish()
        return
      }

      // The new entry was created, so assume all will end well and
      // set the result to be returned.
      setResult(RESULT_OK, new Intent setAction mUri.toString)
    }
    else {
      // Whoops, unknown action!  Bail.
      Log.e(TAG, "Unknown action, exiting")
      finish()
      return
    }

    // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
    setContentView(R.layout.note_editor)

    // The text view for our note, identified by its ID in the XML file.
    mText = findViewById(R.id.note).asInstanceOf[EditText]

    // Get the note!
    mCursor = managedQuery(mUri, PROJECTION, null, null, null)

    // If an instance of this activity had previously stopped, we can
    // get the original text it started with.
    if (savedInstanceState != null)
      mOriginalContent = savedInstanceState getString ORIGINAL_CONTENT
  }

  override protected def onResume() {
    super.onResume()

    // If we didn't have any trouble retrieving the data, it is now
    // time to get at the stuff.
    if (mCursor != null) {
      // Make sure we are at the one and only row in the cursor.
      mCursor.moveToFirst()

      // Modify our overall title depending on the mode we are running in.
      if (mState == STATE_EDIT)
        setTitle(getText(R.string.title_edit))
      else if (mState == STATE_INSERT)
        setTitle(getText(R.string.title_create))

      // This is a little tricky: we may be resumed after previously being
      // paused/stopped.  We want to put the new text in the text view,
      // but leave the user where they were (retain the cursor position
      // etc).  This version of setText does that for us.
      val note = mCursor getString COLUMN_INDEX_NOTE
      mText setTextKeepState note
            
      // If we hadn't previously retrieved the original text, do so
      // now.  This allows the user to revert their changes.
      if (mOriginalContent == null)
        mOriginalContent = note
    }
    else {
      setTitle(getText(R.string.error_title))
      mText setText getText(R.string.error_message)
    }
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    // Save away the original text, so we still have it if the activity
    // needs to be killed while paused.
    outState.putString(ORIGINAL_CONTENT, mOriginalContent)
  }

  override protected def onPause() {
    super.onPause()

    // The user is going somewhere else, so make sure their current
    // changes are safely saved away in the provider.  We don't need
    // to do this if only editing.
    if (mCursor != null) {
      val text = mText.getText.toString
      val length = text.length

      // If this activity is finished, and there is no text, then we
      // do something a little special: simply delete the note entry.
      // Note that we do this both for editing and inserting...  it
      // would be reasonable to only do it when inserting.
      if (isFinishing && (length == 0) && !mNoteOnly) {
        setResult(RESULT_CANCELED)
        deleteNote()

        // Get out updates into the provider.
      } else {
        val values = new ContentValues()

        // This stuff is only done when working with a full-fledged note.
        if (!mNoteOnly) {
          // Bump the modification time to now.
          values.put(Notes.MODIFIED_DATE, System.currentTimeMillis.toDouble)

          // If we are creating a new note, then we want to also create
          // an initial title for it.
          if (mState == STATE_INSERT) {
            var title = text.substring(0, math.min(30, length))
            if (length > 30) {
              val lastSpace = title lastIndexOf ' '
              if (lastSpace > 0) {
                title = title.substring(0, lastSpace)
              }
            }
            values.put(Notes.TITLE, title)
          }
        }

        // Write our text back into the provider.
        values.put(Notes.NOTE, text)

        // Commit all of our changes to persistent storage. When the update completes
        // the content provider will notify the cursor of the change, which will
        // cause the UI to be updated.
        getContentResolver.update(mUri, values, null, null)
      }
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    // Build the menus that are shown when editing.
    if (mState == STATE_EDIT) {
      menu.add(0, REVERT_ID, 0, R.string.menu_revert)
          .setShortcut('0', 'r')
          .setIcon(android.R.drawable.ic_menu_revert)
      if (!mNoteOnly) {
        menu.add(0, DELETE_ID, 0, R.string.menu_delete)
            .setShortcut('1', 'd')
            .setIcon(android.R.drawable.ic_menu_delete)
      }

      // Build the menus that are shown when inserting.
    } else {
      menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
          .setShortcut('0', 'd')
          .setIcon(android.R.drawable.ic_menu_delete)
    }

    // If we are working on a full note, then append to the
    // menu items for any other activities that can do stuff with it
    // as well.  This does a query on the system for any activities that
    // implement the ALTERNATIVE_ACTION for our data, adding a menu item
    // for each one that is found.
    if (!mNoteOnly) {
      val intent = new Intent(null, getIntent().getData())
      intent addCategory Intent.CATEGORY_ALTERNATIVE
      menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
        new ComponentName(this, classOf[NoteEditor]), null, intent, 0, null)
    }

    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    // Handle all of the possible menu actions.
    item.getItemId() match {
      case DELETE_ID =>
        deleteNote()
        finish()
      case DISCARD_ID =>
        cancelNote()
      case REVERT_ID =>
        cancelNote()
      case _ =>
    }
    super.onOptionsItemSelected(item)
  }

  /** Take care of canceling work on a note.  Deletes the note if we
   * had created it, otherwise reverts to the original text.
   */
  private final def cancelNote() {
    if (mCursor != null) {
      if (mState == STATE_EDIT) {
        // Put the original note text back into the database
        mCursor.close()
        mCursor = null
        val values = new ContentValues()
        values.put(Notes.NOTE, mOriginalContent)
        getContentResolver().update(mUri, values, null, null)
      }
      else if (mState == STATE_INSERT) {
        // We inserted an empty note, make sure to delete it
        deleteNote()
      }
    }
    setResult(RESULT_CANCELED)
    finish()
  }

  /** Take care of deleting a note.  Simply deletes the entry.
   */
  private final def deleteNote() {
    if (mCursor != null) {
      mCursor.close()
      mCursor = null
      getContentResolver.delete(mUri, null, null)
      mText setText ""
    }
  }
}
