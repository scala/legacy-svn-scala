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

package com.example.android.contactmanager

import scala.android.app.Activity
import scala.android.provider.ContactsContract
import scala.android.provider.ContactsContract.CommonDataKinds.{Email, Phone, StructuredName}

import android.accounts.{Account, AccountManager, AuthenticatorDescription,
                         OnAccountsUpdateListener}
import android.content.{ContentProviderOperation, Context}
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ArrayAdapter, Button, EditText, ImageView,
                       Spinner, TextView, Toast}

import java.util.{ArrayList, Iterator}

object ContactAdder {
  final val TAG =
    "ContactsAdder"
  final val ACCOUNT_NAME =
    "com.example.android.contactmanager.ContactsAdder.ACCOUNT_NAME"
  final val ACCOUNT_TYPE =
    "com.example.android.contactmanager.ContactsAdder.ACCOUNT_TYPE"

  // Prepare list of supported account types
  // Note: Other types are available in ContactsContract.CommonDataKinds
  //       Also, be aware that type IDs differ between Phone and Email, and
  //       MUST be computed separately.
  private final val mContactEmailTypes = Array(
    Phone.TYPE_HOME,
    Phone.TYPE_WORK,
    Phone.TYPE_MOBILE,
    Phone.TYPE_OTHER)
  private final val mContactPhoneTypes = Array(
    Phone.TYPE_HOME,
    Phone.TYPE_WORK,
    Phone.TYPE_MOBILE,
    Phone.TYPE_OTHER)

  /**
   * Obtain the AuthenticatorDescription for a given account type.
   * @param type The account type to locate.
   * @param dictionary An array of AuthenticatorDescriptions, as returned by AccountManager.
   * @return The description for the specified account type.
   */
  private def getAuthenticatorDescription(dtype: String,
            dictionary: Array[AuthenticatorDescription]): AuthenticatorDescription = {
    for (i <- 0 until dictionary.length) {
      if (dictionary(i).`type` equals dtype) {
        return dictionary(i)
      }
    }
    // No match found
    throw new RuntimeException("Unable to find matching authenticator")
  }
}

final class ContactAdder extends Activity with OnAccountsUpdateListener {
  import ContactAdder._ // companion object

  private var mAccounts: ArrayList[AccountData] = _
  private var mAccountAdapter: AccountAdapter = _
  private var mAccountSpinner: Spinner = _
  private var mContactEmailEditText: EditText = _
  private var mContactEmailTypeSpinner: Spinner = _
  private var mContactNameEditText: EditText = _
  private var mContactPhoneEditText: EditText = _
  private var mContactPhoneTypeSpinner: Spinner = _
  private var mContactSaveButton: Button = _
  private var mSelectedAccount: AccountData = _

  /**
   * Called when the activity is first created. Responsible for initializing the UI.
   */
  override def onCreate(savedInstanceState: Bundle) {
    Log.v(TAG, "Activity State: onCreate()")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.contact_adder)

    // Obtain handles to UI objects
    mAccountSpinner = findView(R.id.accountSpinner)
    mContactNameEditText = findView(R.id.contactNameEditText)
    mContactPhoneEditText = findView(R.id.contactPhoneEditText)
    mContactEmailEditText = findView(R.id.contactEmailEditText)
    mContactPhoneTypeSpinner = findView(R.id.contactPhoneTypeSpinner)
    mContactEmailTypeSpinner = findView(R.id.contactEmailTypeSpinner)
    mContactSaveButton = findView(R.id.contactSaveButton)

    // Prepare model for account spinner
    mAccounts = new ArrayList[AccountData]()
    mAccountAdapter = new AccountAdapter(this, mAccounts)
    mAccountSpinner setAdapter mAccountAdapter

    // Populate list of account types for phone
    var adapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_item)
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    for (phoneType <- mContactPhoneTypes) {
      adapter.add(Phone.getTypeLabel(
            this.getResources,
            phoneType,
            getString(R.string.undefinedTypeLabel)).toString)
    }
    mContactPhoneTypeSpinner setAdapter adapter
    mContactPhoneTypeSpinner setPrompt getString(R.string.selectLabel)

    // Populate list of account types for email
    adapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_item)
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    for (emailType <- mContactEmailTypes) {
      adapter.add(Email.getTypeLabel(
            this.getResources(),
            emailType,
            getString(R.string.undefinedTypeLabel)).toString)
    }
    mContactEmailTypeSpinner setAdapter adapter
    mContactEmailTypeSpinner setPrompt getString(R.string.selectLabel)

    // Prepare the system account manager. On registering the listener below,
    // we also ask for an initial callback to pre-populate the account list.
    AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true)

    // Register handlers for UI elements
    mAccountSpinner setOnItemSelectedListener {
      updateAccountSelection()
    }

    mContactSaveButton setOnClickListener {
      Log.v(TAG, "Save button clicked")
      createContactEntry()
      finish()
    }
  }

  /**
   * Creates a contact entry from the current UI values in the account named
   * by mSelectedAccount.
   */
  protected def createContactEntry() {
    // Get values from UI
    val name = mContactNameEditText.getText.toString
    val phone = mContactPhoneEditText.getText.toString
    val email = mContactEmailEditText.getText.toString
    val phoneType = mContactPhoneTypes(mContactPhoneTypeSpinner.getSelectedItemPosition)
    val emailType = mContactEmailTypes(mContactEmailTypeSpinner.getSelectedItemPosition)

    // Prepare contact creation request
    //
    // Note: We use RawContacts because this data must be associated with a
    //       particular account. The system will aggregate this with any other
    //       data for this contact and create a coresponding entry in the 
    //       ContactsContract.Contacts provider for us.
    val ops = new ArrayList[ContentProviderOperation]()
    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, mSelectedAccount.getType)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, mSelectedAccount.getName)
                .build())
    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                           StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, name)
                .build())
    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                           Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, phone)
                .withValue(Phone.TYPE, phoneType)
                .build())
    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                           Email.CONTENT_ITEM_TYPE)
                .withValue(Email.DATA, email)
                .withValue(Email.TYPE, emailType)
                .build())

    // Ask the Contact provider to create a new contact
    Log.i(TAG,"Selected account: " + mSelectedAccount.getName + " (" +
                                     mSelectedAccount.getType + ")")
    Log.i(TAG,"Creating contact: " + name)
    try {
      getContentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
    } catch {
      case e: Exception =>
        // Display warning
        val ctx = getApplicationContext()
        val txt = getString(R.string.contactCreationFailure)
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(ctx, txt, duration)
        toast.show()

        // Log exception
        Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e)
    }
  }

  /**
   * Called when this activity is about to be destroyed by the system.
   */
  override def onDestroy() {
    // Remove AccountManager callback
    AccountManager.get(this).removeOnAccountsUpdatedListener(this)
    super.onDestroy()
  }

  /**
   * Updates account list spinner when the list of Accounts on the system
   * changes. Satisfies OnAccountsUpdateListener implementation.
   */
  def onAccountsUpdated(a: Array[Account]) {
    Log.i(TAG, "Account list update detected")
    // Clear out any old data to prevent duplicates
    mAccounts.clear()

    // Get account data from system
    val accountTypes = AccountManager.get(this).getAuthenticatorTypes

    // Populate tables
    for (i <- 0 until a.length) {
      // The user may have multiple accounts with the same name, so we need to
      // construct a meaningful display name for each.
      val systemAccountType = a(i).`type`
      val ad = getAuthenticatorDescription(systemAccountType, accountTypes)
      val data = new AccountData(a(i).name, ad)
      mAccounts add data
    }

    // Update the account spinner
    mAccountAdapter.notifyDataSetChanged()
  }

  /**
   * Update account selection. If NO_ACCOUNT is selected, then we prohibit
   * inserting new contacts.
   */
  private def updateAccountSelection() {
    // Read current account selection
    mSelectedAccount = mAccountSpinner.getSelectedItem.asInstanceOf[AccountData]
  }

  /**
   * A container class used to repreresent all known information about an account.
   *
   * @param name The name of the account. This is usually the user's email
   *        address or username.
   * @param description The description for this account. This will be dictated
   *        by the type of account returned, and can be obtained from the
   *        system AccountManager.
   */
  private class AccountData(name: String, description: AuthenticatorDescription) {
    private val mName = name
    private var mType: String = _
    private var mTypeLabel: CharSequence = _
    private var mIcon: Drawable = _

    if (description != null) {
      mType = description.`type`

      // The type string is stored in a resource, so we need
      // to convert it into something human readable.
      val packageName = description.packageName
      val pm = getPackageManager

      if (description.labelId != 0) {
        mTypeLabel = pm.getText(packageName, description.labelId, null)
        if (mTypeLabel == null)
          throw new IllegalArgumentException(
            "LabelID provided, but label not found")
      } else {
        mTypeLabel = ""
      }

      if (description.iconId != 0) {
        mIcon = pm.getDrawable(packageName, description.iconId, null)
        if (mIcon == null)
          throw new IllegalArgumentException(
            "IconID provided, but drawable not found")
      } else {
        mIcon = getResources getDrawable android.R.drawable.sym_def_app_icon
      }
    }

    def getName: String = mName

    def getType: String = mType

    def getTypeLabel: CharSequence = mTypeLabel

    def getIcon: Drawable = mIcon

    override def toString: String = mName
  }

  /**
   * Custom adapter used to display account icons and descriptions in the
   * account spinner.
   */
  private class AccountAdapter(context: Context, accountData: ArrayList[AccountData])
  extends ArrayAdapter(context, android.R.layout.simple_spinner_item, accountData) {
    setDropDownViewResource(R.layout.account_entry)

    override def getDropDownView(position: Int, convertView: View, parent: ViewGroup): View = {
      // Inflate a view template
      val convertView1 = if (convertView == null) {
        val layoutInflater = getLayoutInflater
        layoutInflater.inflate(R.layout.account_entry, parent, false)
      } else
        convertView
      val firstAccountLine = convertView.findViewById(R.id.firstAccountLine).asInstanceOf[TextView]
      val secondAccountLine = convertView.findViewById(R.id.secondAccountLine).asInstanceOf[TextView]
      val accountIcon = convertView.findViewById(R.id.accountIcon).asInstanceOf[ImageView]

      // Populate template
      val data = getItem(position)
      firstAccountLine setText data.getName
      secondAccountLine setText data.getTypeLabel
      var icon = data.getIcon
      if (icon == null)
        icon = getResources getDrawable android.R.drawable.ic_menu_search
      accountIcon setImageDrawable icon
      convertView1
    }
  }

}
