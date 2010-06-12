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

import android.app.Activity
import android.content.Context
import android.graphics.{Canvas, Color, Paint}
import android.os.Bundle
import android.view.View

class DrawPoints extends GraphicsActivity {
  import DrawPoints._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }
}

object DrawPoints {

  private class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    private val mPaint = new Paint()
    private var mPts: Array[Float] = _

    @inline
    private def buildPoints() {
      val ptCount = (SEGS + 1) * 2
      mPts = new Array[Float](ptCount * 2)

      var value = 0f
      val delta = SIZE / SEGS
      for (i <- 0 to SEGS; val x = i*4 + X; val y = i*4 + Y) {
        mPts(x) = SIZE - value
        mPts(y) = 0
        mPts(x + 2) = 0
        mPts(y + 2) = value
        value += delta
      }
    }
    buildPoints()

    override protected def onDraw(canvas: Canvas) {
      val paint = mPaint

      canvas.translate(10, 10)

      canvas drawColor Color.WHITE

      paint setColor Color.RED
      paint setStrokeWidth 0
      canvas.drawLines(mPts, paint)

      paint setColor Color.BLUE
      paint setStrokeWidth 3
      canvas.drawPoints(mPts, paint)
    }
  }

  private object SampleView {
    private final val SIZE = 300f
    private final val SEGS = 32
    private final val X = 0
    private final val Y = 1
  }

}

