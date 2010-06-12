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

package com.example.android.apis.animation

import android.view.animation.{Animation, Transformation}
import android.graphics.{Camera, Matrix}

/**
 * An animation that rotates the view on the Y axis between two specified angles.
 * This animation also adds a translation on the Z axis (depth) to improve the effect.
 */
/**
   * Creates a new 3D rotation on the Y axis. The rotation is defined by its
   * start angle and its end angle. Both angles are in degrees. The rotation
   * is performed around a center point on the 2D space, definied by a pair
   * of X and Y coordinates, called centerX and centerY. When the animation
   * starts, a translation on the Z axis (depth) is performed. The length
   * of the translation can be specified, as well as whether the translation
   * should be reversed in time.
   *
   * @param fromDegrees the start angle of the 3D rotation
   * @param toDegrees the end angle of the 3D rotation
   * @param centerX the X center of the 3D rotation
   * @param centerY the Y center of the 3D rotation
   * @param reverse true if the translation should be reversed, false otherwise
   */
class Rotate3dAnimation(mFromDegrees: Float, mToDegrees: Float,
                        mCenterX: Float, mCenterY: Float,
                        mDepthZ: Float, mReverse: Boolean) extends Animation {
  private var mCamera: Camera = _

  override def initialize(width: Int, height: Int,
                          parentWidth: Int, parentHeight: Int) {
    super.initialize(width, height, parentWidth, parentHeight)
    mCamera = new Camera
  }

  override protected def applyTransformation(interpolatedTime: Float, t: Transformation) {
    val fromDegrees = mFromDegrees
    val degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

    val centerX = mCenterX
    val centerY = mCenterY
    val camera = mCamera

    val matrix = t.getMatrix

    camera.save()
    val z =
      if (mReverse) mDepthZ * interpolatedTime
      else mDepthZ * (1.0f - interpolatedTime)
    camera.translate(0.0f, 0.0f, z)

    camera.rotateY(degrees)
    camera.getMatrix(matrix)
    camera.restore()

    matrix.preTranslate(-centerX, -centerY)
    matrix.postTranslate(centerX, centerY)
  }
}
