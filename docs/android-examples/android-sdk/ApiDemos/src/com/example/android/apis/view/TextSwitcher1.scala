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
import android.view.{Gravity, View}
import android.view.animation.{Animation, AnimationUtils}
import android.widget.{Button, TextSwitcher, TextView, ViewSwitcher}

/**
 * Uses a TextSwitcher.
 */
class TextSwitcher1 extends Activity with ViewSwitcher.ViewFactory
                                     with View.OnClickListener {

  private var mSwitcher: TextSwitcher = _
  private var mCounter = 0

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.text_switcher_1)

    mSwitcher = findViewById(R.id.switcher).asInstanceOf[TextSwitcher]
    mSwitcher setFactory this

    val in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
    val out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
    mSwitcher setInAnimation in
    mSwitcher setOutAnimation out

    val nextButton = findViewById(R.id.next).asInstanceOf[Button]
    nextButton setOnClickListener this

    updateCounter()
  }

  def onClick(v: View) {
    mCounter += 1
    updateCounter()
  }

  private def updateCounter() {
    mSwitcher setText String.valueOf(mCounter)
  }

  def makeView(): View = {
    val t = new TextView(this)
    t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
    t setTextSize 36
    t
  }
}
