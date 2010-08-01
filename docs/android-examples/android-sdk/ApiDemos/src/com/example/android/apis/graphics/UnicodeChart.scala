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
import android.view.{KeyEvent, View, Window}

class UnicodeChart extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(new SampleView(this))
  }

  private object SampleView {
    private final val XMUL = 20
    private final val YMUL = 28
    private final val YBASE = 18
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    private val mChars = new Array[Char](256)
    private val mPos = new Array[Float](512)
    private var mBase: Int = _

    setFocusable(true)
    setFocusableInTouchMode(true)

    private val mBigCharPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    mBigCharPaint setTextSize 15
    mBigCharPaint setTextAlign Paint.Align.CENTER

    private val mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    mLabelPaint setTextSize 8
    mLabelPaint setTextAlign Paint.Align.CENTER

    // the position array is the same for all charts
    val pos = mPos
    var index = 0
    for (col <- 0 until 16) {
      val x = col * 20 + 10f
      for (row <- 0 until 16) {
        pos(index) = x; index += 1
        pos(index) = row * YMUL + YBASE; index += 1
      }
    }

    private def computeX(index: Int): Float = (index >> 4) * 20 + 10f

    private def computeY(index: Int): Float = (index & 0xF) * YMUL + YMUL

    private def drawChart(canvas: Canvas, base: Int) {
      val chars = mChars
      for (i <- 0 until chars.length) {
        val unichar = base + i
        chars(i) = unichar.toChar

        canvas.drawText(Integer.toHexString(unichar),
                        computeX(i), computeY(i), mLabelPaint)
      }
      canvas.drawPosText(chars, 0, 256, mPos, mBigCharPaint)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      canvas.translate(0, 1)
      drawChart(canvas, mBase * 256)
    }

    override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
      keyCode match {
        case KeyEvent.KEYCODE_DPAD_LEFT =>
          if (mBase > 0) {
            mBase -= 1
            invalidate()
          }
          true
        case KeyEvent.KEYCODE_DPAD_RIGHT =>
          mBase += 1
          invalidate()
          true
        case _ =>
          super.onKeyDown(keyCode, event)
      }
    }
  }
}

