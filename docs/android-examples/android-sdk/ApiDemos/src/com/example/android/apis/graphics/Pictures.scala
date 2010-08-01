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
import android.graphics.drawable.{Drawable, PictureDrawable}
import android.os.Bundle
import android.view.{KeyEvent, View}

import java.io._

class Pictures extends GraphicsActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new SampleView(this))
  }

  private object SampleView {
    def drawSomething(canvas: Canvas) {
      val p = new Paint(Paint.ANTI_ALIAS_FLAG)

      p setColor 0x88FF0000
      canvas.drawCircle(50, 50, 40, p)

      p setColor Color.GREEN
      p setTextSize 30
      canvas.drawText("Pictures", 60, 60, p)
    }
  }

  private /*static*/ class SampleView(context: Context) extends View(context) {
    import SampleView._  // companion object

    setFocusable(true)
    setFocusableInTouchMode(true)

    private val mPicture = new Picture
    drawSomething(mPicture.beginRecording(200, 100))
    mPicture.endRecording()

    private val mDrawable = new PictureDrawable(mPicture)

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor Color.WHITE

      canvas drawPicture mPicture

      canvas.drawPicture(mPicture, new RectF(0, 100, getWidth, 200))

      mDrawable.setBounds(0, 200, getWidth, 300)
      mDrawable draw canvas

      val os = new ByteArrayOutputStream
      mPicture writeToStream os
      val is = new ByteArrayInputStream(os.toByteArray)
      canvas.translate(0, 300)
      canvas drawPicture Picture.createFromStream(is)
    }
  }
}

