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
import android.view.{KeyEvent, View}

class ColorMatrixSample extends GraphicsActivity {
    
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private def setTranslate(cm: ColorMatrix, dr: Float, dg: Float,
                                   db: Float, da: Float) {
      cm set Array[Float](
        2, 0, 0, 0, dr,
        0, 2, 0, 0, dg,
        0, 0, 2, 0, db,
        0, 0, 0, 1, da)
    }

    private def setContrast(cm: ColorMatrix, contrast: Float) {
      val scale = contrast + 1.f
      val translate = (-.5f * scale + .5f) * 255.f
      cm set Array(
        scale, 0, 0, 0, translate,
        0, scale, 0, 0, translate,
        0, 0, scale, 0, translate,
        0, 0, 0, 1, 0)
    }

    private def setContrastTranslateOnly(cm: ColorMatrix, contrast: Float) {
      val scale = contrast + 1.f
      val translate = (-.5f * scale + .5f) * 255.f
      cm set Array[Float](
        1, 0, 0, 0, translate,
        0, 1, 0, 0, translate,
        0, 0, 1, 0, translate,
        0, 0, 0, 1, 0)
    }

    private def setContrastScaleOnly(cm: ColorMatrix, contrast: Float) {
      val scale = contrast + 1.f
      val translate = (-.5f * scale + .5f) * 255.f
      cm set Array[Float](
        scale, 0, 0, 0, 0,
        0, scale, 0, 0, 0,
        0, 0, scale, 0, 0,
        0, 0, 0, 1, 0)
    }
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    private val mPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCM = new ColorMatrix()
    private var mSaturation: Float = _
    private var mAngle: Float = _

    private val mBitmap =
      BitmapFactory.decodeResource(context.getResources, R.drawable.balloons)

    override protected def onDraw(canvas: Canvas) {
      val paint = mPaint;
      var x = 20f
      var y = 20f

      canvas drawColor Color.WHITE

      paint setColorFilter null
      canvas.drawBitmap(mBitmap, x, y, paint)

      val cm = new ColorMatrix()

      mAngle += 2
      if (mAngle > 180) {
        mAngle = 0
      }

      //convert our animated angle [-180...180] to a contrast value of [-1..1]
      val contrast = mAngle / 180.f

      setContrast(cm, contrast)
      paint setColorFilter new ColorMatrixColorFilter(cm)
      canvas.drawBitmap(mBitmap, x + mBitmap.getWidth + 10, y, paint)

      setContrastScaleOnly(cm, contrast)
      paint setColorFilter new ColorMatrixColorFilter(cm)
      canvas.drawBitmap(mBitmap, x, y + mBitmap.getHeight + 10, paint)

      setContrastTranslateOnly(cm, contrast)
      paint setColorFilter new ColorMatrixColorFilter(cm)
      canvas.drawBitmap(mBitmap, x, y + 2*(mBitmap.getHeight + 10), paint)

      invalidate()
    }
  }
}

