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

import android.graphics.{Bitmap, Canvas, Paint}

import scala.util.Random

/**
 * This class contains various utilities to manipulate Bitmaps. The methods of
 * this class, although static, are not thread safe and cannot be invoked by
 * several threads at the same time. Synchronization is required by the caller.
 */
object ImageUtilities {
  private final val PHOTO_BORDER_WIDTH = 3.0f
  private final val PHOTO_BORDER_COLOR = 0xffffffff

  private final val ROTATION_ANGLE_MIN = 2.5f
  private final val ROTATION_ANGLE_EXTRA = 5.5f

  private final val sRandom = new Random()
  private final val sPaint =
    new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG)
  private final val sStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG)

  sStrokePaint setStrokeWidth PHOTO_BORDER_WIDTH
  sStrokePaint setStyle Paint.Style.STROKE
  sStrokePaint setColor PHOTO_BORDER_COLOR

  /**
   * Rotate specified Bitmap by a random angle. The angle is either negative or
   * positive, and ranges, in degrees, from 2.5 to 8. After rotation a frame is
   * overlaid on top of the rotated image.
   *
   * This method is not thread safe.
   *
   * @param bitmap The Bitmap to rotate and apply a frame onto.
   *
   * @return A new Bitmap whose dimension are different from the original bitmap.
   */
  def rotateAndFrame(bitmap: Bitmap): Bitmap = {
    val positive = sRandom.nextFloat >= 0.5f
    val angle =
      (ROTATION_ANGLE_MIN + sRandom.nextFloat * ROTATION_ANGLE_EXTRA) *
      (if (positive) 1.0f else -1.0f)
    val radAngle = math.toRadians(angle)

    val bitmapWidth = bitmap.getWidth
    val bitmapHeight = bitmap.getHeight

    val cosAngle = math.abs(math.cos(radAngle))
    val sinAngle = math.abs(math.sin(radAngle))

    val strokedWidth = (bitmapWidth + 2 * PHOTO_BORDER_WIDTH).toInt
    val strokedHeight = (bitmapHeight + 2 * PHOTO_BORDER_WIDTH).toInt

    val width = (strokedHeight * sinAngle + strokedWidth * cosAngle).toInt
    val height = (strokedWidth * sinAngle + strokedHeight * cosAngle).toInt

    val x = (width - bitmapWidth) / 2.0f
    val y = (height - bitmapHeight) / 2.0f

    val decored = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = new Canvas(decored)

    canvas.rotate(angle, width / 2.0f, height / 2.0f)
    canvas.drawBitmap(bitmap, x, y, sPaint)
    canvas.drawRect(x, y, x + bitmapWidth, y + bitmapHeight, sStrokePaint)

    decored
   }

  /**
   * Scales the specified Bitmap to fit within the specified dimensions. After
   * scaling, a frame is overlaid on top of the scaled image.
   *
   * This method is not thread safe.
   *
   * @param bitmap The Bitmap to scale to fit the specified dimensions and to
   *               apply a frame onto.
   * @param width The maximum width of the new Bitmap.
   * @param height The maximum height of the new Bitmap.
   *
   * @return A scaled version of the original bitmap, whose dimension are less
   *         than or equal to the specified width and height.
   */
  def scaleAndFrame(bitmap: Bitmap, width: Int, height: Int): Bitmap = {
    val bitmapWidth = bitmap.getWidth
    val bitmapHeight = bitmap.getHeight

    val scale = math.min(width.toFloat / bitmapWidth, 
                         height.toFloat / bitmapHeight)

    val scaledWidth = (bitmapWidth * scale).toInt
    val scaledHeight = (bitmapHeight * scale).toInt

    val decored =
      Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    val canvas = new Canvas(decored)

    val offset = (PHOTO_BORDER_WIDTH / 2).toInt
    sStrokePaint setAntiAlias false
    canvas.drawRect(offset, offset, scaledWidth - offset - 1,
                    scaledHeight - offset - 1, sStrokePaint)
    sStrokePaint setAntiAlias true

    decored
  }
}
