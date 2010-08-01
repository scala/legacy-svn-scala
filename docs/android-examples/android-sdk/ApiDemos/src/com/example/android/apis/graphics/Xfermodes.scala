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

import android.app.Activity
import android.content.Context
import android.graphics.{Bitmap, BitmapShader, Canvas, Color, Matrix, Paint,
                         PorterDuff, PorterDuffXfermode, RectF, Shader, Xfermode}
import android.os.Bundle
import android.view.View

object Xfermodes {
  // create a bitmap with a circle, used for the "dst" image
  def makeDst(w: Int, h: Int): Bitmap = {
    val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = new Canvas(bm)
    val p = new Paint(Paint.ANTI_ALIAS_FLAG)

    p setColor 0xFFFFCC44
    c.drawOval(new RectF(0, 0, w*3/4, h*3/4), p)
    bm
  }

  // create a bitmap with a rect, used for the "src" image
  def makeSrc(w: Int, h: Int): Bitmap = {
    val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = new Canvas(bm)
    val p = new Paint(Paint.ANTI_ALIAS_FLAG)

    p setColor 0xFF66AAFF
    c.drawRect(w/3, h/3, w*19/20, h*19/20, p)
    bm
  }
}

class Xfermodes extends GraphicsActivity {
  import Xfermodes._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    private final val W = 64
    private final val H = 64
    private final val ROW_MAX = 4   // number of samples per row

    private final val sModes = Array(
      new PorterDuffXfermode(PorterDuff.Mode.CLEAR),
      new PorterDuffXfermode(PorterDuff.Mode.SRC),
      new PorterDuffXfermode(PorterDuff.Mode.DST),
      new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER),
      new PorterDuffXfermode(PorterDuff.Mode.DST_OVER),
      new PorterDuffXfermode(PorterDuff.Mode.SRC_IN),
      new PorterDuffXfermode(PorterDuff.Mode.DST_IN),
      new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT),
      new PorterDuffXfermode(PorterDuff.Mode.DST_OUT),
      new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP),
      new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP),
      new PorterDuffXfermode(PorterDuff.Mode.XOR),
      new PorterDuffXfermode(PorterDuff.Mode.DARKEN),
      new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN),
      new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY),
      new PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    )

    private final val sLabels = Array(
      "Clear", "Src", "Dst", "SrcOver",
      "DstOver", "SrcIn", "DstIn", "SrcOut",
      "DstOut", "SrcATop", "DstATop", "Xor",
      "Darken", "Lighten", "Multiply", "Screen"
    )
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    private val mSrcB = makeSrc(W, H)
    private val mDstB = makeDst(W, H)

    // make a ckeckerboard pattern
    val bm = Bitmap.createBitmap(Array(0xFFFFFFFF, 0xFFCCCCCC,
                                       0xFFCCCCCC, 0xFFFFFFFF), 2, 2,
                                 Bitmap.Config.RGB_565)
    // background checker-board pattern
    private val mBG = new BitmapShader(bm,
                                   Shader.TileMode.REPEAT,
                                   Shader.TileMode.REPEAT)
    val m = new Matrix
    m.setScale(6, 6)
    mBG setLocalMatrix m

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      val labelP = new Paint(Paint.ANTI_ALIAS_FLAG)
      labelP setTextAlign Paint.Align.CENTER

      val paint = new Paint
      paint setFilterBitmap false

      canvas.translate(15, 35)

      var x = 0
      var y = 0
      for (i <- 0 until sModes.length) {
        // draw the border
        paint setStyle Paint.Style.STROKE
        paint setShader null
        canvas.drawRect(x - 0.5f, y - 0.5f,
                        x + W + 0.5f, y + H + 0.5f, paint)

        // draw the checker-board pattern
        paint setStyle Paint.Style.FILL
        paint setShader mBG
        canvas.drawRect(x, y, x + W, y + H, paint)

        // draw the src/dst example into our offscreen bitmap
        val sc = canvas.saveLayer(x, y, x + W, y + H, null,
                                  Canvas.MATRIX_SAVE_FLAG |
                                  Canvas.CLIP_SAVE_FLAG |
                                  Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                                  Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                                  Canvas.CLIP_TO_LAYER_SAVE_FLAG)
        canvas.translate(x, y)
        canvas.drawBitmap(mDstB, 0, 0, paint)
        paint setXfermode sModes(i)
        canvas.drawBitmap(mSrcB, 0, 0, paint)
        paint setXfermode null
        canvas restoreToCount sc

        // draw the label
        canvas.drawText(sLabels(i),
                        x + W/2, y - labelP.getTextSize/2, labelP)

        x += W + 10

        // wrap around when we've drawn enough for one row
        if ((i % ROW_MAX) == ROW_MAX - 1) {
          x = 0
          y += H + 30
        }
      }
    }
  }
}

