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

class Regions extends GraphicsActivity {
  import Regions._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

}

object Regions {

  private object SampleView {
    private def drawCentered(c: Canvas, r: Rect, p: Paint) {
      var inset = p.getStrokeWidth() * 0.5f
      if (inset == 0) {   // catch hairlines
        inset = 0.5f
      }
      c.drawRect(r.left + inset, r.top + inset,
                 r.right - inset, r.bottom - inset, p)
    }
  }

  private class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object
    private final val mPaint = new Paint()
    private final val mRect1 = new Rect()
    private final val mRect2 = new Rect()

    setFocusable(true)

    mPaint setAntiAlias true
    mPaint setTextSize 16
    mPaint setTextAlign Paint.Align.CENTER

    mRect1.set(10, 10, 100, 80)
    mRect2.set(50, 50, 130, 110)

    private def drawOriginalRects(canvas: Canvas, alpha: Int) {
      mPaint setStyle Paint.Style.STROKE
      mPaint setColor Color.RED
      mPaint setAlpha alpha
      drawCentered(canvas, mRect1, mPaint)
      mPaint setColor Color.BLUE
      mPaint setAlpha alpha
      drawCentered(canvas, mRect2, mPaint)

      // restore style
      mPaint setStyle Paint.Style.FILL
    }

    private def drawRgn(canvas: Canvas, color: Int, str: String, op: Region.Op) {
      if (str != null) {
        mPaint setColor Color.BLACK
        canvas.drawText(str, 80, 24, mPaint)
      }

      val rgn = new Region
      rgn set mRect1
      rgn.op(mRect2, op)

      mPaint setColor color
      val iter = new RegionIterator(rgn)
      val r = new Rect

      canvas.translate(0, 30)
      mPaint setColor color
      while (iter.next(r)) {
        canvas.drawRect(r, mPaint)
      }
      drawOriginalRects(canvas, 0x80)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.GRAY

      canvas.save()
      canvas.translate(80, 5)
      drawOriginalRects(canvas, 0xFF)
      canvas.restore()

      mPaint setStyle Paint.Style.FILL

      canvas.save()
      canvas.translate(0, 140)
      drawRgn(canvas, Color.RED, "Union", Region.Op.UNION)
      canvas.restore()

      canvas.save()
      canvas.translate(0, 280)
      drawRgn(canvas, Color.BLUE, "Xor", Region.Op.XOR)
      canvas.restore()

      canvas.save()
      canvas.translate(160, 140)
      drawRgn(canvas, Color.GREEN, "Difference", Region.Op.DIFFERENCE)
      canvas.restore()

      canvas.save()
      canvas.translate(160, 280)
      drawRgn(canvas, Color.WHITE, "Intersect", Region.Op.INTERSECT)
      canvas.restore()
    }
  }
}

