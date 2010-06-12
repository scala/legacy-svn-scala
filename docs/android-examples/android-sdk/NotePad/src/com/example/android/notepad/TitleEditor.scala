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
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.{Button, EditText}

object TitleEditor {

  /** This is a special intent action that means "edit the title of a note". */
  final val EDIT_TITLE_ACTION = "com.android.notepad.action.EDIT_TITLE"

  /** An array of the columns we are interested in. */
  private final val PROJECTION = Array(Notes._ID, Notes.TITLE)

  /** Index of the title column */
  private final val COLUMN_INDEX_TITLE = 1
}

/** An activity that will edit the title of a note. Displays a floating
 *  window with a text field.
 */
class TitleEditor extends Activity with View.OnClickListener {
  import TitleEditor._ // companion object

  /** Cursor which will provide access to the note whose title we are editing.
   */
  private var mCursor: Cursor = _

  /** The EditText field from our UI. Keep track of this so we can extract the
   *  text when we are finished.
   */
  private var mText: EditText = _

  /** The content URI to the note that's being edited.
   */
  private var mUri: Uri = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.title_editor)

    // Get the uri of the note whose title we want to edit
    mUri = getIntent().getData()

    // Get a cursor to access the note
    mCursor = managedQuery(mUri, PROJECTION, null, null, null)

    // Set up click handlers for the text field and button
    mText = this.findViewById(R.id.title).asInstanceOf[EditText]
    mText setOnClickListener this
        
    val b = findViewById(R.id.ok)
    b setOnClickListener this
  }

  override protected def onResume() {
    super.onResume()

    // Initialize the text with the title column from the cursor
    if (mCursor != null) {
      mCursor.moveToFirst()
      mText setText (mCursor getString COLUMN_INDEX_TITLE)
    }
  }

  override protected def onPause() {
    super.onPause()

    if (mCursor != null) {
      // Write the title back to the note 
      val values = new ContentValues
      values.put(Notes.TITLE, mText.getText.toString)
      getContentResolver.update(mUri, values, null, null)
    }
  }

  def onClick(v: View) {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    finish()
  }
}
