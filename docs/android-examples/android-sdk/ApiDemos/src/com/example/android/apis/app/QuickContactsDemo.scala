/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.apis.app

import com.example.android.apis.R

import scala.android.provider.ContactsContract.Contacts

import android.app.ListActivity
import android.content.Context
import android.database.{CharArrayBuffer, Cursor}
import android.os.Bundle
import android.provider.BaseColumns
import android.view.{View, ViewGroup}
import android.widget.{QuickContactBadge, ResourceCursorAdapter, TextView}

class QuickContactsDemo extends ListActivity {
  import QuickContactsDemo._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND (" +
                 Contacts.HAS_PHONE_NUMBER + "=1) AND (" +
                 Contacts.DISPLAY_NAME + " != '' ))"
    val c: Cursor = getContentResolver.query(Contacts.CONTENT_URI,
                CONTACTS_SUMMARY_PROJECTION, select,
                null, Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC")
    startManagingCursor(c)
    val adapter = new ContactListItemAdapter(this, R.layout.quick_contacts, c)
    setListAdapter(adapter)
  }

  private final class ContactListItemAdapter(context: Context,
                                             layout: Int, c: Cursor)
              extends ResourceCursorAdapter(context, layout, c) {

    override def bindView(view: View, context: Context, cursor: Cursor) {
      val cache = view.getTag.asInstanceOf[ContactListItemCache]
      val nameView = cache.nameView
      val photoView = cache.photoView
      // Set the name
      cursor.copyStringToBuffer(SUMMARY_NAME_COLUMN_INDEX, cache.nameBuffer)
      val size = cache.nameBuffer.sizeCopied
      cache.nameView.setText(cache.nameBuffer.data, 0, size)
      val contactId = cursor getLong SUMMARY_ID_COLUMN_INDEX
      val lookupKey = cursor getString SUMMARY_LOOKUP_KEY
      cache.photoView.assignContactUri(
        Contacts.getLookupUri(contactId, lookupKey))
    }

    override def newView(context: Context, cursor: Cursor,
                         parent: ViewGroup): View = {
      val view = super.newView(context, cursor, parent)
      val cache = new ContactListItemCache()
      cache.nameView = view.findViewById(R.id.name).asInstanceOf[TextView]
      cache.photoView =
        view.findViewById(R.id.badge).asInstanceOf[QuickContactBadge]
      view setTag cache
      view
    }
  }

}

object QuickContactsDemo {

  private final val CONTACTS_SUMMARY_PROJECTION = Array(
    Contacts._ID, // 0
    Contacts.DISPLAY_NAME, // 1
    Contacts.STARRED, // 2
    Contacts.TIMES_CONTACTED, // 3
    Contacts.CONTACT_PRESENCE, // 4
    Contacts.PHOTO_ID, // 5
    Contacts.LOOKUP_KEY, // 6
    Contacts.HAS_PHONE_NUMBER  // 7
  )

  private final val SUMMARY_ID_COLUMN_INDEX = 0
  private final val SUMMARY_NAME_COLUMN_INDEX = 1
  private final val SUMMARY_STARRED_COLUMN_INDEX = 2
  private final val SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 3
  private final val SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 4
  private final val SUMMARY_PHOTO_ID_COLUMN_INDEX = 5
  private final val SUMMARY_LOOKUP_KEY = 6
  private final val SUMMARY_HAS_PHONE_COLUMN_INDEX = 7

  private final class ContactListItemCache {
    var nameView: TextView = _
    var photoView: QuickContactBadge = _
    val nameBuffer = new CharArrayBuffer(128)
  }
}
