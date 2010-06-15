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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.os.{Bundle, SystemClock}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, Chronometer}

class ChronometerDemo extends Activity {
  private var mChronometer: Chronometer = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.chronometer)

    mChronometer = findViewById(R.id.chronometer).asInstanceOf[Chronometer]

    // Watch for button clicks.
    var button = findViewById(R.id.start).asInstanceOf[Button]
    button setOnClickListener mStartListener

    button = findViewById(R.id.stop).asInstanceOf[Button]
    button setOnClickListener mStopListener

    button = findViewById(R.id.reset).asInstanceOf[Button]
    button setOnClickListener mResetListener

    button = findViewById(R.id.set_format).asInstanceOf[Button]
    button setOnClickListener mSetFormatListener

    button = findViewById(R.id.clear_format).asInstanceOf[Button]
    button setOnClickListener mClearFormatListener
  }

  private val mStartListener = new OnClickListener() {
    def onClick(v: View) {
      mChronometer.start()
    }
  }

  private val mStopListener = new OnClickListener() {
    def onClick(v: View) {
      mChronometer.stop()
    }
  }

  private val mResetListener = new OnClickListener() {
    def onClick(v: View) {
      mChronometer.setBase(SystemClock.elapsedRealtime)
    }
  }

  private val mSetFormatListener = new OnClickListener() {
    def onClick(v: View) {
      mChronometer setFormat "Formatted time (%s)"
    }
  }

  private val mClearFormatListener = new OnClickListener() {
    def onClick(v: View) {
      mChronometer setFormat null
    }
  }
}
