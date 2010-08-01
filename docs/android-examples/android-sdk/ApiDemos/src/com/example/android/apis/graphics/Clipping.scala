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

class Clipping extends GraphicsActivity {
  import Clipping._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

}

object Clipping {

  private class SampleView(context: Context) extends View(context) {
    private val mPath = new Path
    private val mPaint = new Paint
    mPaint setAntiAlias true
    mPaint setStrokeWidth 6
    mPaint setTextSize 16
    mPaint setTextAlign Paint.Align.RIGHT
    setFocusable(true)

    private def drawScene(canvas: Canvas) {
      canvas.clipRect(0, 0, 100, 100)

      canvas drawColor Color.WHITE

      mPaint setColor Color.RED
      canvas.drawLine(0, 0, 100, 100, mPaint)

      mPaint setColor Color.GREEN
      canvas.drawCircle(30, 70, 30, mPaint)

      mPaint setColor Color.BLUE
      canvas.drawText("Clipping", 100, 30, mPaint)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.GRAY

      canvas.save()
      canvas.translate(10, 10)
      drawScene(canvas)
      canvas.restore()

      canvas.save()
      canvas.translate(160, 10)
      canvas.clipRect(10, 10, 90, 90)
      canvas.clipRect(30, 30, 70, 70, Region.Op.DIFFERENCE)
      drawScene(canvas)
      canvas.restore()

      canvas.save()
      canvas.translate(10, 160)
      mPath.reset()
      canvas.clipPath(mPath) // makes the clip empty
      mPath.addCircle(50, 50, 50, Path.Direction.CCW)
      canvas.clipPath(mPath, Region.Op.REPLACE)
      drawScene(canvas)
      canvas.restore()

      canvas.save()
      canvas.translate(160, 160)
      canvas.clipRect(0, 0, 60, 60)
      canvas.clipRect(40, 40, 100, 100, Region.Op.UNION)
      drawScene(canvas)
      canvas.restore()

      canvas.save()
      canvas.translate(10, 310)
      canvas.clipRect(0, 0, 60, 60)
      canvas.clipRect(40, 40, 100, 100, Region.Op.XOR)
      drawScene(canvas)
      canvas.restore()

      canvas.save()
      canvas.translate(160, 310)
      canvas.clipRect(0, 0, 60, 60)
      canvas.clipRect(40, 40, 100, 100, Region.Op.REVERSE_DIFFERENCE)
      drawScene(canvas)
      canvas.restore()
    }
  }
}

