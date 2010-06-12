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
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

class RemoteServiceController extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.remote_service_controller);

    // Watch for button clicks.
    var button = findViewById(R.id.start).asInstanceOf[Button]
    button setOnClickListener mStartListener
    button = findViewById(R.id.stop).asInstanceOf[Button]
    button setOnClickListener mStopListener
  }

  private val mStartListener = new OnClickListener() {
    def onClick(v: View) {
      // Make sure the service is started.  It will continue running
      // until someone calls stopService().
      // We use an action code here, instead of explictly supplying
      // the component name, so that other packages can replace the service.
      startService(new Intent(
                    "com.example.android.apis.app.REMOTE_SERVICE"))
    }
  }

  private val mStopListener = new OnClickListener() {
    def onClick(v: View) {
      // Cancel a previous call to startService().  Note that the
      // service will not actually stop at this point if there are
      // still bound clients.
      stopService(new Intent(
                    "com.example.android.apis.app.REMOTE_SERVICE"))
    }
  }
}
