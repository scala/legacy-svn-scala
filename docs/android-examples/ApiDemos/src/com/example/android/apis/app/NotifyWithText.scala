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

import com.example.android.apis.R

import _root_.android.app.Activity
import _root_.android.widget.Button
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.view.View.OnClickListener
import _root_.android.widget.Toast

/**
 * When you push the button on this Activity, it creates a {@link Toast} object and
 * using the Toast method.
 * @see Toast
 * @see Toast#makeText(android.content.Context,int,int)
 * @see Toast#makeText(android.content.Context,java.lang.CharSequence,int)
 * @see Toast#LENGTH_SHORT
 * @see Toast#LENGTH_LONG
 */
class NotifyWithText extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.notify_with_text)

    // short notification
    var button = findViewById(R.id.short_notify).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        // Note that we create the Toast object and call the show() method
        // on it all on one line.  Most uses look like this, but there
        // are other methods on Toast that you can call to configure how
        // it appears.
        //
        // Note also that we use the version of makeText that takes a
        // resource id (R.string.short_notification_text).  There is also
        // a version that takes a CharSequence if you must construct
        // the text yourself.
        Toast.makeText(NotifyWithText.this, R.string.short_notification_text,
                       Toast.LENGTH_SHORT).show()
      }
    }

    // long notification
    // The only difference here is that the notification stays up longer.
    // You might want to use this if there is more text that they're going
    // to read.
    button = findViewById(R.id.long_notify).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        Toast.makeText(NotifyWithText.this, R.string.long_notification_text,
                       Toast.LENGTH_LONG).show()
      }
    }

  }
}
