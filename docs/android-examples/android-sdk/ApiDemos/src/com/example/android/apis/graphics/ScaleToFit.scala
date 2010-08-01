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

class ScaleToFit extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private final val sFits = Array(
      Matrix.ScaleToFit.FILL,
      Matrix.ScaleToFit.START,
      Matrix.ScaleToFit.CENTER,
      Matrix.ScaleToFit.END
    )
    private final val sFitLabels = Array(
      "FILL", "START", "CENTER", "END"
    )
    private final val sSrcData = Array(
      80, 40, Color.RED,
      40, 80, Color.GREEN,
      30, 30, Color.BLUE,
      80, 80, Color.BLACK
    )
    private final val N = 4

    private final val WIDTH = 52
    private final val HEIGHT = 52
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object
    private final val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private final val mHairPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private final val mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private final val mMatrix = new Matrix
    private final val mSrcR = new RectF
    private final val mDstR = new RectF(0, 0, WIDTH, HEIGHT)

    mHairPaint setStyle Paint.Style.STROKE
    mLabelPaint setTextSize 16

    private def setSrcR(index: Int) {
      val w = sSrcData(index*3 + 0)
      val h = sSrcData(index*3 + 1)
      mSrcR.set(0, 0, w, h)
    }

    private def drawSrcR(canvas: Canvas, index: Int) {
      mPaint.setColor(sSrcData(index*3 + 2))
      canvas.drawOval(mSrcR, mPaint)
    }

    private def drawFit(canvas: Canvas, index: Int, stf: Matrix.ScaleToFit) {
      canvas.save()

      setSrcR(index)
      mMatrix.setRectToRect(mSrcR, mDstR, stf)
      canvas concat mMatrix
      drawSrcR(canvas, index)

      canvas.restore()

      canvas.drawRect(mDstR, mHairPaint)
    }

    override protected def onDraw(canvas: Canvas) {
      val paint = mPaint

      canvas drawColor Color.WHITE

      canvas.translate(10, 10)

      canvas.save()
      for (i <- 0 until N) {
        setSrcR(i)
        drawSrcR(canvas, i)
        canvas.translate(mSrcR.width + 15, 0)
      }
      canvas.restore()

      canvas.translate(0, 100)
      for (j <- 0 until sFits.length) {
        canvas.save()
        for (i <- 0 until N) {
          drawFit(canvas, i, sFits(j))
          canvas.translate(mDstR.width + 8, 0)
        }
        canvas.drawText(sFitLabels(j), 0, HEIGHT*2/3, mLabelPaint)
        canvas.restore()
        canvas.translate(0, 80)
      }
    }
  }
}

