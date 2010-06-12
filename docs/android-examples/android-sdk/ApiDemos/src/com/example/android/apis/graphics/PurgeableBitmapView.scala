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

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory, Canvas, Color, Paint}
import android.graphics.BitmapFactory.Options
import android.view.View

import java.io.ByteArrayOutputStream

/**
 * PurgeableBitmapView works with PurgeableBitmap to demonstrate the effects of
 * setting Bitmaps as being purgeable.
 *
 * PurgeableBitmapView decodes an encoded bitstream to a Bitmap each time update()
 * is invoked(), and its onDraw() draws the Bitmap and a number to screen.
 * The number is used to indicate the number of Bitmaps that has been decoded.
 */
object PurgeableBitmapView {
  private final val WIDTH = 150
  private final val HEIGHT = 450
  private final val STRIDE = 320   // must be >= WIDTH

  private final val delay = 100
}

class PurgeableBitmapView(context: Context, isPurgeable: Boolean)
                          extends View(context) {
  import PurgeableBitmapView._  // companion object

  private var bitstream: Array[Byte] = _
  private var mBitmap: Bitmap = _
  private val mArraySize = 200
  private val mBitmapArray = new Array[Bitmap](mArraySize)
  private val mOptions = new Options()
  private var mDecodingCount = 0
  private val mPaint = new Paint()
  private val textSize = 32

  { // init
    setFocusable(true)
    mOptions.inPurgeable = isPurgeable

    val colors = createColors()
    val src = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT,
                                  Bitmap.Config.ARGB_8888)
    bitstream = generateBitstream(src, Bitmap.CompressFormat.JPEG, 80)

    mPaint setTextSize textSize
    mPaint setColor Color.GRAY
  }

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

  def update(handler: PurgeableBitmap#RefreshHandler): Int = {
    try {
      mBitmapArray(mDecodingCount) = BitmapFactory.decodeByteArray(
        bitstream, 0, bitstream.length, mOptions)
      mBitmap = mBitmapArray(mDecodingCount)
      mDecodingCount += 1
      if (mDecodingCount < mArraySize) {
        handler sleep delay
        0
      } else {
        -mDecodingCount
      }
    } catch {
      case error: OutOfMemoryError =>
        for (i <- 0 until mDecodingCount) {
          mBitmapArray(i).recycle()
        }
        mDecodingCount + 1
    }
  }

  override protected def onDraw(canvas: Canvas) {
    canvas drawColor Color.WHITE
    canvas.drawBitmap(mBitmap, 0, 0, null)
    canvas.drawText(String.valueOf(mDecodingCount),
                    WIDTH / 2 - 20, HEIGHT / 2, mPaint)
  }

  private def generateBitstream(src: Bitmap, format: Bitmap.CompressFormat,
                                quality: Int): Array[Byte] = {
    val os = new ByteArrayOutputStream()
    src.compress(format, quality, os)
    os.toByteArray()
  }

}
