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

package com.msi.manning.chapter8.smsnotify

import android.app.{Notification, NotificationManager, PendingIntent}
import android.content.{BroadcastReceiver, Context, Intent}
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SMSNotifyExample extends BroadcastReceiver {
  import SMSNotifyExample._  // companion object

  private var tickerMessage: CharSequence = _

  def onReceiveIntent(context: Context, intent: Intent) {
    var nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
               .asInstanceOf[NotificationManager]
    if (intent.getAction equals SMSNotifyExample.ACTION) {
      val sb = new StringBuilder()
      val bundle = intent.getExtras
      if (bundle != null) {
        val pdusObj = bundle.get("pdus").asInstanceOf[Array[AnyRef]]
        val messages = new Array[SmsMessage](pdusObj.length)

        for (currentMessage <- messages) {
          sb append "Received compressed SMS\nFrom: "
          sb append currentMessage.getDisplayOriginatingAddress
          currentMessage.getDisplayOriginatingAddress
          sb append "\n----Message----\n"
          sb append currentMessage.getDisplayMessageBody
        }
      }

      Log.i(SMSNotifyExample.LOG_TAG, "[SMSApp] onReceiveIntent: " + sb)
      abortBroadcast()

      val i = new Intent(context, classOf[SMSNotifyActivity])
      context.startActivity(i)

      val appName = "SMSNotifyExample"
      tickerMessage = sb.toString
      val theWhen = System.currentTimeMillis

      PendingIntent.getBroadcast(appName.asInstanceOf[Context], 0, i, 0)
      val notif = new Notification(R.drawable.incoming, tickerMessage, theWhen)

      notif.vibrate = Array[Long](100, 250, 100, 500)
      nm.notify(R.string.alert_message, notif)
    }
  }

  override def onReceive(context: Context, intent: Intent) {
  }
}

object SMSNotifyExample {
  private final val LOG_TAG = "SMSReceiver"
  final val NOTIFICATION_ID_RECEIVED = 0x1221
  final val ACTION = "android.provider.Telephony.SMS_RECEIVED"
}
