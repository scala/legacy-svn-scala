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
import android.app.Activity._
import android.content.Intent
import android.content.Context._
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, RemoteViews}

/**
 * Demonstrates adding notifications to the status bar
 */
object StatusBarNotifications {
  // Use our layout id for a unique identifier
  private val MOOD_NOTIFICATIONS = R.layout.status_bar_notifications
}

class StatusBarNotifications extends Activity {
  import StatusBarNotifications._  // companion object

  private var mNotificationManager: NotificationManager = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.status_bar_notifications)

    // Get the notification manager serivce.
    mNotificationManager =
      getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

    var button = findViewById(R.id.happy).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMood(R.drawable.stat_happy,
                R.string.status_bar_notifications_happy_message, false)
      }
    }

    button = findViewById(R.id.neutral).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMood(R.drawable.stat_neutral,
                R.string.status_bar_notifications_ok_message, false)
      }
    }

    button = findViewById(R.id.sad).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMood(R.drawable.stat_sad,
                R.string.status_bar_notifications_sad_message, false)
      }
    }

    button = findViewById(R.id.happyMarquee).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMood(R.drawable.stat_happy,
                R.string.status_bar_notifications_happy_message, true)
      }
    }

    button = findViewById(R.id.neutralMarquee).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMood(R.drawable.stat_neutral,
                R.string.status_bar_notifications_ok_message, true)
      }
    }

    button = findViewById(R.id.sadMarquee).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMood(R.drawable.stat_sad,
                R.string.status_bar_notifications_sad_message, true);
      }
    }

    button = findViewById(R.id.happyViews).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMoodView(R.drawable.stat_happy,
                    R.string.status_bar_notifications_happy_message)
      }
    }

    button = findViewById(R.id.neutralViews).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMoodView(R.drawable.stat_neutral,
                    R.string.status_bar_notifications_ok_message)
      }
    }

    button = findViewById(R.id.sadViews).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setMoodView(R.drawable.stat_sad,
                    R.string.status_bar_notifications_sad_message)
      }
    }

    button = findViewById(R.id.defaultSound).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setDefault(Notification.DEFAULT_SOUND)
      }
    }
     
    button = findViewById(R.id.defaultVibrate).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setDefault(Notification.DEFAULT_VIBRATE)
      }
    }

    button = findViewById(R.id.defaultAll).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        setDefault(Notification.DEFAULT_ALL)
      }
    }

    button = findViewById(R.id.clear).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        mNotificationManager.cancel(R.layout.status_bar_notifications)
      }
    }
  }

  private def makeMoodIntent(moodId: Int): PendingIntent = {
    // The PendingIntent to launch our activity if the user selects this
    // notification.  Note the use of FLAG_UPDATE_CURRENT so that if there
    // is already an active matching pending intent, we will update its
    // extras to be the ones passed in here.
    val contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, classOf[NotificationDisplay])
                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  .putExtra("moodimg", moodId),
                PendingIntent.FLAG_UPDATE_CURRENT)
    contentIntent
  }
    
  private def setMood(moodId: Int, textId: Int, showTicker: Boolean) {
    // In this sample, we'll use the same text for the ticker and the
    // expanded notification
    val text = getText(textId)

    // choose the ticker text
    val tickerText = if (showTicker) getString(textId) else null

    // Set the icon, scrolling text and timestamp
    val notification = new Notification(moodId, tickerText,
                                        System.currentTimeMillis)

    // Set the info for the views that show in the notification panel.
    notification.setLatestEventInfo(this,
      getText(R.string.status_bar_notifications_mood_title),
      text, makeMoodIntent(moodId))

    // Send the notification.
    // We use a layout id because it is a unique number.  We use it later to cancel.
    mNotificationManager.notify(R.layout.status_bar_notifications, notification)
  }

  private def setMoodView(moodId: Int, textId: Int) {
    // Instead of the normal constructor, we're going to use the one with
    // no args and fill in all of the data ourselves.  The normal one uses
    // the default layout for notifications.
    // You probably want that in most cases, but if you want to do something
    // custom, you can set the contentView field to your own RemoteViews object.
    val notif = new Notification

    // This is who should be launched if the user selects our notification.
    notif.contentIntent = makeMoodIntent(moodId)

    // In this sample, we'll use the same text for the ticker and the expanded
    // notification
    val text = getText(textId)
    notif.tickerText = text

    // the icon for the status bar
    notif.icon = moodId

    // our custom view
    val contentView = new RemoteViews(getPackageName, R.layout.status_bar_balloon)
    contentView.setTextViewText(R.id.text, text)
    contentView.setImageViewResource(R.id.icon, moodId)
    notif.contentView = contentView

    // we use a string id because is a unique number.  we use it later to cancel the
    // notification
    mNotificationManager.notify(R.layout.status_bar_notifications, notif)
  }
    
  private def setDefault(defaults: Int) {
    // This method sets the defaults on the notification before posting it.
        
    // This is who should be launched if the user selects our notification.
    val contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, classOf[StatusBarNotifications]), 0)

    // In this sample, we'll use the same text for the ticker and the expanded
    // notification
    val text = getText(R.string.status_bar_notifications_happy_message)

    val notification = new Notification(
              R.drawable.stat_happy,    // the icon for the status bar
              text,                     // the text to display in the ticker
              System.currentTimeMillis) // the timestamp for the notification

    notification.setLatestEventInfo(
              this,                     // the context to use
              getText(R.string.status_bar_notifications_mood_title),
                                        // the title for the notification
              text,                     // the details to display in the notification
              contentIntent)            // the contentIntent (see above)

    notification.defaults = defaults
        
    mNotificationManager.notify(
              R.layout.status_bar_notifications, // we use a string id because it is a unique
                                        // number.  we use it later to cancel the
              notification)             // notification
  }    
}
