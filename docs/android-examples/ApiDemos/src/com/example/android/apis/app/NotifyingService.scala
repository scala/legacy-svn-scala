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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.content.Intent
import android.content.Context._
import android.os.{Binder, ConditionVariable, IBinder, Parcel, RemoteException}
import android.widget.RemoteViews

/**
 * This is an example of service that will update its status bar balloon 
 * every 5 seconds for a minute.
 * 
 */
object NotifyingService {
  // Use a layout id for a unique identifier
  private final val MOOD_NOTIFICATIONS = R.layout.status_bar_notifications
}

class NotifyingService extends Service {
  import NotifyingService._  // companion object

  // variable which controls the notification thread 
  private var mCondition: ConditionVariable = _
 
  override def onCreate() {
    mNM = getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

    // Start up the thread running the service.  Note that we create a
    // separate thread because the service normally runs in the process's
    // main thread, which we don't want to block.
    val notifyingThread = new Thread(null, mTask, "NotifyingService")
    mCondition = new ConditionVariable(false)
    notifyingThread.start()
  }

  override def onDestroy() {
    // Cancel the persistent notification.
    mNM cancel MOOD_NOTIFICATIONS
    // Stop the thread from generating further notifications
    mCondition.open()
  }

  private val mTask = new Runnable() {
    def run() {
      var stop = false
      for (i <- 0 until 4 if !stop) {
        showNotification(R.drawable.stat_happy,
                         R.string.status_bar_notifications_happy_message)
        stop = mCondition.block(5 * 1000)
        if (!stop)
          showNotification(R.drawable.stat_neutral,
                           R.string.status_bar_notifications_ok_message)
        stop = mCondition.block(5 * 1000)
        if (!stop)
          showNotification(R.drawable.stat_sad,
                           R.string.status_bar_notifications_sad_message)
        stop = mCondition.block(5 * 1000)
        if (!stop)
          // Done with our work...  stop the service!
          NotifyingService.this.stopSelf()
      }
    }
  }

  override def onBind(intent: Intent): IBinder = mBinder
    
  private def showNotification(moodId: Int, textId: Int) {
    // In this sample, we'll use the same text for the ticker and the expanded notification
    val text = getText(textId)

    // Set the icon, scrolling text and timestamp.
    // Note that in this example, we pass null for tickerText.  We update the
    // icon enough that it is distracting to show the ticker text every time it
    // changes.  We strongly suggest that you do this as well.  (Think of of
    // the "New hardware found" or "Network connection changed" messages that
    // always pop up)
    val notification = new Notification(moodId, null, System.currentTimeMillis)

    // The PendingIntent to launch our activity if the user selects this notification
    val contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, classOf[NotifyingController]), 0)

    // Set the info for the views that show in the notification panel.
    notification.setLatestEventInfo(this,
      getText(R.string.status_bar_notifications_mood_title),
      text, contentIntent)

    // Send the notification.
    // We use a layout id because it is a unique number.  We use it later to cancel.
    mNM.notify(MOOD_NOTIFICATIONS, notification)
  }

  // This is the object that receives interactions from clients.  See
  // RemoteService for a more complete example.
  private final val mBinder = new Binder() {
    @throws(classOf[RemoteException])
    override protected def onTransact(code: Int, data: Parcel, reply: Parcel,
                                      flags: Int): Boolean = {
      super.onTransact(code, data, reply, flags)
    }
  }

  private var mNM: NotificationManager = _
}
