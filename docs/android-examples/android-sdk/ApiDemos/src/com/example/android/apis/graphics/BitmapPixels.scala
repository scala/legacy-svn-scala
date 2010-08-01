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

import java.nio.{IntBuffer, ShortBuffer}

class BitmapPixels extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    // access the red component from a premultiplied color
    private final def getR32(c: Int): Int = (c >>  0) & 0xFF
    // access the red component from a premultiplied color
    private final def getG32(c: Int): Int = (c >>  8) & 0xFF
    // access the red component from a premultiplied color
    private final def getB32(c: Int): Int = (c >> 16) & 0xFF
    // access the red component from a premultiplied color
    private final def getA32(c: Int): Int = (c >> 24) & 0xFF

    /**
     * This takes components that are already in premultiplied form, and
     * packs them into an int in the correct device order.
     */
    private final def pack8888(r: Int, g: Int, b: Int, a: Int): Int =
      (r << 0) | ( g << 8) | (b << 16) | (a << 24)

    private final def pack565(r: Int, g: Int, b: Int): Short =
      ((r << 11) | ( g << 5) | (b << 0)).toShort

    private final def pack4444(r: Int, g: Int, b: Int, a: Int): Short =
      ((a << 0) | ( b << 4) | (g << 8) | (r << 12)).toShort
        
    private final def mul255(c: Int, a: Int): Int = {
      val prod = c * a + 128
      (prod + (prod >> 8)) >> 8
    }

    /**
     * Turn a color int into a premultiplied device color
     */
    private final def premultiplyColor(c: Int): Int = {
      var r = Color.red(c)
      var g = Color.green(c)
      var b = Color.blue(c)
      val a = Color.alpha(c)
      // now apply the alpha to r, g, b
      r = mul255(r, a)
      g = mul255(g, a)
      b = mul255(b, a)
      // now pack it in the correct order
      pack8888(r, g, b, a)
    }

    private final def makeRamp(from: Int, to: Int, n: Int,
                               ramp8888: Array[Int],
                               ramp565: Array[Short],
                               ramp4444: Array[Short]) {
      var r = getR32(from) << 23
      var g = getG32(from) << 23
      var b = getB32(from) << 23
      var a = getA32(from) << 23
      // now compute our step amounts per componenet (biased by 23 bits)
      val dr = ((getR32(to) << 23) - r) / (n - 1)
      val dg = ((getG32(to) << 23) - g) / (n - 1)
      val db = ((getB32(to) << 23) - b) / (n - 1)
      val da = ((getA32(to) << 23) - a) / (n - 1)

      for (i <- 0 until n) {
        ramp8888(i) = pack8888(r >> 23, g >> 23, b >> 23, a >> 23)
        ramp565(i) = pack565(r >> (23+3), g >> (23+2), b >> (23+3))
        ramp4444(i) = pack4444(r >> (23+4), g >> (23+4), b >> (23+4),
                                       a >> (23+4))
        r += dr
        g += dg
        b += db
        a += da
      }
    }

    private final def makeBuffer(src: Array[Int], n: Int): IntBuffer = {
      val dst = IntBuffer.allocate(n*n)
      for (i <- 0 until n) {
        dst put src
      }
      dst.rewind()
      dst
    }

    private final def makeBuffer(src: Array[Short], n: Int): ShortBuffer = {
      val dst = ShortBuffer.allocate(n*n)
      for (i <- 0 until n) {
        dst put src
      }
      dst.rewind()
      dst
    }

  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    setFocusable(true)

    val N = 100
    val data8888 = new Array[Int](N)
    val data565 = new Array[Short](N)
    val data4444 = new Array[Short](N)

    makeRamp(premultiplyColor(Color.RED), premultiplyColor(Color.GREEN),
             N, data8888, data565, data4444)

    private val mBitmap1 = Bitmap.createBitmap(N, N, Bitmap.Config.ARGB_8888)
    private val mBitmap2 = Bitmap.createBitmap(N, N, Bitmap.Config.RGB_565)
    private val mBitmap3 = Bitmap.createBitmap(N, N, Bitmap.Config.ARGB_4444)

    mBitmap1 copyPixelsFromBuffer makeBuffer(data8888, N)
    mBitmap2 copyPixelsFromBuffer makeBuffer(data565, N)
    mBitmap3 copyPixelsFromBuffer makeBuffer(data4444, N)

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor 0xFFCCCCCC        

      var y = 10
      canvas.drawBitmap(mBitmap1, 10, y, null)
      y += mBitmap1.getHeight() + 10
      canvas.drawBitmap(mBitmap2, 10, y, null)
      y += mBitmap2.getHeight() + 10
      canvas.drawBitmap(mBitmap3, 10, y, null)
    }
  }
}

