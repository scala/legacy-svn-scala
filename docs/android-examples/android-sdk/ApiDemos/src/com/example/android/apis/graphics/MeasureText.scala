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
import android.os.Bundle
import android.view.View

class MeasureText extends GraphicsActivity {
  import MeasureText._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

}

object MeasureText {
  private final val WIDTH  = 50
  private final val HEIGHT = 50
  private final val STRIDE = 64   // must be >= WIDTH
    
  private def createColors(): Array[Int] = {
    val colors = new Array[Int](STRIDE * HEIGHT)
    for (y <- 0 until HEIGHT; x <- 0 until WIDTH) {
      val r = x * 255 / (WIDTH - 1)
      val g = y * 255 / (HEIGHT - 1)
      val b = 255 - math.min(r, g)
      val a = math.max(r, g)
      colors(y * STRIDE + x) = (a << 24) | (r << 16) | (g << 8) | b
    }
    colors
  }

  private class SampleView(context: Context) extends View(context) {
    private var mOriginX = 10f
    private var mOriginY = 80f

    setFocusable(true)
            
    private val mPaint = new Paint()
    mPaint setAntiAlias true
    mPaint setStrokeWidth 5
    mPaint setStrokeCap Paint.Cap.ROUND
    mPaint setTextSize 64
    mPaint setTypeface Typeface.create(Typeface.SERIF, Typeface.ITALIC)

    private def showText(canvas: Canvas, text: String, align: Paint.Align) {
      //   mPaint.setTextAlign(align);

      val N = text.length    
      val bounds = new Rect()
      val widths = new Array[Float](N)

      val count = mPaint.getTextWidths(text, 0, N, widths)
      val w = mPaint.measureText(text, 0, N);
      mPaint.getTextBounds(text, 0, N, bounds)
            
      mPaint setColor 0xFF88FF88
      canvas.drawRect(bounds, mPaint)
      mPaint setColor Color.BLACK
      canvas.drawText(text, 0, 0, mPaint)
            
      val pts = new Array[Float](2 + count*2)
      var x = 0f
      var y = 0f
      pts(0) = x
      pts(1) = y
      for (i <- 0 until count; j = 2 + i*2) {
        x += widths(i)
        pts(j) = x
        pts(j + 1) = y
      }
      mPaint setColor Color.RED
      mPaint setStrokeWidth 0
      canvas.drawLine(0, 0, w, 0, mPaint)
      mPaint setStrokeWidth 5
      canvas.drawPoints(pts, 0, (count + 1) << 1, mPaint)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      canvas.translate(mOriginX, mOriginY)

      showText(canvas, "Measure", Paint.Align.LEFT)
      canvas.translate(0, 80)
      showText(canvas, "wiggy!", Paint.Align.CENTER)
      canvas.translate(0, 80)
      showText(canvas, "Text", Paint.Align.RIGHT)
    }
  }
}

