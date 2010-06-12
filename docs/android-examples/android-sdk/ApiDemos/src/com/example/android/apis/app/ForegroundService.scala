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

import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.app.Service._
import android.content.Intent
import android.content.Context._
import android.os.IBinder
import android.util.Log

import java.lang.reflect.{InvocationTargetException, Method}

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * This is an example of implementing an application service that can
 * run in the "foreground".  It shows how to code this to work well by using
 * the improved Android 2.0 APIs when available and otherwise falling back
 * to the original APIs.  Yes: you can take this exact code, compile it
 * against the Android 2.0 SDK, and it will against everything down to
 * Android 1.0.
 */
object ForegroundService {
  final val ACTION_FOREGROUND = "com.example.android.apis.FOREGROUND"
  final val ACTION_BACKGROUND = "com.example.android.apis.BACKGROUND"
    
  private final val mStartForegroundSignature = Array(
    classOf[Int], classOf[Notification])
  private final val mStopForegroundSignature = Array(
    classOf[Boolean])
}

class ForegroundService extends Service {   
  import ForegroundService._  // companion object
 
  private var mNM: NotificationManager = _
  private var mStartForeground: Method = _
  private var mStopForeground: Method = _
  private val mStartForegroundArgs = new Array[Object](2)
  private val mStopForegroundArgs = new Array[Object](1)
    
  override def onCreate() {
    mNM = getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    try {
      mStartForeground =
        getClass.getMethod("startForeground", mStartForegroundSignature: _*)
      mStopForeground =
        getClass.getMethod("stopForeground", mStopForegroundSignature: _*)
    } catch { 
      case e: NoSuchMethodException =>
        // Running on an older platform.
        mStartForeground = null
        mStopForeground = null
    }
  }

  // This is the old onStart method that will be called on the pre-2.0
  // platform.  On 2.0 or later we override onStartCommand() so this
  // method will not be called.
  override def onStart(intent: Intent, startId: Int) {
    handleCommand(intent)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    handleCommand(intent)
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    START_STICKY
  }

  def handleCommand(intent: Intent) {
    if (ACTION_FOREGROUND equals intent.getAction) {
      // In this sample, we'll use the same text for the ticker and the
      // expanded notification
      val text = getText(R.string.foreground_service_started)

      // Set the icon, scrolling text and timestamp
      val notification =
        new Notification(R.drawable.stat_sample, text, System.currentTimeMillis)

      // The PendingIntent to launch our activity if the user selects this notification
      val contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, classOf[ForegroundServiceController]), 0)

      // Set the info for the views that show in the notification panel.
      notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                           text, contentIntent)
            
      startForegroundCompat(R.string.foreground_service_started, notification)
            
    } else if (ACTION_BACKGROUND equals intent.getAction) {
      stopForegroundCompat(R.string.foreground_service_started)
    }
  }
    
  /**
   * This is a wrapper around the new startForeground method, using the older
   * APIs if it is not available.
   */
  def startForegroundCompat(id: Int, notification: Notification) {
    // If we have the new startForeground API, then use it.
    if (mStartForeground != null) {
      mStartForegroundArgs(0) = java.lang.Integer.valueOf(id)
      mStartForegroundArgs(1) = notification
      try {
        mStartForeground.invoke(this, mStartForegroundArgs)
      } catch {
        case e: InvocationTargetException =>
          // Should not happen.
          Log.w("ApiDemos", "Unable to invoke startForeground", e)
        case e: IllegalAccessException =>
          // Should not happen.
          Log.w("ApiDemos", "Unable to invoke startForeground", e)
      }
    } else {
      // Fall back on the old API.
      setForeground(true)
      mNM.notify(id, notification)
    }
  }

  /**
   * This is a wrapper around the new stopForeground method, using the older
   * APIs if it is not available.
   */
  def stopForegroundCompat(id: Int) {
    // If we have the new stopForeground API, then use it.
    if (mStopForeground != null) {
      mStopForegroundArgs(0) = java.lang.Boolean.TRUE
      try {
        mStopForeground.invoke(this, mStopForegroundArgs)
      } catch {
        case e: InvocationTargetException =>
          // Should not happen.
          Log.w("ApiDemos", "Unable to invoke stopForeground", e)
        case e: IllegalAccessException =>
          // Should not happen.
          Log.w("ApiDemos", "Unable to invoke stopForeground", e)
      }
    } else {
      // Fall back on the old API.  Note to cancel BEFORE changing the
      // foreground state, since we could be killed at that point.
      mNM cancel id
      setForeground(false)
    }
  }

  override def onDestroy() {
    // Make sure our notification is gone.
    stopForegroundCompat(R.string.foreground_service_started)
  }

  override def onBind(intent: Intent): IBinder = {
    null
  }
}
