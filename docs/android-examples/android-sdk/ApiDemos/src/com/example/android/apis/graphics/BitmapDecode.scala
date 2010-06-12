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
import android.graphics.drawable._
import android.os.Bundle
import android.view._

import java.io.{IOException, InputStream, ByteArrayOutputStream}

class BitmapDecode extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private def streamToBytes(is: InputStream): Array[Byte] = {
      val os = new ByteArrayOutputStream(1024)
      val buffer = new Array[Byte](1024)
      var len = is.read(buffer)
      try {
        while (len >= 0) {
          os.write(buffer, 0, len)
          len = is.read(buffer)
        }
      } catch {
        case e: java.io.IOException =>
      }
      os.toByteArray()
    }
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object 
    private var mMovieStart: Long = _

    setFocusable(true)

    val res = context.getResources
    var is = res openRawResource R.drawable.beach
    val opts = new BitmapFactory.Options()
    opts.inJustDecodeBounds = true
    var bm = BitmapFactory.decodeStream(is, null, opts)

    // now opts.outWidth and opts.outHeight are the dimension of the
    // bitmap, even though bm is null

    opts.inJustDecodeBounds = false   // this will request the bm
    opts.inSampleSize = 4             // scaled down by 4
    bm = BitmapFactory.decodeStream(is, null, opts)

    private val mBitmap = bm

    // decode an image with transparency
    is = context.getResources.openRawResource(R.drawable.frog)
    private val mBitmap2 = BitmapFactory.decodeStream(is)

    // create a deep copy of it using getPixels() into different configs
    val w = mBitmap2.getWidth
    val h = mBitmap2.getHeight
    val pixels = new Array[Int](w*h)
    mBitmap2.getPixels(pixels, 0, w, 0, 0, w, h)
    private val mBitmap3 =
      Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888)
    private val mBitmap4 =
      Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_4444)

    private val mDrawable = res getDrawable R.drawable.button
    mDrawable.setBounds(150, 20, 300, 100)

    is = res openRawResource R.drawable.animated_gif
    private val mMovie = if (true) {
      Movie.decodeStream(is)
    } else {
      val array = streamToBytes(is)
      Movie.decodeByteArray(array, 0, array.length)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor 0xFFCCCCCC

      val p = new Paint
      p setAntiAlias true

      canvas.drawBitmap(mBitmap, 10, 10, null)
      canvas.drawBitmap(mBitmap2, 10, 170, null)
      canvas.drawBitmap(mBitmap3, 110, 170, null)
      canvas.drawBitmap(mBitmap4, 210, 170, null)

      mDrawable draw canvas

      val now = android.os.SystemClock.uptimeMillis
      if (mMovieStart == 0) {   // first time
        mMovieStart = now
      }
      if (mMovie != null) {
        var dur = mMovie.duration()
        if (dur == 0) {
          dur = 1000
        }
        val relTime = (now - mMovieStart).toInt % dur
        mMovie setTime relTime
        mMovie.draw(canvas, getWidth - mMovie.width, getHeight - mMovie.height)
        invalidate()
      }
    }
  }
}

