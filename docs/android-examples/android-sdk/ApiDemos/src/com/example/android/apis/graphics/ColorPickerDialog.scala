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

import android.app.Dialog
import android.content.Context
import android.graphics._
import android.os.Bundle
import android.view.{MotionEvent, View}

trait OnColorChangedListener {
  def colorChanged(color: Int)
}

class ColorPickerDialog(context: Context,
                        listener: OnColorChangedListener,
                        initialColor: Int) extends Dialog(context) {
  import ColorPickerDialog._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val l = new OnColorChangedListener() {
      def colorChanged(color: Int) {
        listener colorChanged color
        dismiss()
      }
    }

    setContentView(new ColorPickerView(getContext, l, initialColor))
    setTitle("Pick a Color")
  }

}

object ColorPickerDialog {

  private class ColorPickerView(context: Context,
                                listener: OnColorChangedListener,
                                color: Int) extends View(context) {
    import ColorPickerView._  // companion object

    private val mListener = listener
    private val mColors = Array(
      0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
      0xFFFFFF00, 0xFFFF0000
    )
    val s = new SweepGradient(0, 0, mColors, null)

    private val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    mPaint setShader s
    mPaint setStyle Paint.Style.STROKE
    mPaint setStrokeWidth 32

    private val mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    mCenterPaint setColor color
    mCenterPaint setStrokeWidth 5

    private var mTrackingCenter: Boolean = _
    private var mHighlightCenter: Boolean = _

    override protected def onDraw(canvas: Canvas) {
      val r = CENTER_X - mPaint.getStrokeWidth*0.5f

      canvas.translate(CENTER_X, CENTER_X)

      canvas.drawOval(new RectF(-r, -r, r, r), mPaint)           
      canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint)

      if (mTrackingCenter) {
        val c = mCenterPaint.getColor
        mCenterPaint setStyle Paint.Style.STROKE      
        mCenterPaint.setAlpha(if (mHighlightCenter) 0xFF else 0x80)
        canvas.drawCircle(0, 0,
                          CENTER_RADIUS + mCenterPaint.getStrokeWidth,
                          mCenterPaint)

        mCenterPaint setStyle Paint.Style.FILL
        mCenterPaint setColor c
      }
    }

    override protected def onMeasure(widthMeasureSpec: Int,
                                     heightMeasureSpec: Int) {
      setMeasuredDimension(CENTER_X*2, CENTER_Y*2)
    }

    private def interpColor(colors: Array[Int], unit: Float): Int = {
      def ave(s: Int, d: Int, p: Float): Int =
        s + math.round(p * (d - s))

      if (unit <= 0) {
        colors(0)
      } else if (unit >= 1) {
        colors(colors.length - 1)
      } else {
        var p = unit * (colors.length - 1)
        val i = p.toInt
        p -= i

        // now p is just the fractional part [0...1) and i is the index
        val c0 = colors(i)
        val c1 = colors(i+1)
        val a = ave(Color.alpha(c0), Color.alpha(c1), p)
        val r = ave(Color.red(c0), Color.red(c1), p)
        val g = ave(Color.green(c0), Color.green(c1), p)
        val b = ave(Color.blue(c0), Color.blue(c1), p)

        Color.argb(a, r, g, b)
      }
    }

    private def rotateColor(color: Int, rad: Float): Int = {
      def floatToByte(x: Float): Int = math.round(x)
      def pinToByte(n: Int): Int =
        if (n < 0) 0 else if (n > 255) 255 else n
      val deg = rad * 180 / 3.1415927f
      val r = Color.red(color)
      val g = Color.green(color)
      val b = Color.blue(color)

      val cm = new ColorMatrix()
      val tmp = new ColorMatrix()

      cm.setRGB2YUV()
      tmp.setRotate(0, deg)
      cm postConcat tmp
      tmp.setYUV2RGB()
      cm postConcat tmp

      val a: Array[Float] = cm.getArray()

      val ir = floatToByte(a(0) * r +  a(1) * g +  a(2) * b)
      val ig = floatToByte(a(5) * r +  a(6) * g +  a(7) * b)
      val ib = floatToByte(a(10) * r + a(11) * g + a(12) * b)

      Color.argb(Color.alpha(color), pinToByte(ir),
                 pinToByte(ig), pinToByte(ib))
    }

    override def onTouchEvent(event: MotionEvent): Boolean = {
      val x = event.getX - CENTER_X
      val y = event.getY - CENTER_Y
      val inCenter = math.sqrt(x*x + y*y) <= CENTER_RADIUS

      event.getAction match {
        case MotionEvent.ACTION_DOWN =>
          mTrackingCenter = inCenter
          if (inCenter) {
            mHighlightCenter = true
             invalidate()
          }
        case MotionEvent.ACTION_MOVE =>
          if (mTrackingCenter) {
            if (mHighlightCenter != inCenter) {
              mHighlightCenter = inCenter
              invalidate()
            }
          } else {
            val angle = math.atan2(y, x).toFloat
            // need to turn angle [-PI ... PI] into unit [0....1]
            var unit = angle / (2*PI)
            if (unit < 0) {
              unit += 1
            }
            mCenterPaint setColor interpColor(mColors, unit)
            invalidate()
          }
        case MotionEvent.ACTION_UP =>
          if (mTrackingCenter) {
            if (inCenter) {
              mListener colorChanged mCenterPaint.getColor
            }
            mTrackingCenter = false    // so we draw w/o halo
            invalidate()
          }
        case _ =>
      }
      true
    }
  }

  private object ColorPickerView {
    private final val CENTER_X = 100
    private final val CENTER_Y = 100
    private final val CENTER_RADIUS = 32

    private final val PI = 3.1415926f
  }

}
