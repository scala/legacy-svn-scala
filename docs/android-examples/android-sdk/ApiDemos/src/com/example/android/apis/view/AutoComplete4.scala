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

import com.example.android.apis.R

import android.app.Activity
import android.content.{ContentResolver, Context}
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.Contacts.{OrganizationColumns, People}
import android.provider.ContactsContract.CommonDataKinds.{Nickname, Phone}
import android.view.LayoutInflater
import android.view.{View, ViewGroup}
import android.widget.{AutoCompleteTextView, CursorAdapter, Filterable, TextView}

class AutoComplete4 extends Activity {
  import AutoComplete4._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.autocomplete_4)

    val content = getContentResolver()
    val cursor = content.query(People.CONTENT_URI,
                               PEOPLE_PROJECTION, null, null,
                               People.DEFAULT_SORT_ORDER)
    val adapter = new ContactListAdapter(this, cursor)

    val textView = findViewById(R.id.edit).asInstanceOf[AutoCompleteTextView]
    textView setAdapter adapter
  }

}

object AutoComplete4 {

  // XXX compiler bug in javac 1.5.0_07-164, we need to implement Filterable
  // to make compilation work
  class ContactListAdapter(context: Context, c: Cursor)
  extends CursorAdapter(context, c) with Filterable {

    private val mContent = context.getContentResolver

    override def newView(context: Context, cursor: Cursor, parent: ViewGroup): View = {
      val inflater = LayoutInflater.from(context)
      val view = inflater.inflate(
        android.R.layout.simple_dropdown_item_1line, parent, false).asInstanceOf[TextView]
      view setText cursor.getString(5)
      view
    }

    override def bindView(view: View, context: Context, cursor: Cursor) {
      view.asInstanceOf[TextView] setText cursor.getString(5)
    }

    override def convertToString(cursor: Cursor): String =
      cursor.getString(5)

    override def runQueryOnBackgroundThread(constraint: CharSequence): Cursor = {
      if (getFilterQueryProvider != null) {
        return getFilterQueryProvider runQuery constraint
      }

      var buffer: StringBuilder = null
      var args: Array[String] = null
      if (constraint != null) {
        buffer = new StringBuilder()
        buffer append "UPPER("
        buffer append Nickname.NAME
        buffer append ") GLOB ?"
        args = Array(constraint.toString.toUpperCase() + "*")
      }
      mContent.query(People.CONTENT_URI, PEOPLE_PROJECTION,
                     if (buffer == null) null else buffer.toString, args,
                     People.DEFAULT_SORT_ORDER)
    }
       
  }

  private final val PEOPLE_PROJECTION = Array(
    BaseColumns._ID,
    People.PRIMARY_PHONE_ID,
    OrganizationColumns.TYPE,
    Phone.NUMBER,
    OrganizationColumns.LABEL,
    Nickname.NAME)
}
