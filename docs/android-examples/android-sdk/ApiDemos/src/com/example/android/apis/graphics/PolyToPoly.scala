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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
//import com.example.android.apis.R;

import android.app.Activity
import android.content.Context
import android.graphics._
import android.os.Bundle
import android.view.View

class PolyToPoly extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    private val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMatrix = new Matrix()

    // for when the style is STROKE
    mPaint setStrokeWidth 4
    // for when we draw text
    mPaint setTextSize 40
    mPaint setTextAlign Paint.Align.CENTER
    private val mFontMetrics = mPaint.getFontMetrics()

    override protected def onDraw(canvas: Canvas) {
      def doDraw(canvas: Canvas, src: Array[Float], dst: Array[Float]) {
        canvas.save()
        mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1)
        canvas concat mMatrix

        mPaint setColor Color.GRAY
        mPaint setStyle Paint.Style.STROKE
        canvas.drawRect(0, 0, 64, 64, mPaint)
        canvas.drawLine(0, 0, 64, 64, mPaint)
        canvas.drawLine(0, 64, 64, 0, mPaint)

        mPaint setColor Color.RED
        mPaint setStyle Paint.Style.FILL
        // how to draw the text center on our square
        // centering in X is easy... use alignment (and X at midpoint)
        val x = 64/2
        // centering in Y, we need to measure ascent/descent first
        val y = 64/2 - (mFontMetrics.ascent + mFontMetrics.descent)/2
        canvas.drawText(src.length/2 + "", x, y, mPaint)

        canvas.restore()
      }
      val paint = mPaint

      canvas drawColor Color.WHITE

      canvas.save()
      canvas.translate(10, 10)
      // translate (1 point)
      doDraw(canvas, Array(0f, 0f), Array(5f, 5f))
      canvas.restore()
            
      canvas.save()
      canvas.translate(160, 10)
      // rotate/uniform-scale (2 points)
      doDraw(canvas, Array(32f, 32f, 64f, 32f),
                     Array(32f, 32f, 64f, 48f))
      canvas.restore()

      canvas.save()
      canvas.translate(10, 110)
      // rotate/skew (3 points)
      doDraw(canvas, Array(0f, 0f, 64f, 0f, 0f,  64f),
                     Array(0f, 0f, 96f, 0f, 24f, 64f))
      canvas.restore()

      canvas.save()
      canvas.translate(160, 110)
      // perspective (4 points)
      doDraw(canvas, Array(0f, 0f, 64f, 0f, 64f, 64f, 0f, 64f),
                     Array(0f, 0f, 96f, 0f, 64f, 96f, 0f, 64f))
      canvas.restore()
    }
  }
}

