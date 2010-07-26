/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.android.photostream

import android.app.{AlarmManager, Service, NotificationManager,
                    Notification, PendingIntent}
import android.os.{IBinder, SystemClock}
import android.content.{Context, ContentValues, Intent, SharedPreferences}
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils

import java.util.{Calendar, GregorianCalendar, TimeZone}

/**
 * CheckUpdateService checks every 24 hours if updates have been made to the
 * photostreams of the current contacts. This service simply polls an RSS feed
 * and compares the modification timestamp with the one stored in the database.
 */
class CheckUpdateService extends Service {
  import CheckUpdateService._  // companion object

  private var mTask: CheckForUpdatesTask = _

  override def onStart(intent: Intent, startId: Int) {
    super.onStart(intent, startId)
    mTask = new CheckForUpdatesTask()
    mTask.execute()
  }

  override def onDestroy() {
    super.onDestroy()
    if (mTask != null && mTask.getStatus == UserTask.Status.RUNNING) {
      mTask cancel true
    }
  }

  def onBind(intent: Intent): IBinder = null

  private class CheckForUpdatesTask extends UserTask[Nothing, AnyRef, Nothing] {
    private var mPreferences: SharedPreferences = _
    private var mManager: NotificationManager = _

    override def onPreExecute() {
      mPreferences = getSharedPreferences(Preferences.NAME, Context.MODE_PRIVATE)
      mManager = getSystemService(Context.NOTIFICATION_SERVICE)
        .asInstanceOf[NotificationManager]
    }

    def doInBackground(params: Nothing*): Nothing = {
      val helper = new UserDatabase(CheckUpdateService.this)
      val database = helper.getWritableDatabase

      var cursor: Cursor = null
      try {
        cursor = database.query(UserDatabase.TABLE_USERS,
                        Array(UserDatabase._ID, UserDatabase.COLUMN_NSID,
                        UserDatabase.COLUMN_REALNAME, UserDatabase.COLUMN_LAST_UPDATE),
                        null, null, null, null, null)

        val idIndex = cursor getColumnIndexOrThrow UserDatabase._ID
        val realNameIndex = cursor getColumnIndexOrThrow UserDatabase.COLUMN_REALNAME
        val nsidIndex = cursor getColumnIndexOrThrow UserDatabase.COLUMN_NSID
        val lastUpdateIndex = cursor getColumnIndexOrThrow UserDatabase.COLUMN_LAST_UPDATE

        val flickr = Flickr.get

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        val reference = Calendar.getInstance

        while (!isCancelled && cursor.moveToNext()) {
          val nsid = cursor getString nsidIndex
          calendar setTimeInMillis cursor.getLong(lastUpdateIndex)

          reference.set(calendar get Calendar.YEAR,
                        calendar get Calendar.MONTH,
                        calendar get Calendar.DAY_OF_MONTH,
                        calendar get Calendar.HOUR_OF_DAY,
                        calendar get Calendar.MINUTE,
                        calendar get Calendar.SECOND)

          if (flickr.hasUpdates(Flickr.User.fromId(nsid), reference)) {
             publishProgress(nsid, cursor getString realNameIndex,
                                   cursor.getInt(idIndex).asInstanceOf[AnyRef])
          }
        }

        val values = new ContentValues()
        values.put(UserDatabase.COLUMN_LAST_UPDATE, System.currentTimeMillis.toDouble)

        database.update(UserDatabase.TABLE_USERS, values, null, null)
      } finally {
        if (cursor != null) cursor.close()
        database.close()
      }

      null.asInstanceOf[Nothing]
    }

    override def onProgressUpdate(values: AnyRef*) {
      if (mPreferences.getBoolean(Preferences.KEY_ENABLE_NOTIFICATIONS, true)) {
        val id = values(2).toString.toInt
        val intent = new Intent(PhotostreamActivity.ACTION)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(PhotostreamActivity.EXTRA_NOTIFICATION, id)
        intent.putExtra(PhotostreamActivity.EXTRA_NSID, values(0).toString)

        val notification = new Notification(R.drawable.stat_notify,
                        getString(R.string.notification_new_photos, values(1)),
                        System.currentTimeMillis)
        notification.setLatestEventInfo(CheckUpdateService.this,
                        getString(R.string.notification_title),
                        getString(R.string.notification_contact_has_new_photos, values(1)),
                        PendingIntent.getActivity(CheckUpdateService.this, 0, intent,
                                PendingIntent.FLAG_CANCEL_CURRENT))

        if (mPreferences.getBoolean(Preferences.KEY_VIBRATE, false)) {
          notification.defaults |= Notification.DEFAULT_VIBRATE
        }

        val ringtoneUri = mPreferences.getString(Preferences.KEY_RINGTONE, null)
        notification.sound =
          if (TextUtils.isEmpty(ringtoneUri)) null else Uri.parse(ringtoneUri)

        mManager.notify(id, notification)
      }
    }

    override def onPostExecute(aVoid: Nothing) {
      stopSelf()
    }
  }
}

object CheckUpdateService {

  private var DEBUG = false

  // Check interval: every 24 hours
  private val UPDATES_CHECK_INTERVAL = 24 * 60 * 60 * 1000

  def schedule(context: Context) {
    val intent = new Intent(context, classOf[CheckUpdateService])
    val pending = PendingIntent.getService(context, 0, intent, 0)

    val c = new GregorianCalendar()
    c.add(Calendar.DAY_OF_YEAR, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)

    val alarm = context
      .getSystemService(Context.ALARM_SERVICE)
      .asInstanceOf[AlarmManager]
    alarm cancel pending
    if (DEBUG) {
      alarm.setRepeating(AlarmManager.ELAPSED_REALTIME,
                         SystemClock.elapsedRealtime, 30 * 1000, pending)
    } else {
      alarm.setRepeating(AlarmManager.RTC, c.getTimeInMillis,
                         UPDATES_CHECK_INTERVAL, pending)
    }
  }

}
