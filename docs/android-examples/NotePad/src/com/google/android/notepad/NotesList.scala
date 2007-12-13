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

import _root_.android.app.Activity._
import _root_.android.app.ListActivity
import _root_.android.content.{ComponentName, Intent}
import _root_.android.database.Cursor
import _root_.android.graphics.drawable.Drawable
import _root_.android.net.ContentURI
import _root_.android.os.Bundle
import _root_.android.view.{KeyEvent, Menu, View}
import _root_.android.view.View.MeasureSpec
import _root_.android.widget.{ListAdapter, ListView, SimpleCursorAdapter, TextView}


/**
 * Displays a list of notes. Will display notes from the ContentUri
 * provided int the intent if there is one, otherwise uses the default list
 * from the 
 *
 */
object NotesList {
  // Menu item Ids
  val DELETE_ID = Menu.FIRST
  val INSERT_ID = Menu.FIRST + 1

  /**
   * The columns we are interested in from the database
   */
  private val PROJECTION = Array(
    NotePad.Notes._ID,
    NotePad.Notes.TITLE
  )
}

class NotesList extends ListActivity {
  import NotesList._ // companion object

  /**
   * Cursor which holds list of all notes
   */
  private var mCursor: Cursor = _

  override protected def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    setDefaultKeyMode(SHORTCUT_DEFAULT_KEYS)

    // If no data was given in the intent (because we were started
    // as a MAIN activity), then use our default content provider.
    val intent = getIntent()
    if (intent.getData() == null)
      intent setData NotePad.Notes.CONTENT_URI

    setupListStripes()

    mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null)
        
    // Used to map notes entries from the database to views
    val adapter = new SimpleCursorAdapter(this,
                _root_.android.R.layout.simple_list_item_1, mCursor,
                Array(NotePad.Notes.TITLE), Array(_root_.android.R.id.text1))
    setListAdapter(adapter)
  }

  /**
   * Add stripes to the list view.
   */
  private def setupListStripes() {
    // Get Drawables for alternating stripes
    val lineBackgrounds = new Array[Drawable](2)

    lineBackgrounds(0) = getResources().getDrawable(R.drawable.even_stripe)
    lineBackgrounds(1) = getResources().getDrawable(R.drawable.odd_stripe)

    // Make and measure a sample TextView of the sort our adapter will
    // return
    val view = getViewInflate().
      inflate(_root_.android.R.layout.simple_list_item_1, null, null)

    val v = view.findViewById(_root_.android.R.id.text1).asInstanceOf[TextView]
    v setText "X"
    // Make it 100 pixels wide, and let it choose its own height.
    v.measure(MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY, 100),
              MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, 0))
    val height = v.getMeasuredHeight()
    getListView().setStripes(lineBackgrounds, height)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    // This is our one standard application action -- inserting a
    // new note into the list.
    menu.add(0, INSERT_ID, R.string.menu_insert).setShortcut(
            KeyEvent.KEYCODE_3, 0, KeyEvent.KEYCODE_A)

    // Generate any additional actions that can be performed on the
    // overall list.  In a normal install, there are no additional
    // actions found here, but this allows other applications to extend
    // our menu with their own actions.
    val intent = new Intent(null, getIntent().getData())
    intent addCategory Intent.ALTERNATIVE_CATEGORY
    menu.addIntentOptions(
        Menu.ALTERNATIVE, 0, new ComponentName(this, classOf[NotesList]),
        null, intent, 0, null)

    true
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    super.onPrepareOptionsMenu(menu)
    val haveItems = mCursor.count() > 0

    // If there are any notes in the list (which implies that one of
    // them is selected), then we need to generate the actions that
    // can be performed on the current selection.  This will be a combination
    // of our own specific actions along with any extensions that can be
    // found.
    if (haveItems) {
      // This is the selected item.
      val uri = getIntent().getData() addId getSelectionRowID()

      // Build menu...  always starts with the EDIT action...
      val specifics = Array(new Intent(Intent.EDIT_ACTION, uri))
      val items = new Array[Menu.Item](1)

      // ... is followed by whatever other actions are available...
      val intent = new Intent(null, uri)
      intent addCategory Intent.SELECTED_ALTERNATIVE_CATEGORY
      menu.addIntentOptions(Menu.SELECTED_ALTERNATIVE, 0, null, specifics,
                            intent, Menu.NO_SEPARATOR_AFTER, items)

      // ... and ends with the delete command.
      menu.add(Menu.SELECTED_ALTERNATIVE, DELETE_ID, R.string.menu_delete).
        setShortcut(KeyEvent.KEYCODE_2, 0, KeyEvent.KEYCODE_D)
      menu.addSeparator(Menu.SELECTED_ALTERNATIVE, 0)

      // Give a shortcut to the edit action.
      if (items(0) != null)
        items(0).setShortcut(KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_E)
    }
    else
      menu removeGroup Menu.SELECTED_ALTERNATIVE

    // Make sure the delete action is disabled if there are no items.
    menu.setItemShown(DELETE_ID, haveItems)
    true
  }

  override def onOptionsItemSelected(item: Menu.Item): Boolean = item.getId() match {
    case DELETE_ID =>
      deleteItem()
      true
    case INSERT_ID =>
      insertItem()
      true
    case _ =>
      super.onOptionsItemSelected(item)
  }

  override protected def onListItemClick(l: ListView, v: View,
                                         position: Int, id: Long) {
    val url = getIntent().getData() addId getSelectionRowID()

    val action = getIntent().getAction()
    if ((Intent.PICK_ACTION equals action) ||
        (Intent.GET_CONTENT_ACTION equals action))
      // The caller is waiting for us to return a note selected by
      // the user.  The have clicked on one, so return it now.
      setResult(RESULT_OK, url.toString())
    else
      // Launch activity to view/edit the currently selected item
      startActivity(new Intent(Intent.EDIT_ACTION, url))
  }

  private final def deleteItem() {
    mCursor moveTo getSelection()
    mCursor.deleteRow()
  }

  private final def insertItem() {
    // Launch activity to insert a new item
    startActivity(new Intent(Intent.INSERT_ACTION, getIntent().getData()))
  }
}
