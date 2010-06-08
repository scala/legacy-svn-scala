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

package com.example.android.apis.app

import android.app.{Activity, AlertDialog, Dialog, ProgressDialog}
import android.content.DialogInterface
import android.content.DialogInterface._
import android.os.{Bundle, Handler, Message}
import android.view.{LayoutInflater, View}
import android.view.View.OnClickListener
import android.widget.Button

import com.example.android.apis.R

/**
 * Example of how to use an {@link android.app.AlertDialog}.
 * <h3>AlertDialogSamples</h3>

<p>This demonstrates the different ways the AlertDialog can be used.</p>

<h4>Demo</h4>
App/Dialog/Alert Dialog
 
<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com.example.android.apis/app/AlertDialogSamples.java</td>
 *             <td >The Alert Dialog Samples implementation</td>
 *         </tr>
 *         <tr>
 *             <td >/res/any/layout/alert_dialog.xml</td>
 *             <td >Defines contents of the screen</td>
 *         </tr>
 * </table> 
 */
object AlertDialogSamples {
  private final val DIALOG_YES_NO_MESSAGE = 1
  private final val DIALOG_YES_NO_LONG_MESSAGE = 2
  private final val DIALOG_LIST = 3
  private final val DIALOG_PROGRESS = 4
  private final val DIALOG_SINGLE_CHOICE = 5
  private final val DIALOG_MULTIPLE_CHOICE = 6
  private final val DIALOG_TEXT_ENTRY = 7

  private final val MAX_PROGRESS = 100
}

class AlertDialogSamples extends Activity {
  import AlertDialogSamples._  // companion object

  private var mProgressDialog: ProgressDialog = _
  private var mProgress: Int = _
  private var mProgressHandler: Handler = _

  override protected def onCreateDialog(id: Int): Dialog = {
    val dummyListener =
      new DialogInterface.OnClickListener {
        def onClick(dialog: DialogInterface, whichButton: Int) {
          /* User clicked OK so do some stuff */
        }
      }
    id match {
      case DIALOG_YES_NO_MESSAGE =>
        new AlertDialog.Builder(AlertDialogSamples.this)
          .setIcon(R.drawable.alert_dialog_icon)
          .setTitle(R.string.alert_dialog_two_buttons_title)
          .setPositiveButton(R.string.alert_dialog_ok, dummyListener)
          .setNegativeButton(R.string.alert_dialog_cancel, dummyListener)
          .create()
      case DIALOG_YES_NO_LONG_MESSAGE =>
        new AlertDialog.Builder(AlertDialogSamples.this)
          .setIcon(R.drawable.alert_dialog_icon)
          .setTitle(R.string.alert_dialog_two_buttons_msg)
          .setMessage(R.string.alert_dialog_two_buttons2_msg)
          .setPositiveButton(R.string.alert_dialog_ok, dummyListener)
          .setNeutralButton(R.string.alert_dialog_something, dummyListener)
          .setNegativeButton(R.string.alert_dialog_cancel, dummyListener)
          .create()
      case DIALOG_LIST =>
        new AlertDialog.Builder(AlertDialogSamples.this)
          .setTitle(R.string.select_dialog)
          .setItems(R.array.select_dialog_items,
            new DialogInterface.OnClickListener {
              def onClick(dialog: DialogInterface, which: Int) {
                /* User clicked so do some stuff */
                val items = getResources.getStringArray(R.array.select_dialog_items)
                new AlertDialog.Builder(AlertDialogSamples.this)
                  .setMessage("You selected: " + which + " , " + items(which))
                  .show();
              }
            })
          .create()
      case DIALOG_PROGRESS =>
        mProgressDialog = new ProgressDialog(AlertDialogSamples.this)
        mProgressDialog setIcon R.drawable.alert_dialog_icon
        mProgressDialog setTitle R.string.select_dialog
        mProgressDialog setProgressStyle ProgressDialog.STYLE_HORIZONTAL
        mProgressDialog setMax MAX_PROGRESS
        mProgressDialog.setButton(getText(R.string.alert_dialog_hide),
          dummyListener)
        mProgressDialog.setButton2(getText(R.string.alert_dialog_cancel),
          dummyListener)
        mProgressDialog
      case DIALOG_SINGLE_CHOICE =>
        new AlertDialog.Builder(AlertDialogSamples.this)
          .setIcon(R.drawable.alert_dialog_icon)
          .setTitle(R.string.alert_dialog_single_choice)
          .setSingleChoiceItems(R.array.select_dialog_items2, 0, dummyListener)
          .setPositiveButton(R.string.alert_dialog_ok, dummyListener)
          .setNegativeButton(R.string.alert_dialog_cancel, dummyListener)
          .create()
      case DIALOG_MULTIPLE_CHOICE =>
        new AlertDialog.Builder(AlertDialogSamples.this)
          .setIcon(R.drawable.ic_popup_reminder)
          .setTitle(R.string.alert_dialog_multi_choice)
          .setMultiChoiceItems(R.array.select_dialog_items3,
            Array(false, true, false, true, false, false, false),
            new DialogInterface.OnMultiChoiceClickListener {
              def onClick(dialog: DialogInterface, whichButton: Int,
                                  isChecked: Boolean) {
              /* User clicked on a check box do some stuff */
              }
            })
          .setPositiveButton(R.string.alert_dialog_ok, dummyListener)
          .setNegativeButton(R.string.alert_dialog_cancel, dummyListener)
          .create()
      case DIALOG_TEXT_ENTRY =>
        // This example shows how to add a custom layout to an AlertDialog
        val factory = LayoutInflater.from(this)
        val textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null)
        new AlertDialog.Builder(AlertDialogSamples.this)
          .setIcon(R.drawable.alert_dialog_icon)
          .setTitle(R.string.alert_dialog_text_entry)
          .setView(textEntryView)
          .setPositiveButton(R.string.alert_dialog_ok, dummyListener)
          .setNegativeButton(R.string.alert_dialog_cancel, dummyListener)
          .create()
      case _ =>
        null
    }
  }

  /**
   * Initialization of the Activity after it is first created.  Must at least
   * call {@link android.app.Activity#setContentView(int)} to
   * describe what is to be displayed in the screen.
   */
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.alert_dialog)

    // Display a text message with yes/no buttons and handle each message as
    // well as the cancel action
    val twoButtonsTitle = findViewById(R.id.two_buttons).asInstanceOf[Button]
    twoButtonsTitle setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showDialog(DIALOG_YES_NO_MESSAGE)
      }
    }

    // Display a long text message with yes/no buttons and handle each
    // message as well as the cancel action
    val twoButtons2Title = findViewById(R.id.two_buttons2).asInstanceOf[Button]
    twoButtons2Title setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showDialog(DIALOG_YES_NO_LONG_MESSAGE)
      }
    } 

    /* Display a list of items */
    val selectButton = findViewById(R.id.select_button).asInstanceOf[Button]
    selectButton setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showDialog(DIALOG_LIST)
      }
    }

    /* Display a custom progress bar */
    val progressButton = findViewById(R.id.progress_button).asInstanceOf[Button]
    progressButton setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showDialog(DIALOG_PROGRESS)
        mProgress = 0
        mProgressDialog.setProgress(0)
        mProgressHandler.sendEmptyMessage(0)
      }
    }

    /* Display a radio button group */
    val radioButton = findViewById(R.id.radio_button).asInstanceOf[Button]
    radioButton setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showDialog(DIALOG_SINGLE_CHOICE)
      }
    }

    /* Display a list of checkboxes */
    val checkBox = findViewById(R.id.checkbox_button).asInstanceOf[Button]
    checkBox setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showDialog(DIALOG_MULTIPLE_CHOICE)
      }
    }

    /* Display a text entry dialog */
    val textEntry = findViewById(R.id.text_entry_button).asInstanceOf[Button]
    textEntry setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showDialog(DIALOG_TEXT_ENTRY)
      }
    }

    mProgressHandler = new Handler {
      override def handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (mProgress >= MAX_PROGRESS) {
          mProgressDialog.dismiss()
        } else {
          mProgress += 1
          mProgressDialog.incrementProgressBy(1)
          mProgressHandler.sendEmptyMessageDelayed(0, 100)
        }
      }
    }
  }
}
