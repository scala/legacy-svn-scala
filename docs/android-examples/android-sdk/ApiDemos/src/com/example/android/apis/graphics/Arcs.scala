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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
//import com.example.android.apis.R;

import android.app.Activity
import android.content.Context
import android.graphics._
import android.os.Bundle
import android.view.View

class Arcs extends GraphicsActivity {
  import Arcs._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

}

object Arcs {

  private object SampleView {
    private final val SWEEP_INC = 2f
    private final val START_INC = 15f
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object
    private val mPaints = {
      val p0 = new Paint
        p0 setAntiAlias true
        p0 setStyle Paint.Style.FILL
        p0 setColor 0x88FF0000
      val p1 = new Paint(p0)
        p1 setColor 0x8800FF00
      val p2 = new Paint(p0)
        p2 setStyle Paint.Style.STROKE
        p2 setStrokeWidth 4
        p2 setColor 0x880000FF
      val p3 = new Paint(p2)
        p3 setColor 0x88888888
      Array(p0, p1, p2, p3)
    }
    private val mUseCenters =
      Array(false, true, false, true)
    private val mOvals = Array(
      new RectF( 10, 270,  70, 330),
      new RectF( 90, 270, 150, 330),
      new RectF(170, 270, 230, 330),
      new RectF(250, 270, 310, 330)
    )
    private val mBigOval = new RectF(40, 10, 280, 250)

    private val mFramePaint = new Paint
    mFramePaint setAntiAlias true
    mFramePaint setStyle Paint.Style.STROKE
    mFramePaint setStrokeWidth 0

    private var mStart: Float = _
    private var mSweep: Float = _
    private var mBigIndex: Int = _

    private def drawArcs(canvas: Canvas, oval: RectF, useCenter: Boolean,
                         paint: Paint) {
      canvas.drawRect(oval, mFramePaint)
      canvas.drawArc(oval, mStart, mSweep, useCenter, paint)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      drawArcs(canvas, mBigOval, mUseCenters(mBigIndex), mPaints(mBigIndex))

      for (i <- 0 until mOvals.length) {
        drawArcs(canvas, mOvals(i), mUseCenters(i), mPaints(i))
      }

      mSweep += SWEEP_INC
      if (mSweep > 360) {
        mSweep -= 360
        mStart += START_INC
        if (mStart >= 360) mStart -= 360
        mBigIndex = (mBigIndex + 1) % mOvals.length
      }
      invalidate()
    }
  }
}

