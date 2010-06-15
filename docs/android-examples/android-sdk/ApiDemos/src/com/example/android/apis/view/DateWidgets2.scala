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

package com.example.android.apis.view

import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.widget.{TextView, TimePicker}

class DateWidgets2 extends Activity {
  import DateWidgets2._  // companion object

  // where we display the selected date and time
  private var mTimeDisplay: TextView = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.date_widgets_example_2)

    val timePicker = findViewById(R.id.timePicker).asInstanceOf[TimePicker]
    timePicker setCurrentHour 12
    timePicker setCurrentMinute 15

    mTimeDisplay = findViewById(R.id.dateDisplay).asInstanceOf[TextView]

    updateDisplay(12, 15)

    timePicker setOnTimeChangedListener new TimePicker.OnTimeChangedListener() {
      def onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) {
        updateDisplay(hourOfDay, minute)
      }
    }
  }

  private def updateDisplay(hourOfDay: Int, minute: Int) {
    mTimeDisplay.setText(new StringBuilder()
      .append(pad(hourOfDay)).append(":").append(pad(minute)))
  }

}

object DateWidgets2 {

  private final def pad(c: Int): String =
    (if (c >= 10) "" else "0") + String.valueOf(c)

}
