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

package com.example.android.apis.graphics

import com.example.android.apis.R

import android.app.Activity
import android.content.Context
import android.graphics._
import android.graphics.drawable._
import android.os.Bundle
import android.view._

class RoundRects extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    def setCornerRadii(drawable: GradientDrawable,
                       r0: Float, r1: Float, r2: Float, r3: Float) {
      drawable.setCornerRadii(Array(r0, r0, r1, r1,
                                    r2, r2, r3, r3))
    }
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object
    private val mPath = new Path
    private val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRect = new Rect(0, 0, 120, 120)

    private val mDrawable = new GradientDrawable(
      GradientDrawable.Orientation.TL_BR,
      Array(0xFFFF0000, 0xFF00FF00, 0xFF0000FF))
    mDrawable.setShape(GradientDrawable.RECTANGLE);
    mDrawable.setGradientRadius(math.sqrt(2).toFloat * 60)

    setFocusable(true)

    override protected def onDraw(canvas: Canvas) {
      mDrawable setBounds mRect

      var r = 16f

      canvas.save()
      canvas.translate(10, 10)
      mDrawable setGradientType GradientDrawable.LINEAR_GRADIENT
      setCornerRadii(mDrawable, r, r, 0, 0)
      mDrawable draw canvas
      canvas.restore()

      canvas.save()
      canvas.translate(10 + mRect.width + 10, 10)
      mDrawable setGradientType GradientDrawable.RADIAL_GRADIENT
      setCornerRadii(mDrawable, 0, 0, r, r)
      mDrawable draw canvas
      canvas.restore()

      canvas.translate(0, mRect.height + 10)

      canvas.save()
      canvas.translate(10, 10)
      mDrawable setGradientType GradientDrawable.SWEEP_GRADIENT
      setCornerRadii(mDrawable, 0, r, r, 0)
      mDrawable draw canvas
      canvas.restore()

      canvas.save()
      canvas.translate(10 + mRect.width + 10, 10)
      mDrawable setGradientType GradientDrawable.LINEAR_GRADIENT
      setCornerRadii(mDrawable, r, 0, 0, r)
      mDrawable draw canvas
      canvas.restore()

      canvas.translate(0, mRect.height() + 10)

      canvas.save()
      canvas.translate(10, 10)
      mDrawable setGradientType GradientDrawable.RADIAL_GRADIENT
      setCornerRadii(mDrawable, r, 0, r, 0)
      mDrawable draw canvas
      canvas.restore()

      canvas.save()
      canvas.translate(10 + mRect.width + 10, 10)
      mDrawable setGradientType GradientDrawable.SWEEP_GRADIENT
      setCornerRadii(mDrawable, 0, r, 0, r)
      mDrawable draw canvas
      canvas.restore()
    }
  }
}

