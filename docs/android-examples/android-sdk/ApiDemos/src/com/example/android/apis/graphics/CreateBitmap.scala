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

import java.io.ByteArrayOutputStream

class CreateBitmap extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private final val WIDTH = 50
    private final val HEIGHT = 50
    private final val STRIDE = 64   // must be >= WIDTH

    private def createColors(): Array[Int] = {
      val colors = new Array[Int](STRIDE * HEIGHT)
      for (y <- 0 until HEIGHT; x <- 0 until WIDTH) {
        val r = x * 255 / (WIDTH - 1)
        val g = y * 255 / (HEIGHT - 1)
        val b = 255 - math.min(r, g)
        val a = math.max(r, g)
        colors(y * STRIDE + x) = (a << 24) | (r << 16) | (g << 8) | b
      }
      colors
    }

    private def codec(src: Bitmap, format: Bitmap.CompressFormat,
                      quality: Int): Bitmap = {
      val os = new ByteArrayOutputStream()
      src.compress(format, quality, os) 

      val array = os.toByteArray()
      BitmapFactory.decodeByteArray(array, 0, array.length)
    }     
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import Bitmap._, SampleView._  // companion object

    setFocusable(true)

    private val mColors = createColors()
    val colors = mColors

    private val mBitmaps = Array(
      // these three are initialized with colors[]
      Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Config.ARGB_8888),
      Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Config.RGB_565),
      Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Config.ARGB_4444),

      // these three will have their colors set later
      Bitmap.createBitmap(WIDTH, HEIGHT, Config.ARGB_8888),
      Bitmap.createBitmap(WIDTH, HEIGHT, Config.RGB_565),
      Bitmap.createBitmap(WIDTH, HEIGHT, Config.ARGB_4444)
    )
    for (i <- 3 to 5) {
      mBitmaps(i).setPixels(colors, 0, STRIDE, 0, 0, WIDTH, HEIGHT)
    }

    private val mPaint = new Paint()
    mPaint setDither true

    // now encode/decode using JPEG and PNG
    private val mJPEG = new Array[Bitmap](mBitmaps.length)
    private val mPNG = new Array[Bitmap](mBitmaps.length)
    for (i <- 0 until mBitmaps.length) {
      mJPEG(i) = codec(mBitmaps(i), Bitmap.CompressFormat.JPEG, 80)
      mPNG(i) = codec(mBitmaps(i), Bitmap.CompressFormat.PNG, 0)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      for (i <- 0 until mBitmaps.length) {
        canvas.drawBitmap(mBitmaps(i), 0, 0, null)
        canvas.drawBitmap(mJPEG(i), 80, 0, null)
        canvas.drawBitmap(mPNG(i), 160, 0, null)
        canvas.translate(0, mBitmaps(i).getHeight)
      }

      // draw the color array directly, w/o craeting a bitmap object
      canvas.drawBitmap(mColors, 0, STRIDE, 0, 0, WIDTH, HEIGHT,
                        true, null)
      canvas.translate(0, HEIGHT)
      canvas.drawBitmap(mColors, 0, STRIDE, 0, 0, WIDTH, HEIGHT,
                        false, mPaint)
    }
  }
}

