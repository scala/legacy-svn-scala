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

import android.graphics.{Canvas, ColorFilter, PixelFormat}
import android.graphics.drawable.Drawable

class ProxyDrawable(target: Drawable) extends Drawable {

  private var mProxy = target
  private var mMutated: Boolean = _

  def getProxy: Drawable = mProxy

  def setProxy(proxy: Drawable) {
    if (proxy != this) {
      mProxy = proxy
    }
  }

  override def draw(canvas: Canvas) {
    if (mProxy != null) {
      mProxy draw canvas
    }
  }

  override def getIntrinsicWidth: Int = {
    if (mProxy != null) mProxy.getIntrinsicWidth else -1
  }

  override def getIntrinsicHeight: Int = {
    if (mProxy != null) mProxy.getIntrinsicHeight else -1
  }

  override def getOpacity: Int = {
    if (mProxy != null) mProxy.getOpacity else PixelFormat.TRANSPARENT
  }

  override def setFilterBitmap(filter: Boolean) {
    if (mProxy != null) {
      mProxy setFilterBitmap filter
    }
  }

  override def setDither(dither: Boolean) {
    if (mProxy != null) {
      mProxy setDither dither
    }
  }

  override def setColorFilter(colorFilter: ColorFilter) {
    if (mProxy != null) {
      mProxy setColorFilter colorFilter
    }
  }

  override def setAlpha(alpha: Int) {
    if (mProxy != null) {
      mProxy setAlpha alpha
    }
  }

  override def mutate(): Drawable = {
    if (mProxy != null && !mMutated && super.mutate() == this) {
      mProxy.mutate()
      mMutated = true
    }
    this
  }
}
    
