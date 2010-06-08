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
import android.os.{Binder, IBinder, Parcel, RemoteException}
import android.widget.Toast

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an
 * intent receiver.
 * 
 * @see AlarmService
 * @see AlarmService_Alarm
 */
class AlarmService_Service extends Service {
  private var mNM: NotificationManager = _

  override def onCreate() {
    mNM = getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

    // show the icon in the status bar
    showNotification()

    // Start up the thread running the service.  Note that we create a
    // separate thread because the service normally runs in the process's
    // main thread, which we don't want to block.
    val thr = new Thread(null, mTask, "AlarmService_Service")
    thr.start()
  }

  override def onDestroy() {
    // Cancel the notification -- we use the same ID that we had used to start it
    mNM cancel R.string.alarm_service_started

     // Tell the user we stopped.
     Toast.makeText(this, R.string.alarm_service_finished,
                    Toast.LENGTH_SHORT).show()
  }

  /**
   * The function that runs in our worker thread
   */
  val mTask = new Runnable {
    def run() {
      // Normally we would do some work here...  for our sample, we will
      // just sleep for 30 seconds.
      val endTime = System.currentTimeMillis + 15*1000
      while (System.currentTimeMillis < endTime) {
        mBinder synchronized {
          try {
            mBinder.wait(endTime - System.currentTimeMillis());
          } catch {
            case e: Exception =>
          }
        }
      }

      // Done with our work...  stop the service!
      AlarmService_Service.this.stopSelf()
    }
  }

  override def onBind(intent: Intent): IBinder = {
    mBinder
  }

  /**
   * Show a notification while this service is running.
   */
  private def showNotification() {
    // In this sample, we'll use the same text for the ticker and the
    // expanded notification
    val text = getText(R.string.alarm_service_started)

    // Set the icon, scrolling text and timestamp
    val notification = new Notification(R.drawable.stat_sample, text,
                                        System.currentTimeMillis)

    // The PendingIntent to launch our activity if the user selects
    // this notification
    val contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, classOf[AlarmService]), 0)

    // Set the info for the views that show in the notification panel.
    notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
                       text, contentIntent)

    // Send the notification.
    // We use a layout id because it is a unique number.  We use it later
    // to cancel.
    mNM.notify(R.string.alarm_service_started, notification)
  }

  /**
   * This is the object that receives interactions from clients. 
   * See RemoteService for a more complete example.
   */
  private final val mBinder: IBinder = new Binder {
    @throws(classOf[RemoteException])
    override protected def onTransact(code: Int, data: Parcel,
                                      reply: Parcel, flags: Int): Boolean = {
      super.onTransact(code, data, reply, flags)
    }
  }
}

