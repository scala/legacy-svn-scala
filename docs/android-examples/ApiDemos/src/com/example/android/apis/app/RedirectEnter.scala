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
import _root_.android.content.Intent
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.view.View.OnClickListener
import _root_.android.widget.Button


/**
 * Entry into our redirection example, describing what will happen.
 */
class RedirectEnter extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.redirect_enter)

    // Watch for button clicks.
    val goButton = findViewById(R.id.go).asInstanceOf[Button]
    goButton setOnClickListener mGoListener
  }

  private val mGoListener = new OnClickListener {
    def onClick(v: View) {
      // Here we start up the main entry point of our redirection
      // example.
      val intent = new Intent(RedirectEnter.this, classOf[RedirectMain])
      startActivity(intent)
    }
  }
}

