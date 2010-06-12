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

package com.example.android.apis.text

import com.example.android.apis.R

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.{Html, SpannableString, Spanned}
import android.text.method.LinkMovementMethod
import android.text.style.{StyleSpan, URLSpan}
import android.widget.TextView

class Link extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.link)

    // text1 shows the android:autoLink property, which
    // automatically linkifies things like URLs and phone numbers
    // found in the text.  No java code is needed to make this
    // work.

    // text2 has links specified by putting <a> tags in the string
    // resource.  By default these links will appear but not
    // respond to user input.  To make them active, you need to
    // call setMovementMethod() on the TextView object.

    val t2 = findViewById(R.id.text2).asInstanceOf[TextView]
    t2 setMovementMethod LinkMovementMethod.getInstance

    // text3 shows creating text with links from HTML in the Java
    // code, rather than from a string resource.  Note that for a
    // fixed string, using a (localizable) resource as shown above
    // is usually a better way to go; this example is intended to
    // illustrate how you might display text that came from a
    // dynamic source (eg, the network).

    val t3 = findViewById(R.id.text3).asInstanceOf[TextView]
    t3 setText Html.fromHtml(
      "<b>text3:</b>  Text with a " +
      "<a href=\"http://www.google.com\">link</a> " +
      "created in the Java source code using HTML.")
    t3 setMovementMethod LinkMovementMethod.getInstance

    // text4 illustrates constructing a styled string containing a
    // link without using HTML at all.  Again, for a fixed string
    // you should probably be using a string resource, not a
    // hardcoded value.

    val ss = new SpannableString("text4: Click here to dial the phone.")

    ss.setSpan(new StyleSpan(Typeface.BOLD), 0, 6,
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    ss.setSpan(new URLSpan("tel:4155551212"), 13, 17,
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    val t4 = findViewById(R.id.text4).asInstanceOf[TextView]
    t4 setText ss
    t4 setMovementMethod LinkMovementMethod.getInstance
  }
}
