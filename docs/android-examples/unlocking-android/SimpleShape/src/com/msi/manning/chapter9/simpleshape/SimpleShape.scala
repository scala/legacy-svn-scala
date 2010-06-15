/*
 * Copyright (C) 2009 Manning Publications Co.
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

package com.msi.manning.chapter9.simpleshape

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape

class SimpleShape extends Activity {
  import SimpleShape._  // companion object

  override protected def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(new SimpleView(this))
  }

}

object SimpleShape {

  private class SimpleView(context: Context) extends View(context) {
    private var mDrawable: ShapeDrawable = _

    setFocusable(true)
    mDrawable = new ShapeDrawable(new RectShape())
    mDrawable.getPaint setColor 0xFFFF0000

    override protected def onDraw(canvas: Canvas) {
      var x = 10
      var y = 10
      val width = 300
      val height = 50
      mDrawable.setBounds(x, y, x + width, y + height)
      mDrawable draw canvas
      y += height + 5
    }
  }
}
