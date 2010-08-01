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
import android.graphics._
import android.os.Bundle
import android.view.{KeyEvent, View}

class PathEffects extends GraphicsActivity {
  import PathEffects._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

}

object PathEffects {

  private class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    private var mPhase: Float = _

    setFocusable(true)
    setFocusableInTouchMode(true)

    private val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    mPaint setStyle Paint.Style.STROKE
    mPaint setStrokeWidth 6

    private var mPath = makeFollowPath()

    private val mEffects = new Array[PathEffect](6)

    private val mColors = Array(
      Color.BLACK, Color.RED, Color.BLUE,
      Color.GREEN, Color.MAGENTA, Color.BLACK
    )

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      val bounds = new RectF()
      mPath.computeBounds(bounds, false)
      canvas.translate(10 - bounds.left, 10 - bounds.top)

      makeEffects(mEffects, mPhase)
      mPhase += 1
      invalidate()

      for (i <- 0 until mEffects.length) {
        mPaint setPathEffect mEffects(i)
        mPaint setColor mColors(i)
        canvas.drawPath(mPath, mPaint)
        canvas.translate(0, 28)
      }
    }

    override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
      keyCode match {
        case KeyEvent.KEYCODE_DPAD_CENTER =>
          mPath = makeFollowPath()
          true
        case _ =>
          super.onKeyDown(keyCode, event)
      }
  }

  private object SampleView {

    private def makeDash(phase: Int): PathEffect =
      new DashPathEffect(Array(15f, 5f, 8f, 5f), phase)

    private def makeEffects(e: Array[PathEffect], phase: Float) {
      e(0) = null     // no effect
      e(1) = new CornerPathEffect(10)
      e(2) = new DashPathEffect(Array(10f, 5f, 5f, 5f), phase)
      e(3) = new PathDashPathEffect(makePathDash(), 12, phase,
                                    PathDashPathEffect.Style.ROTATE)
      e(4) = new ComposePathEffect(e(2), e(1))
      e(5) = new ComposePathEffect(e(3), e(1))
    }

    private def makeFollowPath(): Path = {
      val p = new Path()
      p.moveTo(0, 0)
      for (i <- 1 to 15) {
        p.lineTo(i*20, math.random.toFloat * 35)
      }
      p
    }

    private def makePathDash(): Path = {
      val p = new Path()
      p.moveTo(4, 0)
      p.lineTo(0, -4)
      p.lineTo(8, -4)
      p.lineTo(12, 0)
      p.lineTo(8, 4)
      p.lineTo(0, 4)
      p
    }
  }

}

