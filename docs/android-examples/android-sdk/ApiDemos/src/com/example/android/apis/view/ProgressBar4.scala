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
import android.view.{View, Window}
import android.widget.Button

/**
 * Demonstrates how to use an indeterminate progress indicator in the
 * window's title bar.
 */
class ProgressBar4 extends Activity {
  private var mToggleIndeterminate = false

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Request progress bar
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    setContentView(R.layout.progressbar_4)
    setProgressBarIndeterminateVisibility(mToggleIndeterminate)
        
    val button = findViewById(R.id.toggle).asInstanceOf[Button]
    button setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        mToggleIndeterminate = !mToggleIndeterminate
        setProgressBarIndeterminateVisibility(mToggleIndeterminate)
      }
    }
  }
}
