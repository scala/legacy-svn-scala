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

import com.example.android.apis.R;

import android.app.{Activity, DatePickerDialog, TimePickerDialog, Dialog}
import android.os.Bundle
import android.widget.{Button, DatePicker, TextView, TimePicker}
import android.view.View

import java.util.Calendar

/**
 * Basic example of using date and time widgets, including
 * {@link android.app.TimePickerDialog} and {@link android.widget.DatePicker}.
 *
 * Also provides a good example of using {@link Activity#onCreateDialog},
 * {@link Activity#onPrepareDialog} and {@link Activity#showDialog} to have the
 * activity automatically save and restore the state of the dialogs.
 */
class DateWidgets1 extends Activity {
  import DateWidgets1._  // companion object

  // where we display the selected date and time
  private var mDateDisplay: TextView = _

  // date and time
  private var mYear: Int = _
  private var mMonth: Int = _
  private var mDay: Int = _
  private var mHour: Int = _
  private var mMinute: Int = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.date_widgets_example_1)

    mDateDisplay = findViewById(R.id.dateDisplay).asInstanceOf[TextView]

    val pickDate = findViewById(R.id.pickDate).asInstanceOf[Button]
    pickDate.setOnClickListener(new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(DATE_DIALOG_ID)
      }
    })

    val pickTime = findViewById(R.id.pickTime).asInstanceOf[Button]
    pickTime.setOnClickListener(new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(TIME_DIALOG_ID)
      }
    })

    val c = Calendar.getInstance()
    mYear = c.get(Calendar.YEAR)
    mMonth = c.get(Calendar.MONTH)
    mDay = c.get(Calendar.DAY_OF_MONTH)
    mHour = c.get(Calendar.HOUR_OF_DAY)
    mMinute = c.get(Calendar.MINUTE)

    updateDisplay()
  }

  override protected def onCreateDialog(id: Int): Dialog =
    id match {
      case TIME_DIALOG_ID =>
        new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false)
      case DATE_DIALOG_ID =>
        new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay)
      case _ =>
        null
    }

  override protected def onPrepareDialog(id: Int, dialog: Dialog) {
    id match {
      case TIME_DIALOG_ID =>
        dialog.asInstanceOf[TimePickerDialog].updateTime(mHour, mMinute)
      case DATE_DIALOG_ID =>
        dialog.asInstanceOf[DatePickerDialog].updateDate(mYear, mMonth, mDay)
      case _ =>
    }
  }

  private def updateDisplay() {
    mDateDisplay setText 
      new StringBuilder()
        // Month is 0 based so add 1
        .append(mMonth + 1).append("-")
        .append(mDay).append("-")
        .append(mYear).append(" ")
        .append(pad(mHour)).append(":")
        .append(pad(mMinute))
  }

  private val mDateSetListener = new DatePickerDialog.OnDateSetListener() {
    def onDateSet(view: DatePicker, year: Int,
                  monthOfYear: Int, dayOfMonth: Int) {
      mYear = year
      mMonth = monthOfYear
      mDay = dayOfMonth
      updateDisplay();
    }
  }

  private val mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
    def onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
      mHour = hourOfDay
      mMinute = minute
      updateDisplay()
    }
  }

}

object DateWidgets1 {
  private final val TIME_DIALOG_ID = 0
  private final val DATE_DIALOG_ID = 1

  private def pad(c: Int): String =
    if (c >= 10) String.valueOf(c) else "0" + String.valueOf(c)

}

