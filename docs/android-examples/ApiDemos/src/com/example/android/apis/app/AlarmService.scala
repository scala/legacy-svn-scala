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

import android.app.{Activity, AlarmManager, PendingIntent}
import android.content.Intent
import android.content.Context._
import android.os.{Bundle, SystemClock}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, Toast}


/**
 * This demonstrates how you can schedule an alarm that causes a service to
 * be started.  This is useful when you want to schedule alarms that initiate
 * long-running operations, such as retrieving recent e-mails.
 */
class AlarmService extends Activity {
  private var mAlarmSender: PendingIntent = _
    
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Create an IntentSender that will launch our service, to be scheduled
    // with the alarm manager.
    val intent = new Intent(AlarmService.this, classOf[AlarmService_Service])
    mAlarmSender = PendingIntent.getService(AlarmService.this, 0, intent, 0)
        
    setContentView(R.layout.alarm_service)

    // Watch for button clicks.
    var button = findViewById(R.id.start_alarm).asInstanceOf[Button]
    button setOnClickListener mStartAlarmListener
    button = findViewById(R.id.stop_alarm).asInstanceOf[Button]
    button setOnClickListener mStopAlarmListener
  }

  private val mStartAlarmListener = new OnClickListener {
    def onClick(v: View) {
      // We want the alarm to go off 30 seconds from now.
      val firstTime = SystemClock.elapsedRealtime

      // Schedule the alarm!
      val am = getSystemService(ALARM_SERVICE).asInstanceOf[AlarmManager]
      am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                      firstTime, 30*1000, mAlarmSender)

      // Tell the user about what we did.
      Toast.makeText(AlarmService.this, R.string.repeating_scheduled,
                  Toast.LENGTH_LONG).show()
    }
  }

  private val mStopAlarmListener = new OnClickListener {
    def onClick(v: View) {
      // And cancel the alarm.
      val am = getSystemService(ALARM_SERVICE).asInstanceOf[AlarmManager]
      am cancel mAlarmSender

      // Tell the user about what we did.
      Toast.makeText(AlarmService.this, R.string.repeating_unscheduled,
                     Toast.LENGTH_LONG).show()

    }
  }
}
