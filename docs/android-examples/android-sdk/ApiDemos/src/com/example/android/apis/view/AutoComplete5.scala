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
import android.content.ContentResolver
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.Contacts.{OrganizationColumns, People}
import android.provider.ContactsContract.CommonDataKinds.{Nickname, Phone}
import android.widget.AutoCompleteTextView

class AutoComplete5 extends Activity {
  import AutoComplete5._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.autocomplete_5)

    val content = getContentResolver()
    val cursor = content.query(People.CONTENT_URI,
                PEOPLE_PROJECTION, null, null, People.DEFAULT_SORT_ORDER)
    val adapter = new AutoComplete4.ContactListAdapter(this, cursor)

    val textView = findViewById(R.id.edit).asInstanceOf[AutoCompleteTextView]
    textView setAdapter adapter
  }

}

object AutoComplete5 {
  private final val PEOPLE_PROJECTION = Array(
    BaseColumns._ID,
    People.PRIMARY_PHONE_ID,
    OrganizationColumns.TYPE,
    Phone.NUMBER,
    OrganizationColumns.LABEL,
    Nickname.NAME)
}
