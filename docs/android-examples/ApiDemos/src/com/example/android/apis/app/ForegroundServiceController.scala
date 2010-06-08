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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.content.{ComponentName, Intent}
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

/**
 * <p>Example of explicitly starting and stopping the {@link ForegroundService}.
 */
class ForegroundServiceController extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.foreground_service_controller)

    // Watch for button clicks.
    var button = findViewById(R.id.start_foreground).asInstanceOf[Button]
    button setOnClickListener mForegroundListener
    button = findViewById(R.id.start_background).asInstanceOf[Button]
    button setOnClickListener mBackgroundListener
    button = findViewById(R.id.stop).asInstanceOf[Button]
    button setOnClickListener mStopListener
  }

  private val mForegroundListener = new OnClickListener {
    def onClick(v: View) {
      val intent = new Intent(ForegroundService.ACTION_FOREGROUND)
      intent.setClass(ForegroundServiceController.this, classOf[ForegroundService])
      startService(intent)
    }
  }

  private val mBackgroundListener = new OnClickListener {
    def onClick(v: View) {
      val intent = new Intent(ForegroundService.ACTION_BACKGROUND)
      intent.setClass(ForegroundServiceController.this, classOf[ForegroundService])

      startService(intent);
    }
  }

  private val mStopListener = new OnClickListener {
    def onClick(v: View) {
      stopService(new Intent(ForegroundServiceController.this,
                  classOf[ForegroundService]))
    }
  }
}

