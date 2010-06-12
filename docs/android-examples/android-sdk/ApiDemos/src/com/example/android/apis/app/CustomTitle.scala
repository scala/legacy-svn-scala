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

import android.app.Activity
import android.os.Bundle
import android.view.{View, Window}
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView}

import com.example.android.apis.R


/**
 * Example of how to use a custom title {@link android.view.Window#FEATURE_CUSTOM_TITLE}.
 * <h3>CustomTitle</h3>

<p>This demonstrates how a custom title can be used.</p>

<h4>Demo</h4>
App/Title/Custom Title
 
<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com.example.android.apis/app/CustomTitle.java</td>
 *             <td >The Custom Title implementation</td>
 *         </tr>
 *         <tr>
 *             <td >/res/any/layout/custom_title.xml</td>
 *             <td >Defines contents of the screen</td>
 *         </tr>
 * </table> 
 */
class CustomTitle extends Activity {
    
  /**
   * Initialization of the Activity after it is first created.  Must at least
   * call {@link android.app.Activity#setContentView(int)} to
   * describe what is to be displayed in the screen.
   */
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
    setContentView(R.layout.custom_title)
    getWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_1)
        
    val leftText = findViewById(R.id.left_text).asInstanceOf[TextView]
    val rightText = findViewById(R.id.right_text).asInstanceOf[TextView]
    val leftTextEdit = findViewById(R.id.left_text_edit).asInstanceOf[EditText]
    val rightTextEdit = findViewById(R.id.right_text_edit).asInstanceOf[EditText]
    val leftButton = findViewById(R.id.left_text_button).asInstanceOf[Button]
    val rightButton = findViewById(R.id.right_text_button).asInstanceOf[Button]
        
    leftButton setOnClickListener new OnClickListener {
      def onClick(v: View) {
        leftText setText leftTextEdit.getText
      }
    }
    rightButton setOnClickListener new OnClickListener {
      def onClick(v: View) {
        rightText setText rightTextEdit.getText
      }
    }
  }
}
