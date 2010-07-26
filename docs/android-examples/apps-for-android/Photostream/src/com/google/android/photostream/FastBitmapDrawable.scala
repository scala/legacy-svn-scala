/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.android.photostream

import android.graphics.{Bitmap, Canvas, ColorFilter, PixelFormat}
import android.graphics.drawable.Drawable

class FastBitmapDrawable(mBitmap: Bitmap) extends Drawable {

  override def draw(canvas: Canvas) {
    canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null)
  }

  override def getOpacity: Int = PixelFormat.TRANSLUCENT

  override def setAlpha(alpha: Int) {}

  override def setColorFilter(cf: ColorFilter) {}

  override def getIntrinsicWidth: Int = mBitmap.getWidth

  override def getIntrinsicHeight: Int = mBitmap.getHeight

  override def getMinimumWidth: Int = mBitmap.getWidth

  override def getMinimumHeight: Int = mBitmap.getHeight

  def getBitmap: Bitmap = mBitmap

}
