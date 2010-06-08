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
import android.content.{ComponentName, Context}
import android.os.{Bundle, RemoteException}
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

/**
 * Front-end for launching {@link ContactsFilterInstrumentation} example
 * instrumentation class.
 */
class ContactsFilter extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.contacts_filter)

    // Watch for button clicks.
    val button = findViewById(R.id.go).asInstanceOf[Button]
    button.setOnClickListener(mGoListener)
  }

  private val mGoListener = new OnClickListener {
    def onClick(v: View) {
      startInstrumentation(new ComponentName(ContactsFilter.this,
                           classOf[ContactsFilterInstrumentation]), null, null)
    }
  }
}

