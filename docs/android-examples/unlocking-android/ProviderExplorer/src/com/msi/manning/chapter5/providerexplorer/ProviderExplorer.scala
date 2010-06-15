/*
 * Copyright (C) 2009 Manning Publications Co.
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

package com.msi.manning.chapter5.providerexplorer

import android.app.Activity
import android.content.{ContentResolver, ContentValues, Context, Intent}
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.{BaseColumns, Contacts}
import android.provider.Contacts.{PeopleColumns, PhonesColumns}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams
import android.widget.{Button, EditText, LinearLayout, TextView}

import scala.collection.mutable.ListBuffer

class ProviderExplorer extends Activity {
  import ProviderExplorer._  // companion object

  private var addName: EditText = _
  private var addPhoneNumber: EditText = _
  private var editName: EditText = _
  private var editPhoneNumber: EditText = _
  private var addContactBtn: Button = _
  private var editContactBtn: Button = _

  private var contactId: Long = _

  private def find[V <: View](id: Int) = findViewById(id).asInstanceOf[V]

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.provider_explorer)

    addName = find(R.id.add_name)
    addPhoneNumber = find(R.id.add_phone_number)
    editName = find(R.id.edit_name)
    editPhoneNumber = find(R.id.edit_phone_number)
    addContactBtn = find(R.id.add_contact_button)
    addContactBtn setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        addContact()
      }
    }
    editContactBtn = find(R.id.edit_contact_button)
    editContactBtn setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        editContact()
      }
    }
  }

  override def onPause() {
    super.onPause()
  }

  override def onStart() {
    super.onStart()
    val contacts = getContacts
    val params = new LinearLayout.LayoutParams(200, LayoutParams.WRAP_CONTENT)
    if (contacts != null) {
      val editLayout: LinearLayout = find(R.id.edit_buttons_layout)
      val deleteLayout: LinearLayout = find(R.id.delete_buttons_layout)
      params.setMargins(10, 0, 0, 0)
      for (c <- contacts) {
        val contactEditButton = new ContactButton(this, c)
        contactEditButton setText c.toString
        editLayout.addView(contactEditButton, params)
        contactEditButton setOnClickListener new OnClickListener() {
          def onClick(v: View) {
            val view = v.asInstanceOf[ContactButton]
            editName setText view.contact.name
            editPhoneNumber setText view.contact.phoneNumber
            contactId = view.contact.id
          }
        }

        val contactDeleteButton = new ContactButton(this, c)
        contactDeleteButton.setText("Delete " + c.name)
        deleteLayout.addView(contactDeleteButton, params)
        contactDeleteButton setOnClickListener new OnClickListener() {
          def onClick(v: View) {
            val view = v.asInstanceOf[ContactButton]
            contactId = view.contact.id
            deleteContact()
          }
        }
      }
    } else {
      val layout: LinearLayout = find(R.id.edit_buttons_layout)
      val empty = new TextView(this)
      empty setText "No current contacts"
      layout.addView(empty, params)
    }
  }

  //
  // begin resolver methods
  //

  private def getContacts: List[Contact] = {
    var results: ListBuffer[Contact] = null
    var id = 0L
    var name: String = null
    var phoneNumber: String = null
    val projection = Array(BaseColumns._ID, PeopleColumns.NAME,
                           PhonesColumns.NUMBER)
    val resolver = getContentResolver
    val cur = resolver.query(Contacts.People.CONTENT_URI, projection,
                             null, null, Contacts.People.DEFAULT_SORT_ORDER)
    while (cur.moveToNext()) {
      if (results == null) {
        results = new ListBuffer[Contact]()
      }
      id = cur.getLong(cur getColumnIndex BaseColumns._ID)
      name = cur.getString(cur getColumnIndex PeopleColumns.NAME)
      phoneNumber = cur.getString(cur getColumnIndex PhonesColumns.NUMBER)
      results append new Contact(id, name, phoneNumber)
    }
    results.toList
  }

  private def addContact() {
    val resolver = getContentResolver
    val values = new ContentValues()

    // create Contacts.People record first, using helper method to get
    // person in "My Contacts" group
    values.put(PeopleColumns.NAME, addName.getText.toString)
    val personUri =
      Contacts.People.createPersonInMyContactsGroup(resolver, values)
    Log.v("ProviderExplorer", "ADD personUri - " + personUri.toString)

    // append other contact data, like phone number
    values.clear()
    val phoneUri = Uri.withAppendedPath(personUri,
                     Contacts.People.Phones.CONTENT_DIRECTORY)
    Log.v("ProviderExplorer", "ADD phoneUri - " + phoneUri.toString)
    values.put(PhonesColumns.TYPE, PhonesColumns.TYPE_MOBILE.toString)
    values.put(PhonesColumns.NUMBER, addPhoneNumber.getText.toString)

    // insert manually (this time no helper method)
    resolver.insert(phoneUri, values)

    startActivity(new Intent(this, classOf[ProviderExplorer]))
  }

  private def deleteContact() {
    var personUri = Contacts.People.CONTENT_URI
    personUri =
      personUri.buildUpon().appendPath(contactId.toString).build()
    Log.v("ProviderExplorer", "DELETE personUri - " + personUri.toString)
    getContentResolver.delete(personUri, null, null)
    startActivity(new Intent(this, classOf[ProviderExplorer]))
  }

  private def editContact() {
    val resolver = getContentResolver
    val values = new ContentValues()

    // another way to append to a Uri, use buildUpon
    val personUri = Contacts.People.CONTENT_URI.buildUpon()
      .appendPath(contactId.toString).build()
    Log.v("ProviderExplorer", "EDIT personUri - " + personUri.toString)

    // once we have the person Uri we can edit person values, like name
    values.put(PeopleColumns.NAME, editName.getText.toString)
    resolver.update(personUri, values, null, null)

    // separate step to update phone values
    values.clear()
    // just edit the first phone, with id 1
    // (in real life we would need to parse more phone data and edit the
    // correct phone out of a possible many)
    val phoneUri = Uri.withAppendedPath(personUri,
                     Contacts.People.Phones.CONTENT_DIRECTORY + "/1")
    values.put(PhonesColumns.NUMBER, editPhoneNumber.getText.toString)
    resolver.update(phoneUri, values, null, null)

    startActivity(new Intent(this, classOf[ProviderExplorer]))
  }

  //
  // end resolver methods
  //
}

object ProviderExplorer {

  //
  // addtl classes
  //

  private class Contact(val id: Long, val name: String, val phoneNumber: String) {
    override def toString: String = name + "\n" + phoneNumber
  }

  private class ContactButton(ctx: Context, val contact: Contact)
  extends Button(ctx) {
  }

}
