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

import android.app.ExpandableListActivity
import android.content.{ContentUris, Context}
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.Contacts.People
import android.provider.ContactsContract.CommonDataKinds.{Nickname, Phone}
import android.widget.{ExpandableListAdapter, SimpleCursorTreeAdapter}

/**
 * Demonstrates expandable lists backed by Cursors
 */
class ExpandableList2 extends ExpandableListActivity {
  import ExpandableList2._  // companion object

  private var mGroupIdColumnIndex: Int = _    
  private var mAdapter: ExpandableListAdapter = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Query for people
    val groupCursor = managedQuery(People.CONTENT_URI,
                Array(BaseColumns._ID, Nickname.NAME), null, null, null)

    // Cache the ID column index
    mGroupIdColumnIndex = groupCursor getColumnIndexOrThrow BaseColumns._ID

    // Set up our adapter
    mAdapter = new MyExpandableListAdapter(groupCursor,
                this,
                android.R.layout.simple_expandable_list_item_1,
                Array(Nickname.NAME), // Name for group layouts
                Array(android.R.id.text1),
                android.R.layout.simple_expandable_list_item_1,
                Array(Phone.NUMBER), // Number for child layouts
                Array(android.R.id.text1))
    setListAdapter(mAdapter)
  }

  class MyExpandableListAdapter(cursor: Cursor, context: Context,
                                groupLayout: Int,
                                groupFrom: Array[String],
                                groupTo: Array[Int],
                                childLayout: Int,
                                childrenFrom: Array[String],
                                childrenTo: Array[Int])
  extends SimpleCursorTreeAdapter(context, cursor,
                                  groupLayout, groupFrom, groupTo,
                                  childLayout, childrenFrom, childrenTo) {

    override protected def getChildrenCursor(groupCursor: Cursor): Cursor = {
      // Given the group, we return a cursor for all the children within that group 

      // Return a cursor that points to this contact's phone numbers
      val builder = People.CONTENT_URI.buildUpon()
      ContentUris.appendId(builder, groupCursor getLong mGroupIdColumnIndex)
      builder appendEncodedPath People.Phones.CONTENT_DIRECTORY
      val phoneNumbersUri = builder.build()

      // The returned Cursor MUST be managed by us, so we use Activity's helper
      // functionality to manage it for us.
      managedQuery(phoneNumbersUri, mPhoneNumberProjection, null, null, null)
      }

    }
}

object ExpandableList2 {
  private val mPhoneNumberProjection = Array(
    BaseColumns._ID, Phone.NUMBER
  )
}
