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

import _root_.android.app.Activity
import _root_.android.app.Activity._
import _root_.android.content.Intent
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.view.View.OnClickListener
import _root_.android.widget.Button


/**
 * Example of receiving a result from another activity.
 */
class SendResult extends Activity {
  /**
   * Initialization of the Activity after it is first created.  Must at least
   * call {@link android.app.Activity#setContentView setContentView()} to
   * describe what is to be displayed in the screen.
   */
  override protected def onCreate(savedInstanceState: Bundle) {
    // Be sure to call the super class.
    super.onCreate(savedInstanceState)

    // See assets/res/any/layout/hello_world.xml for this
    // view layout definition, which is being set here as
    // the content of our screen.
    setContentView(R.layout.send_result)

    // Watch for button clicks.
    var button = findViewById(R.id.corky).asInstanceOf[Button]
    button setOnClickListener mCorkyListener
    button = findViewById(R.id.violet).asInstanceOf[Button]
    button setOnClickListener mVioletListener
  }

  private val mCorkyListener = new OnClickListener {
    def onClick(v: View) {
      // To send a result, simply call setResult() before your
      // activity is finished.
      setResult(RESULT_OK, (new Intent) setAction "Corky!")
      finish()
    }
  }

  private val mVioletListener = new OnClickListener {
    def onClick(v: View) {
      // To send a result, simply call setResult() before your
      // activity is finished.
      setResult(RESULT_OK, (new Intent) setAction "Violet!")
      finish()
    }
  }
}

