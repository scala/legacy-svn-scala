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

package com.msi.manning.chapter8.simplealarm

import java.util.Calendar

import android.app.{Activity, AlarmManager,PendingIntent}
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, Toast}

class GenerateAlarm extends Activity {

  private var mToast: Toast = _

  override protected def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)
    val button = findViewById(R.id.set_alarm_button).asInstanceOf[Button]
    button setOnClickListener mOneShotListener
  }

  private val mOneShotListener = new OnClickListener() {
    def onClick(v: View) {
      val intent = new Intent(GenerateAlarm.this, classOf[AlarmReceiver])
      val appIntent = PendingIntent.getBroadcast(GenerateAlarm.this, 0, intent, 0)

      val calendar = Calendar.getInstance
      calendar.setTimeInMillis(System.currentTimeMillis)
      calendar.add(Calendar.SECOND, 30)

      val am = getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
      am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis, appIntent)

      if (mToast != null) {
        mToast.cancel()
      }
      mToast = Toast.makeText(GenerateAlarm.this, R.string.alarm_message,
                              Toast.LENGTH_LONG)
      mToast.show()
    }
  }

}
