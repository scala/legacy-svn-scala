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

package com.example.android.apis.view

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.ListActivity
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.ContactsContract.CommonDataKinds.{Nickname, Phone}
import android.provider.ContactsContract.Contacts
import android.os.Bundle
import android.view.View
import android.widget.{AdapterView, ListAdapter, SimpleCursorAdapter, TextView}
import android.widget.AdapterView.OnItemSelectedListener

/**
 * A list view example where the data comes from a cursor.
 */
class List7 extends ListActivity with OnItemSelectedListener {
  import List7._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.list_7)
    mPhone = findViewById(R.id.phone).asInstanceOf[TextView]
    getListView setOnItemSelectedListener this

    // Get a cursor with all people
    val c = getContentResolver.query(Contacts.CONTENT_URI, PROJECTION, null, null, null)
    startManagingCursor(c)
    mPhoneColumnIndex = c.getColumnIndex(Phone.NUMBER)

    val adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, // Use a template that
                                                     // displays a text view
                c, // Give the cursor to the list adatper
                Array(Nickname.NAME), // Map the NAME column in the
                                      // people database to...
                Array(android.R.id.text1)) // The "text1" view defined in
                                           // the XML template
    setListAdapter(adapter)
  }

  def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long) {
    if (position >= 0) {
      val c = parent.getItemAtPosition(position).asInstanceOf[Cursor]
      mPhone setText c.getString(mPhoneColumnIndex)
    }
  }

  def onNothingSelected(parent: AdapterView[_]) {
    mPhone setText R.string.list_7_nothing
  }

  private var mPhoneColumnIndex: Int = _
  private var mPhone: TextView = _
}

object List7 {
  private val PROJECTION = Array(BaseColumns._ID, Nickname.NAME, Phone.NUMBER)
}
