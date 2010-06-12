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

import android.app.Activity
import android.content.Intent
import android.os.{Bundle, Process}
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

/**
 * Example of explicitly starting the {@link ServiceStartArguments}.
 */
class ServiceStartArgumentsController extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.service_start_arguments_controller)

    // Watch for button clicks.
    var button = findViewById(R.id.start1).asInstanceOf[Button]
    button setOnClickListener mStart1Listener
    button = findViewById(R.id.start2).asInstanceOf[Button]
    button setOnClickListener mStart2Listener
    button = findViewById(R.id.start3).asInstanceOf[Button]
    button setOnClickListener mStart3Listener
    button = findViewById(R.id.startfail).asInstanceOf[Button]
    button setOnClickListener mStartFailListener
    button = findViewById(R.id.kill).asInstanceOf[Button]
    button setOnClickListener mKillListener
  }

  private val mStart1Listener = new OnClickListener {
    def onClick(v: View) {
      startService(new Intent(ServiceStartArgumentsController.this,
                   classOf[ServiceStartArguments])
                     .putExtra("name", "One"))
    }
  }

  private val mStart2Listener = new OnClickListener {
    def onClick(v: View) {
      startService(new Intent(ServiceStartArgumentsController.this,
                   classOf[ServiceStartArguments])
                     .putExtra("name", "Two"))
    }
  }

  private val mStart3Listener = new OnClickListener {
    def onClick(v: View) {
      startService(new Intent(ServiceStartArgumentsController.this,
                   classOf[ServiceStartArguments])
                     .putExtra("name", "Three")
                     .putExtra("redeliver", true))
    }
  }

  private val mStartFailListener = new OnClickListener {
    def onClick(v: View) {
      startService(new Intent(ServiceStartArgumentsController.this,
                   classOf[ServiceStartArguments])
                     .putExtra("name", "Failure")
                     .putExtra("fail", true))
    }
  }

  private val mKillListener = new OnClickListener {
    def onClick(v: View) {
      // This is to simulate the service being killed while it is
      // running in the background.
      Process.killProcess(Process.myPid())
    }
  }
}
