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

package com.example.android.phonedialer

import android.app.ListActivity
import android.content.{ContentUris, Intent}
import android.database.Cursor
import android.os.Bundle
import android.provider.Contacts.{People, PeopleColumns, Phones}
import android.widget.{ListAdapter, ListView, SimpleCursorAdapter}
import android.view.View

/**
 *  Based on YouTube demo by Dan Morrill
 *  (http://www.youtube.com/watch?v=I6ObTqIiYfE)
 */
class PhoneDialer extends ListActivity {
  private var mAdapter1: ListAdapter = _

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val c = getContentResolver.query(People.CONTENT_URI, null, null, null, null)
    startManagingCursor(c)

    val columns = Array(PeopleColumns.NAME)
    val names = Array(R.id.row_entry)
    
    mAdapter1 = new SimpleCursorAdapter(this, R.layout.main, c, columns, names)
    setListAdapter(mAdapter1)
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    super.onListItemClick(l, v, position, id)
    val intent = new Intent(Intent.ACTION_CALL)
    val cursor = mAdapter1.getItem(position).asInstanceOf[Cursor]
    val phoneId = cursor getLong cursor.getColumnIndex(People.PRIMARY_PHONE_ID)
    //i setData Phones.CONTENT_URI.addId(phoneId)
    val uri = ContentUris.withAppendedId(Phones.CONTENT_URI, phoneId)
    intent setData uri

    startActivity(intent)
  }
}
