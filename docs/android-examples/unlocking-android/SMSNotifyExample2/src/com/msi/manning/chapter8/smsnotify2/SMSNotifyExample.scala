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

package com.msi.manning.chapter8.smsnotify2

import android.content.{BroadcastReceiver, Context, Intent}
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

class SMSNotifyExample extends BroadcastReceiver {
  import SMSNotifyExample._  // companion object

  def onReceiveIntent(context: Context, intent: Intent) {
    if (intent.getAction equals SMSNotifyExample.ACTION) {
      val sb = new StringBuilder()

      val bundle = intent.getExtras
      if (bundle != null) {

        val pdusObj = bundle.get("pdus").asInstanceOf[Array[AnyRef]]
        val messages = new Array[SmsMessage](pdusObj.length)

        for (currentMessage <- messages) {
          sb append "Received SMS\nFrom: "
          sb append currentMessage.getDisplayOriginatingAddress
          sb append "\n----Message----\n"
          sb append currentMessage.getDisplayMessageBody
        }
      }
      Log.i(SMSNotifyExample.LOG_TAG, "[SMSApp] onReceiveIntent: " + sb)
      Toast.makeText(context, sb.toString, Toast.LENGTH_LONG).show()
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
