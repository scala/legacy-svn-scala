/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.graphics

import com.example.android.apis.R

import android.app.Activity
import android.content.Context
import android.graphics._
import android.graphics.drawable._
import android.view.animation._
import android.os.Bundle
import android.view.{KeyEvent, View}

class AnimateDrawables extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {

    setFocusable(true)
    setFocusableInTouchMode(true)

    val dr = context.getResources.getDrawable(R.drawable.beach)
    dr.setBounds(0, 0, dr.getIntrinsicWidth, dr.getIntrinsicHeight)

    val an = new TranslateAnimation(0, 100, 0, 200)
    an setDuration 2000
    an.setRepeatCount(-1)
    an.initialize(10, 10, 10, 10)

    private val mDrawable = new AnimateDrawable(dr, an)
    an.startNow()

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      mDrawable.draw(canvas)
      invalidate()
    }
  }
}

