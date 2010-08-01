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
import android.view.{KeyEvent, View}

class Sweep extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    private val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRotate: Float = _
    private val mMatrix = new Matrix
    private var mDoTiming: Boolean = _

    setFocusable(true)
    setFocusableInTouchMode(true)

    val x = 160f
    val y = 100f
    private val mShader = new SweepGradient(x, y,
      Array(Color.GREEN, Color.RED, Color.BLUE, Color.GREEN), null)
    mPaint setShader mShader

    override protected def onDraw(canvas: Canvas) {
      val paint = mPaint
      val x = 160f
      val y = 100f

      canvas drawColor Color.WHITE

      mMatrix.setRotate(mRotate, x, y)
      mShader setLocalMatrix mMatrix
      mRotate += 3
      if (mRotate >= 360) {
        mRotate = 0
      }
      invalidate()

      if (mDoTiming) {
        var now = System.currentTimeMillis
        for (i <- 0 until 20) {
          canvas.drawCircle(x, y, 80, paint)
        }
        now = System.currentTimeMillis - now
        android.util.Log.d("skia", "sweep ms = " + (now/20.))
      }
      else {
        canvas.drawCircle(x, y, 80, paint)
      }
    }

    override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
      keyCode match {
        case KeyEvent.KEYCODE_D =>
          mPaint setDither !mPaint.isDither
          invalidate()
          true
        case KeyEvent.KEYCODE_T =>
          mDoTiming = !mDoTiming
          invalidate()
          true
        case _ =>
          super.onKeyDown(keyCode, event)
      }
    }
  }
}

