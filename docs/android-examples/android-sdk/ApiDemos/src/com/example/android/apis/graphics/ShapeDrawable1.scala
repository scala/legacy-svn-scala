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

import android.content.Context
import android.graphics._
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes._
import android.os.Bundle
import android.view.{KeyEvent, View}

class ShapeDrawable1 extends GraphicsActivity {
  import ShapeDrawable1._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

}

object ShapeDrawable1 {

  private class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    setFocusable(true)

    private val mDrawables = {
      val outerR = Array[Float](12, 12, 12, 12, 0, 0, 0, 0)
      val inset = new RectF(6, 6, 6, 6)
      val innerR = Array[Float](12, 12, 0, 0, 12, 12, 0, 0)
      val path = new Path()
      path.moveTo(50, 0)
      path.lineTo(0, 50)
      path.lineTo(50, 100)
      path.lineTo(100, 50)
      path.close()
      Array(
        new ShapeDrawable(new RectShape()),
        new ShapeDrawable(new OvalShape()),
        new ShapeDrawable(new RoundRectShape(outerR, null, null)),
        new ShapeDrawable(new RoundRectShape(outerR, inset, null)),
        new ShapeDrawable(new RoundRectShape(outerR, inset, innerR)),
        new ShapeDrawable(new PathShape(path, 100, 100)),
        new MyShapeDrawable(new ArcShape(45, -270))
      )
    }
    mDrawables(0).getPaint() setColor 0xFFFF0000
    mDrawables(1).getPaint() setColor 0xFF00FF00
    mDrawables(2).getPaint() setColor 0xFF0000FF
    mDrawables(3).getPaint() setShader makeSweep()
    mDrawables(4).getPaint() setShader makeLinear()
    mDrawables(5).getPaint() setShader makeTiling()
    mDrawables(6).getPaint() setColor 0x88FF8844

    mDrawables(3).getPaint setPathEffect {
      val pe = new DiscretePathEffect(10, 4)
      val pe2 = new CornerPathEffect(4)
      new ComposePathEffect(pe2, pe)
    }
    val msd = mDrawables(6).asInstanceOf[MyShapeDrawable]
    msd.getStrokePaint setStrokeWidth 4

    override protected def onDraw(canvas: Canvas) {
      val x = 10
      var y = 10
      val width = 300
      val height = 50

      for (dr <- mDrawables) {
        dr.setBounds(x, y, x + width, y + height)
        dr draw canvas
        y += height + 5
      }
    }
  }

  private object SampleView {
    private def makeSweep(): Shader =
      new SweepGradient(150, 25,
        Array(0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFF0000),
        null)

    private def makeLinear(): Shader =
      new LinearGradient(0, 0, 50, 50,
        Array(0xFFFF0000, 0xFF00FF00, 0xFF0000FF),
        null, Shader.TileMode.MIRROR)

    private def makeTiling(): Shader = {
      val pixels = Array(0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0)
      val bm = Bitmap.createBitmap(pixels, 2, 2, Bitmap.Config.ARGB_8888)
      new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private class MyShapeDrawable(s: Shape) extends ShapeDrawable(s) {
      private val mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG)
      mStrokePaint setStyle Paint.Style.STROKE
            
      def getStrokePaint: Paint = mStrokePaint
            
      override protected def onDraw(s: Shape, c: Canvas, p: Paint) {
        s.draw(c, p)
        s.draw(c, mStrokePaint)
      }
    }
  }

}

