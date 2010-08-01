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

import com.example.android.apis.R

import android.content.Context
import android.graphics._
import android.os.Bundle
import android.view._
import android.util.FloatMath

class BitmapMesh extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private final val WIDTH = 20
    private final val HEIGHT = 20
    private final val COUNT = (WIDTH + 1) * (HEIGHT + 1)

    private def setXY(array: Array[Float], index: Int, x: Float, y: Float) {
      array(index*2 + 0) = x
      array(index*2 + 1) = y
    }
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    private final val mVerts = new Array[Float](COUNT*2)
    private final val mOrig = new Array[Float](COUNT*2)

    private final val mMatrix = new Matrix()
    private final val mInverse = new Matrix()

    setFocusable(true)

    private val mBitmap =
      BitmapFactory.decodeResource(getResources, R.drawable.beach)   
    val w = mBitmap.getWidth
    val h = mBitmap.getHeight
    // construct our mesh
    var index = 0
    for (y <- 0 to HEIGHT) {
      val fy = h * y / HEIGHT
      for (x <- 0 to WIDTH) {
        val fx = w * x / WIDTH
        setXY(mVerts, index, fx, fy)
        setXY(mOrig, index, fx, fy)
        index += 1
      }
    }

    mMatrix.setTranslate(10, 10)
    mMatrix invert mInverse

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor 0xFFCCCCCC

      canvas concat mMatrix
      canvas.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, mVerts, 0,
                                  null, 0, null)
    }

    private def warp(cx: Float, cy: Float) {
      val K = 10000f
      val src = mOrig
      val dst = mVerts
      for (i <- 0 until COUNT*2 by 2) {
        val x = src(i+0)
        val y = src(i+1)
        val dx = cx - x
        val dy = cy - y
        val dd = dx*dx + dy*dy
        val d = FloatMath.sqrt(dd)
        var pull = K / (dd + 0.000001f)
                
        pull /= (d + 0.000001f)
        //   android.util.Log.d("skia", "index " + i + " dist=" + d + " pull=" + pull);

        if (pull >= 1) {
          dst(i+0) = cx
          dst(i+1) = cy
        } else {
          dst(i+0) = x + dx * pull
          dst(i+1) = y + dy * pull
        }
      }
    }

    private var mLastWarpX = -9999  // don't match a touch coordinate
    private var mLastWarpY: Int = _

    override def onTouchEvent(event: MotionEvent): Boolean = {
      val pt = Array(event.getX, event.getY)
      mInverse mapPoints pt

      val x = pt(0).toInt
      val y = pt(1).toInt
      if (mLastWarpX != x || mLastWarpY != y) {
        mLastWarpX = x
        mLastWarpY = y
        warp(pt(0), pt(1))
        invalidate()
      }
      true
    }
  }
}

