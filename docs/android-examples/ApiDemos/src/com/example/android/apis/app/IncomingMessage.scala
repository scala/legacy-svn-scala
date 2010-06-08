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

import com.example.android.apis.R

import android.app.{Activity, Notification, NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.content.Context._
import android.os.Bundle
import android.view.{LayoutInflater, View}
import android.view.View.OnClickListener
import android.widget.{Button, TextView, Toast}

class IncomingMessage extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.incoming_message)

    val button = findViewById(R.id.notify).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        showToast()
        showNotification()
      }
    }
  }

  /**
   * The toast pops up a quick message to the user showing what could be
   * the text of an incoming message.  It uses a custom view to do so.
   */
  protected def showToast() {
    // create the view
    val view = inflateView(R.layout.incoming_message_panel)

    // set the text in the view
    val tv = view.findViewById(R.id.message).asInstanceOf[TextView]
    tv setText "khtx. meet u for dinner. cul8r"

    // show the toast
    val toast = new Toast(this)
    toast setView view
    toast setDuration Toast.LENGTH_LONG
    toast.show()
  }

  private def inflateView(resource: Int): View = {
    val vi = getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    vi.inflate(resource, null)
  }

  /**
   * The notification is the icon and associated expanded entry in the
   * status bar.
   */
  protected def showNotification() {
    // look up the notification manager service
    val nm = getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

    // The details of our fake message
    val from = "Joe"
    val message = "kthx. meet u for dinner. cul8r"

    // The PendingIntent to launch our activity if the user selects this notification
    val contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, classOf[IncomingMessageView]), 0)

    // The ticker text, this uses a formatted string so our message could be localized
    val tickerText = getString(R.string.imcoming_message_ticker_text, message)

    // construct the Notification object.
    val notif = new Notification(R.drawable.stat_sample, tickerText,
                                 System.currentTimeMillis)

    // Set the info for the views that show in the notification panel.
    notif.setLatestEventInfo(this, from, message, contentIntent)

    // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
    // then vibrate for 500ms.
    notif.vibrate = Array[Long](100, 250, 100, 500)

    // Note that we use R.layout.incoming_message_panel as the ID for
    // the notification.  It could be any integer you want, but we use
    // the convention of using a resource id for a string related to
    // the notification.  It will always be a unique number within your
    // application.
    nm.notify(R.string.imcoming_message_ticker_text, notif)
  }
}
