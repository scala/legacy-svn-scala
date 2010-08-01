/*
 * Copyright (C) 2009 The Android Open Source Project
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

class ColorFilters extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private def addToTheRight(curr: Drawable, prev: Drawable) {
      val r = prev.getBounds
      val x = r.right + 12
      val center = (r.top + r.bottom) >> 1
      val h = curr.getIntrinsicHeight
      val y = center - (h >> 1)

      curr.setBounds(x, y, x + curr.getIntrinsicWidth, y + h)
    }
  }

  private /*static*/ class SampleView(activity: Activity) extends View(activity) {
    import SampleView._  // companion object

    private val mActivity = activity
    private val resources = mActivity.getResources
    setFocusable(true)

    private val mDrawable =
      resources.getDrawable(R.drawable.btn_default_normal)
    mDrawable.setBounds(0, 0, 150, 48)
    mDrawable setDither true

    private val resIDs = Array(
      R.drawable.btn_circle_normal,
      R.drawable.btn_check_off,
      R.drawable.btn_check_on
    )
    private val mDrawables = new Array[Drawable](resIDs.length)
    private var prev = mDrawable
    for (i <- 0 until resIDs.length) {
      mDrawables(i) = resources getDrawable resIDs(i)
      mDrawables(i) setDither true
      addToTheRight(mDrawables(i), prev)
      prev = mDrawables(i)
    }

    private val mPaint = new Paint()
    mPaint setAntiAlias true
    mPaint setTextSize 16
    mPaint setTextAlign Paint.Align.CENTER
            
    private val mPaint2 = new Paint(mPaint)
    mPaint2 setAlpha 64

    private val mPaintTextOffset = {
      val fm = mPaint.getFontMetrics
      (fm.descent + fm.ascent) * 0.5f
    }
    private val mColors = Array(
      0,
      0xCC0000FF,
      0x880000FF,
      0x440000FF,
      0xFFCCCCFF,
      0xFF8888FF,
      0xFF4444FF
    )
    private val mModes = Array(
      PorterDuff.Mode.SRC_ATOP,
      PorterDuff.Mode.MULTIPLY
    )
    private var mModeIndex = 0

    updateTitle()

    private def updateTitle() {
      mActivity setTitle mModes(mModeIndex).toString
    }

    override protected def onDraw(canvas: Canvas) {
      def drawSample(canvas: Canvas, filter: ColorFilter) {
        val r = mDrawable.getBounds
        val x = (r.left + r.right) * 0.5f
        val y = (r.top + r.bottom) * 0.5f - mPaintTextOffset

        mDrawable setColorFilter filter
        mDrawable draw canvas
        canvas.drawText("Label", x+1, y+1, mPaint2)
        canvas.drawText("Label", x, y, mPaint)
            
        for (dr <- mDrawables) {
          dr setColorFilter filter
          dr draw canvas
        }
      }
      canvas drawColor 0xFFCCCCCC

      canvas.translate(8, 12)
      for (color <- mColors) {
        val filter =
          if (color == 0) null
          else new PorterDuffColorFilter(color, mModes(mModeIndex))
        drawSample(canvas, filter)
        canvas.translate(0, 55)
      }
    }

    override def onTouchEvent(event: MotionEvent): Boolean = {
      def swapPaintColors() {
        if (mPaint.getColor == 0xFF000000) {
          mPaint setColor 0xFFFFFFFF
          mPaint2 setColor 0xFF000000
        } else {
          mPaint setColor 0xFF000000
          mPaint2 setColor 0xFFFFFFFF
        }
        mPaint2 setAlpha 64
      }
      val x = event.getX
      val y = event.getY
      event.getAction match {
        case MotionEvent.ACTION_DOWN =>
          // do nothing
        case MotionEvent.ACTION_MOVE =>
          // do nothing
        case MotionEvent.ACTION_UP =>
          // update mode every other time we change paint colors
          if (mPaint.getColor == 0xFFFFFFFF) {
            mModeIndex = (mModeIndex + 1) % mModes.length
            updateTitle()
          }
          swapPaintColors()
          invalidate()
      }
      true
    }
  }
}

