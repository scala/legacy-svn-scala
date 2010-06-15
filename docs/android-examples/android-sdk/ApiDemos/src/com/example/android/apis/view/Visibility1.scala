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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button


/**
 * Demonstrates making a view VISIBLE, INVISIBLE and GONE
 *
 */
class Visibility1 extends Activity {

  private var mVictim: View = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.visibility_1)

    // Find the view whose visibility will change
    mVictim = findViewById(R.id.victim)

    // Find our buttons
    val visibleButton = findViewById(R.id.vis).asInstanceOf[Button]
    val invisibleButton = findViewById(R.id.invis).asInstanceOf[Button]
    val goneButton = findViewById(R.id.gone).asInstanceOf[Button]

    // Wire each button to a click listener
    visibleButton setOnClickListener mVisibleListener
    invisibleButton setOnClickListener mInvisibleListener
    goneButton setOnClickListener mGoneListener
  }

  private val mVisibleListener = new OnClickListener() {
    def onClick(v: View) {
      mVictim setVisibility View.VISIBLE
    }
  }

  private val mInvisibleListener = new OnClickListener() {
    def onClick(v: View) {
      mVictim setVisibility View.INVISIBLE
    }
  }

  private val mGoneListener = new OnClickListener() {
    def onClick(v: View) {
      mVictim setVisibility View.GONE
    }
  }
}
