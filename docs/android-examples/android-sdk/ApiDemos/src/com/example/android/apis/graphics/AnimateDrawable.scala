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

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.animation.{Animation, AnimationUtils, Transformation}

class AnimateDrawable(target: Drawable, animation: Animation)
    extends ProxyDrawable(target) {

  private var mAnimation = animation
  private val mTransformation = new Transformation

  def this(target: Drawable) = this(target, null)

  def getAnimation = mAnimation

  def setAnimation(anim: Animation) { mAnimation = anim }

  def hasStarted: Boolean =
    mAnimation != null && mAnimation.hasStarted

  def hasEnded: Boolean =
    mAnimation == null || mAnimation.hasEnded

  override def draw(canvas: Canvas) {
    val dr: Drawable = getProxy
    if (dr != null) {
      val sc = canvas.save()
      val anim = mAnimation
      if (anim != null) {
        anim.getTransformation(
          AnimationUtils.currentAnimationTimeMillis, mTransformation)
        canvas concat mTransformation.getMatrix
      }
      dr draw canvas
      canvas restoreToCount sc
    }
  }
}
    
