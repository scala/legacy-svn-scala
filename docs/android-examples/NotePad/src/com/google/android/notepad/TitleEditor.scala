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
import _root_.android.database.Cursor
import _root_.android.net.ContentURI
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.widget.{Button, EditText}


/**
 * An activity that will edit the title of a note. Displays a floating
 * window with a text field.
 */
object TitleEditor {

  /**
   * This is a special intent action that means "edit the title of a note".
   */
  val EDIT_TITLE_ACTION =
    "com.google.android.notepad.action.EDIT_TITLE"

  /**
   * Index of the title column
   */
  private val TITLE_INDEX = 1

  /**
   * An array of the columns we are interested in.
   */
  private val PROJECTION = Array(
    NotePad.Notes._ID, // 0
    NotePad.Notes.TITLE, // 1
  )
}

class TitleEditor extends Activity with View.OnClickListener {
  import TitleEditor._ // companion object

  /**
   * Cursor which will provide access to the note whose title we are editing.
   */
  var mCursor: Cursor = _

  /**
   * The EditText field from our UI. Keep track of this so we can extract the
   * text when we are finished.
   */
  var mText: EditText = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    setContentView(R.layout.title_editor)

    // Get the uri of the note whose title we want to edit
    val uri = getIntent().getData()

    // Get a cursor to access the note
    mCursor = managedQuery(uri, PROJECTION, null, null)

    // Set up click handlers for the text field and button
    mText = this.findViewById(R.id.title).asInstanceOf[EditText]
    mText setOnClickListener this

    val b = /*(Button)*/ findViewById(R.id.ok)
    b setOnClickListener this
  }

  override protected def onResume() {
    super.onResume()

    // Initialize the text with the title column from the cursor
    if (mCursor != null) {
      mCursor.first()
      val title = mCursor getString TITLE_INDEX
      mText setText title
    }
  }

  override protected def onPause() {
    super.onPause()

    // Write the text back into the cursor 
    if (mCursor != null) {
      val title = mText.getText().toString()
       mCursor.updateString(TITLE_INDEX, title)
       mCursor.commitUpdates()
    }
  }

  def onClick(v: View) {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    finish()
  }
}
