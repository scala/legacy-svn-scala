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

import android.app.Activity._
import android.app.ListActivity
import android.content.{ComponentName, ContentUris, Intent}
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.{ContextMenu, Menu, MenuItem, View}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.{AdapterView, ListView, SimpleCursorAdapter}
import android.widget.AdapterView.AdapterContextMenuInfo

object NotesList {
  private final val TAG = "NotesList"

  // Menu item ids
  final val MENU_ITEM_DELETE = Menu.FIRST
  final val MENU_ITEM_INSERT = Menu.FIRST + 1

  /** The columns we are interested in from the database */
  private final val PROJECTION = Array(Notes._ID, Notes.TITLE)

  /** The index of the title column */
  private final val COLUMN_INDEX_TITLE = 1
}

/** Displays a list of notes. Will display notes from the {@link Uri}
 *  provided in the intent if there is one, otherwise defaults to displaying the
 *  contents of the {@link NotePadProvider}
 */
class NotesList extends ListActivity {
  import NotesList._ // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT)

    // If no data was given in the intent (because we were started
    // as a MAIN activity), then use our default content provider.
    val intent = getIntent
    if (intent.getData == null)
      intent setData Notes.CONTENT_URI

    // Inform the list we provide context menus for items
    getListView setOnCreateContextMenuListener this
        
    // Perform a managed query. The Activity will handle closing and requerying the cursor
    // when needed.
    val cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                              Notes.DEFAULT_SORT_ORDER)

    // Used to map notes entries from the database to views
    val adapter = new SimpleCursorAdapter(this, R.layout.noteslist_item, cursor,
                                          Array(Notes.TITLE), Array(android.R.id.text1))
    setListAdapter(adapter)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    // This is our one standard application action -- inserting a
    // new note into the list.
    menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert)
        .setShortcut('3', 'a')
        .setIcon(android.R.drawable.ic_menu_add)

    // Generate any additional actions that can be performed on the
    // overall list.  In a normal install, there are no additional
    // actions found here, but this allows other applications to extend
    // our menu with their own actions.
    val intent = new Intent(null, getIntent().getData())
    intent addCategory Intent.CATEGORY_ALTERNATIVE
    menu.addIntentOptions(
      Menu.CATEGORY_ALTERNATIVE, 0, 0,
      new ComponentName(this, classOf[NotesList]), null, intent, 0, null)
    true
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    super.onPrepareOptionsMenu(menu)
    val haveItems = getListAdapter.getCount > 0

    // If there are any notes in the list (which implies that one of
    // them is selected), then we need to generate the actions that
    // can be performed on the current selection.  This will be a combination
    // of our own specific actions along with any extensions that can be
    // found.
    if (haveItems) {
      // This is the selected item.
      val uri = ContentUris.withAppendedId(getIntent.getData, getSelectedItemId)

      // Build menu...  always starts with the EDIT action...
      val specifics = new Array[Intent](1)
      specifics(0) = new Intent(Intent.ACTION_EDIT, uri)
      val items = new Array[MenuItem](1)

      // ... is followed by whatever other actions are available...
      val intent = new Intent(null, uri)
      intent addCategory Intent.CATEGORY_ALTERNATIVE
      menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null,
                            specifics, intent, 0, items)

      // Give a shortcut to the edit action.
      if (items(0) != null)
        items(0).setShortcut('1', 'e')
    } else {
      menu removeGroup Menu.CATEGORY_ALTERNATIVE
    }

    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case MENU_ITEM_INSERT =>
        // Launch activity to insert a new item
        startActivity(new Intent(Intent.ACTION_INSERT, getIntent.getData))
        true;
      case _ =>
        super.onOptionsItemSelected(item)
    }

  override def onCreateContextMenu(menu: ContextMenu, view: View,
                                   menuInfo: ContextMenuInfo) {
    val info: AdapterContextMenuInfo =
      try { menuInfo.asInstanceOf[AdapterContextMenuInfo] }
      catch {
        case e: ClassCastException =>
          Log.e(TAG, "bad menuInfo", e)
          return
      }

    val cursor = getListAdapter.getItem(info.position).asInstanceOf[Cursor]
    if (cursor == null) {
      // For some reason the requested item isn't available, do nothing
      return
    }

    // Setup the menu header
    menu setHeaderTitle cursor.getString(COLUMN_INDEX_TITLE)

    // Add a menu item to delete the note
    menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete)
  }
        
  override def onContextItemSelected(item: MenuItem): Boolean = {
    val info: AdapterContextMenuInfo =
      try { item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo] }
      catch {
        case e: ClassCastException =>
          Log.e(TAG, "bad menuInfo", e)
          return false
      }

    item.getItemId() match {
      case MENU_ITEM_DELETE =>
        // Delete the note that the context menu is for
        val noteUri = ContentUris.withAppendedId(getIntent.getData, info.id)
        getContentResolver.delete(noteUri, null, null)
        true
      case _ =>
        false
    }
  }

  override protected def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val uri = ContentUris.withAppendedId(getIntent.getData, id)
        
    val action = getIntent.getAction
    if ((Intent.ACTION_PICK equals action) ||
        (Intent.ACTION_GET_CONTENT equals action)) {
      // The caller is waiting for us to return a note selected by
      // the user.  The have clicked on one, so return it now.
      setResult(RESULT_OK, new Intent setData uri)
    }
    else {
      // Launch activity to view/edit the currently selected item
      startActivity(new Intent(Intent.ACTION_EDIT, uri))
    }
  }
}
