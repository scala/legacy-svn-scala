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

import android.app.Activity
import android.content.Context
import android.graphics._
import android.os.Bundle
import android.view.View

class Typefaces extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    private val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private var mFace =
      Typeface.createFromAsset(getContext.getAssets, "fonts/samplefont.ttf")

    mPaint setTextSize 64

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      mPaint setTypeface null
      canvas.drawText("Default", 10, 100, mPaint)
      mPaint setTypeface mFace
      canvas.drawText("Custom", 10, 200, mPaint)
    }
  }
}

