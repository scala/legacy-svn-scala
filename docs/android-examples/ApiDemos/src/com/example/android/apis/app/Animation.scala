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
import com.example.android.apis.view.Controls1

import android.app.Activity
import android.content.{ComponentName, Intent}
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

/**
 * <p>Example of explicitly starting and stopping the {@link LocalService}.
 * This demonstrates the implementation of a service that runs in the same
 * process as the rest of the application, which is explicitly started and stopped
 * as desired.</p>
 */
class Animation extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_animation)

    // Watch for button clicks.
    var button = findViewById(R.id.fade_animation).asInstanceOf[Button]
    button setOnClickListener mFadeListener
    button = findViewById(R.id.zoom_animation).asInstanceOf[Button]
    button setOnClickListener mZoomListener
  }

  private val mFadeListener = new OnClickListener {
    def onClick(v: View) {
      // Request the next activity transition (here starting a new one).
      startActivity(new Intent(Animation.this, classOf[Controls1]))
      // Supply a custom animation.  This one will just fade the new
      // activity on top.  Note that we need to also supply an animation
      // (here just doing nothing for the same amount of time) for the
      // old activity to prevent it from going away too soon.
      overridePendingTransition(R.anim.fade, R.anim.hold)
    }
  }

  private val mZoomListener = new OnClickListener {
    def onClick(v: View) {
      // Request the next activity transition (here starting a new one).
      startActivity(new Intent(Animation.this, classOf[Controls1]))
      // This is a more complicated animation, involving transformations
      // on both this (exit) and the new (enter) activity.  Note how for
      // the duration of the animation we force the exiting activity
      // to be Z-ordered on top (even though it really isn't) to achieve
      // the effect we want.
      overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
    }
  }
}

