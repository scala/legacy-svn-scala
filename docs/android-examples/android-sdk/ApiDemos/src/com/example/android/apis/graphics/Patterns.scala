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
import android.view.{KeyEvent, MotionEvent, View}

class Patterns extends GraphicsActivity {
  import Patterns._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

}

object Patterns {

  private class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object
        
    private var mTouchStartX: Float = _
    private var mTouchStartY: Float = _
    private var mTouchCurrX: Float = _
    private var mTouchCurrY: Float = _
    private var mDF: DrawFilter = _

    setFocusable(true)
    setFocusableInTouchMode(true)

    private val mFastDF =
      new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG, 0)

    private val mShader1 =
      new BitmapShader(makeBitmap1(), Shader.TileMode.REPEAT,
                                      Shader.TileMode.REPEAT)
    private val mShader2 =
      new BitmapShader(makeBitmap2(), Shader.TileMode.REPEAT,
                                      Shader.TileMode.REPEAT)

    mShader2 setLocalMatrix {
      val m = new Matrix(); m setRotate 30; m
    }

    private val mPaint = new Paint(Paint.FILTER_BITMAP_FLAG)

    override protected def onDraw(canvas: Canvas) {
      canvas setDrawFilter mDF

      mPaint setShader mShader1
      canvas drawPaint mPaint

      canvas.translate(mTouchCurrX - mTouchStartX,
                       mTouchCurrY - mTouchStartY)

      mPaint setShader mShader2
      canvas drawPaint mPaint
    }

    override def onTouchEvent(event: MotionEvent): Boolean = {
      val x = event.getX
      val y = event.getY

      event.getAction match {
        case MotionEvent.ACTION_DOWN =>
          mTouchStartX = x; mTouchCurrX = x
          mTouchStartY = y; mTouchCurrY = y
          mDF = mFastDF
          invalidate()
        case MotionEvent.ACTION_MOVE =>
          mTouchCurrX = x
          mTouchCurrY = y
          invalidate()
        case MotionEvent.ACTION_UP =>
          mDF = null
          invalidate()
        case _ =>
          // do nothing
      }
      true
    }
  }

  private object SampleView {
    private def makeBitmap1(): Bitmap = {
      val bm = Bitmap.createBitmap(40, 40, Bitmap.Config.RGB_565)
      val c = new Canvas(bm)
      c drawColor Color.RED
      val p = new Paint()
      p setColor Color.BLUE
      c.drawRect(5, 5, 35, 35, p)
      bm
    }

    private def makeBitmap2(): Bitmap = {
      val bm = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
      val c = new Canvas(bm)
      val p = new Paint(Paint.ANTI_ALIAS_FLAG)
      p setColor Color.GREEN
      p setAlpha 0xCC
      c.drawCircle(32, 32, 27, p)
      bm
    }
  }

}

