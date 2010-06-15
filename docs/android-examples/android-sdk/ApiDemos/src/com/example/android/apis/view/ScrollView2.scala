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
import android.view.{View, ViewGroup}
import android.view.ViewGroup.LayoutParams
import android.widget.{Button, LinearLayout, TextView}

/**
 * Demonstrates wrapping a layout in a ScrollView.
 *
 */
class ScrollView2 extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.scroll_view_2)

    val layout = findViewById(R.id.layout).asInstanceOf[LinearLayout]
    for (i <- 2 until 64) {
      val textView = new TextView(this)
      textView.setText("Text View " + i)
      val p = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                                            LayoutParams.WRAP_CONTENT)
      layout.addView(textView, p)

      val buttonView = new Button(this)
      buttonView.setText("Button " + i)
      layout.addView(buttonView, p)
    }
  }
}
