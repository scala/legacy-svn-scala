/* 
 * Copyright (C) 2007 Google Inc.
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

package com.google.android.notepad

import com.google.provider.NotePad

import _root_.android.app.Activity
import _root_.android.app.Activity._
import _root_.android.content.{ComponentName, Context, Intent}
import _root_.android.database.Cursor
import _root_.android.graphics.{Canvas, Paint, Rect}
import _root_.android.net.ContentURI
import _root_.android.os.Bundle
import _root_.android.text.TextUtils
import _root_.android.util.{AttributeSet, Log, Config}
import _root_.android.view.{KeyEvent, Menu}
import _root_.android.widget.EditText

import java.util.Map

/**
 * A generic activity for editing a note in a database.  This can be used
 * either to simply view a note (Intent.VIEW_ACTION), view and edit a note
 * (Intent.EDIT_ACTION), or create a new note (Intent.INSERT_ACTION).  
 */
object NoteEditor {
  private val TAG = "Notes"

  private val NOTE_INDEX = 1
  private val TITLE_INDEX = 2
  private val MODIFIED_INDEX = 3

  /**
   * Standard projection for the interesting columns of a normal note.
   */
  private val PROJECTION = Array(
    NotePad.Notes._ID, // 0
    NotePad.Notes.NOTE, // 1
    NotePad.Notes.TITLE, // 2
    NotePad.Notes.MODIFIED_DATE // 3
  )
    
  // This is our state data that is stored when freezing.
  private val ORIGINAL_CONTENT = "origContent"

  // Identifiers for our menu items.
  private val REVERT_ID = Menu.FIRST
  private val DISCARD_ID = Menu.FIRST + 1
  private val DELETE_ID = Menu.FIRST + 2

  // The different distinct states the activity can be run in.
  private val STATE_UNDEF = -1
  private val STATE_EDIT = 0
  private val STATE_INSERT = 1

  // we need this constructor for ViewInflate
  class MyEditText(context: Context, attrs: AttributeSet, params: Map)
      extends EditText(context, attrs, params) {
    private val mRect = new Rect()
    private val mPaint = new Paint()
    mPaint setStyle Paint.Style.STROKE
    mPaint setColor 0xFF0000FF
        
    override protected def onDraw(canvas: Canvas) {
      val count = getLineCount()
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

class NoteEditor extends Activity {
  import NoteEditor._ // companion object

  private var mState = STATE_UNDEF
  private var mNoteOnly = false
  private var mURI: ContentURI = _
  private var mCursor: Cursor = null
  private var mText: EditText = null
  private var mOriginalContent: String = ""

  override protected def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    val intent = getIntent()
    //val type = intent.resolveType(this)

    // Do some setup based on the action being performed.

    val action = intent.getAction()
    if (action equals Intent.EDIT_ACTION) {
      // Requested to edit: set that state, and the data being edited.
      mState = STATE_EDIT
      mURI = intent.getData()

    } else if (action equals Intent.INSERT_ACTION) {
      // Requested to insert: set that state, and create a new entry
      // in the container.
      mState = STATE_INSERT
      mURI = getContentResolver().insert(intent.getData(), null)

      // If we were unable to create a new note, then just finish
      // this activity.  A RESULT_CANCELED will be sent back to the
      // original activity if they requested a result.
      if (mURI == null) {
        Log.e("Notes", "Failed to insert new note into " +
              getIntent().getData())
        finish()
        return
      }

      // The new entry was created, so assume all will end well and
      // set the result to be returned.
      setResult(RESULT_OK, mURI.toString())

    } else {
      // Whoops, unknown action!  Bail.
      Log.e(TAG, "Unknown action, exiting")
      finish()
      return
    }

    // Set the layout for this activity.  You can find it
    // in res/layout/hello_activity.xml
    setContentView(R.layout.note_editor)
        
    // The text view for our note, identified by its ID in the XML file.
    mText = findViewById(R.id.note).asInstanceOf[EditText]

    // Get the note!
    mCursor = managedQuery(mURI, PROJECTION, null, null)

    // If an instance of this activity had previously stopped, we can
    // get the original text it started with.
    if (icicle != null)
      mOriginalContent = icicle getString ORIGINAL_CONTENT
  }

  override protected def onResume() {
    super.onResume()

    // If we didn't have any trouble retrieving the data, it is now
    // time to get at the stuff.
    if (mCursor != null) {
      // Make sure we are at the one and only row in the cursor.
      mCursor.first()

      // Modify our overall title depending on the mode we are running in.
      if (mState == STATE_EDIT)
        setTitle(getText(R.string.title_edit))
      else if (mState == STATE_INSERT)
        setTitle(getText(R.string.title_create))

      // This is a little nasty: we be resumed after previously being
      // paused/stopped.  We want to re-retrieve the data to make sure
      // we are still accurately showing what is in the cursor...  but
      // we don't want to lose any UI state like the current cursor
      // position.  This trick accomplishes that.  In the future we
      // should have a better API for doing this...
      val curState = mText.saveState()
      val note = mCursor getString NOTE_INDEX
      mText setText note
      mText restoreState curState

      // If we hadn't previously retrieved the original text, do so
      // now.  This allows the user to revert their changes.
      if (mOriginalContent == null)
        mOriginalContent = note
    } else {
      setTitle(getText(R.string.error_title))
      mText setText getText(R.string.error_message)
    }
  }

  override protected def onFreeze(outState: Bundle) {
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
      val text = mText.getText().toString()
      val length = text.length()

      // If this activity is finished, and there is no text, then we
      // do something a little special: simply delete the note entry.
      // Note that we do this both for editing and inserting...  it
      // would be reasonable to only do it when inserting.
      if (isFinishing() && (length == 0) && !mNoteOnly) {
        setResult(RESULT_CANCELED)
        deleteNote()

        // Get out updates into the provider.
      } else {
        // This stuff is only done when working with a full-fledged note.
        if (!mNoteOnly) {
          // Bump the modification time to now.
          mCursor.updateLong(MODIFIED_INDEX, System.currentTimeMillis())

          // If we are creating a new note, then we want to also create
          // an initial title for it.
          if (mState == STATE_INSERT) {
            var title = text.substring(0, Math.min(30, length))
            if (length > 30) {
              val lastSpace = title lastIndexOf ' '
              if (lastSpace > 0)
                title = title.substring(0, lastSpace)
            }
            mCursor.updateString(TITLE_INDEX, title)
          }
        }

        // Write our text back into the provider.
        mCursor.updateString(NOTE_INDEX, text)

        // Commit all of our changes to persistent storage.  Note the
        // use of managedCommitUpdates() instead of
        // mCursor.commitUpdates() -- this lets Activity take care of
        // requerying the new data if needed.
        managedCommitUpdates(mCursor)
      }
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    // Build the menus that are shown when editing.
    if (mState == STATE_EDIT) {
      menu.add(0, REVERT_ID, R.string.menu_revert).setShortcut(
               KeyEvent.KEYCODE_0, 0, KeyEvent.KEYCODE_R)
      if (!mNoteOnly)
        menu.add(0, DELETE_ID, R.string.menu_delete).setShortcut(
                 KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_D)

      // Build the menus that are shown when inserting.
    } else
      menu.add(0, DISCARD_ID, R.string.menu_discard).setShortcut(
               KeyEvent.KEYCODE_0, 0, KeyEvent.KEYCODE_D)

    // If we are working on a real honest-to-ghod note, then append to the
    // menu items for any other activities that can do stuff with it
    // as well.  This does a query on the system for any activities that
    // implement the ALTERNATIVE_ACTION for our data, adding a menu item
    // for each one that is found.
    if (!mNoteOnly) {
      val intent = new Intent(null, getIntent().getData())
      intent addCategory Intent.ALTERNATIVE_CATEGORY
      menu.addIntentOptions(
        Menu.ALTERNATIVE, 0,
        new ComponentName(this, classOf[NoteEditor]), null,
        intent, 0, null)
    }

    true
  }

  override def onOptionsItemSelected(item: Menu.Item): Boolean = {
    // Handle all of the possible menu actions.
    item.getId() match {
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

  /**
   * Take care of cancelling work on a note.  Deletes the note if we
   * had created it, otherwise reverts to the original text.
   */
  private final def cancelNote() {
    if (mCursor != null) {
      if (mState == STATE_EDIT) {
        mCursor.updateString(NOTE_INDEX, mOriginalContent)
        mCursor.commitUpdates()
        mCursor.deactivate()
        mCursor = null
      } else if (mState == STATE_INSERT)
        deleteNote()
    }
    setResult(RESULT_CANCELED)
    finish()
  }

  /**
   * Take care of deleting a note.  Simply deletes the entry.
   */
  private final def deleteNote() {
    if (mCursor != null) {
      mText setText ""
      mCursor.deleteRow()
      mCursor.deactivate()
      mCursor = null
    }
  }
}
