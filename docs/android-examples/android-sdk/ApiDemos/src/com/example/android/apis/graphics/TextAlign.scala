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
import android.view._

class TextAlign extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private final val DY = 30
    private final val TEXT_L = "Left"
    private final val TEXT_C = "Center"
    private final val TEXT_R = "Right"
    private final val POSTEXT = "Positioned"
    private final val TEXTONPATH = "Along a path"

    private def makePath(p: Path) {
      p.moveTo(10, 0)
      p.cubicTo(100, -50, 200, 50, 300, 0)
    }

  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    private val mPaint = new Paint
    mPaint setAntiAlias true
    mPaint setTextSize 30
    mPaint setTypeface Typeface.SERIF

    private var mX: Float = _

    private val mPos = buildTextPositions(POSTEXT, 0, mPaint)

    private val mPath = new Path
    makePath(mPath)

    private val mPathPaint = new Paint
    mPathPaint setAntiAlias true
    mPathPaint setColor 0x800000FF
    mPathPaint setStyle Paint.Style.STROKE

    setFocusable(true)

    private def buildTextPositions(text: String, y: Float, paint: Paint): Array[Float] = {
      val widths = new Array[Float](text.length)
      // initially get the widths for each char
      val n = paint.getTextWidths(text, widths)
      // now popuplate the array, interleaving spaces for the Y values
      val pos = new Array[Float](n * 2)
      var accumulatedX = 0f
      for (i <- 0 until n) {
        pos(i*2 + 0) = accumulatedX
        pos(i*2 + 1) = y
        accumulatedX += widths(i)
      }
      pos
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      val p = mPaint
      val x = mX
      var y = 0
      val pos = mPos

      // draw the normal strings

      p setColor 0x80FF0000
      canvas.drawLine(x, y, x, y+DY*3, p)
      p setColor Color.BLACK

      canvas.translate(0, DY)
      p setTextAlign Paint.Align.LEFT
      canvas.drawText(TEXT_L, x, y, p)

      canvas.translate(0, DY)
      p setTextAlign Paint.Align.CENTER
      canvas.drawText(TEXT_C, x, y, p)

      canvas.translate(0, DY)
      p setTextAlign Paint.Align.RIGHT
      canvas.drawText(TEXT_R, x, y, p)

      canvas.translate(100, DY*2)

      // now draw the positioned strings

      p setColor 0xBB00FF00
      for (i <- 0 until pos.length/2) {
        canvas.drawLine(pos(i*2+0), pos(i*2+1)-DY,
                        pos(i*2+0), pos(i*2+1)+DY*2, p)
      }
      p setColor Color.BLACK

      p setTextAlign Paint.Align.LEFT
      canvas.drawPosText(POSTEXT, pos, p)

      canvas.translate(0, DY)
      p setTextAlign Paint.Align.CENTER
      canvas.drawPosText(POSTEXT, pos, p)

      canvas.translate(0, DY)
      p setTextAlign Paint.Align.RIGHT
      canvas.drawPosText(POSTEXT, pos, p)
      
      // now draw the text on path

      canvas.translate(-100, DY*2)

      canvas.drawPath(mPath, mPathPaint)
      p setTextAlign Paint.Align.LEFT
      canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p)

      canvas.translate(0, DY*1.5f)
      canvas.drawPath(mPath, mPathPaint)
      p setTextAlign Paint.Align.CENTER
      canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p)

      canvas.translate(0, DY*1.5f)
      canvas.drawPath(mPath, mPathPaint)
      p setTextAlign Paint.Align.RIGHT
      canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p)
    }

    override protected def onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
      super.onSizeChanged(w, h, ow, oh)
      mX = w * 0.5f  // remember the center of the screen
    }
  }
}

