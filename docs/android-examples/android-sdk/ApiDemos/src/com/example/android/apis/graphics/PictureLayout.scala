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

import android.content.Context
import android.graphics.{Canvas, Picture, Rect}
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.{View, ViewGroup, ViewParent}
import android.view.ViewGroup.LayoutParams

class PictureLayout(context: Context, attrs: AttributeSet)
    extends ViewGroup(context, attrs) {
  private final val mPicture = new Picture()

  def this(context: Context) = this(context, null)

  override def addView(child: View) {
    if (getChildCount > 1) {
      throw new IllegalStateException(
        "PictureLayout can host only one direct child")
    }
    super.addView(child)
  }

  override def addView(child: View, index: Int) {
    if (getChildCount > 1) {
      throw new IllegalStateException(
        "PictureLayout can host only one direct child")
    }
    super.addView(child, index)
  }

  override def addView(child: View, params: LayoutParams) {
    if (getChildCount > 1) {
      throw new IllegalStateException(
        "PictureLayout can host only one direct child")
    }
    super.addView(child, params)
  }

  override def addView(child: View, index: Int, params: LayoutParams) {
    if (getChildCount > 1) {
      throw new IllegalStateException(
        "PictureLayout can host only one direct child")
    }
    super.addView(child, index, params)
  }

  override protected def generateDefaultLayoutParams: LayoutParams =
    new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)

  override protected def onMeasure(widthMeasureSpec: Int,
                                   heightMeasureSpec: Int) {
    val count = getChildCount

    var maxHeight = 0
    var maxWidth = 0

    for (i <- 0 until count) {
      val child = getChildAt(i)
      if (child.getVisibility != View.GONE) {
         measureChild(child, widthMeasureSpec, heightMeasureSpec)
      }
    }

    maxWidth += getPaddingLeft + getPaddingRight
    maxHeight += getPaddingTop + getPaddingBottom

    val drawable = getBackground
    if (drawable != null) {
      maxHeight = math.max(maxHeight, drawable.getMinimumHeight)
      maxWidth = math.max(maxWidth, drawable.getMinimumWidth)
    }

    setMeasuredDimension(View.resolveSize(maxWidth, widthMeasureSpec),
                         View.resolveSize(maxHeight, heightMeasureSpec))
  }

  private def drawPict(canvas: Canvas, x: Int, y: Int, w: Int, h: Int,
                       sx: Float, sy: Float) {
    canvas.save()
    canvas.translate(x, y)
    canvas.clipRect(0, 0, w, h)
    canvas.scale(0.5f, 0.5f)
    canvas.scale(sx, sy, w, h)
    canvas drawPicture mPicture
    canvas.restore()
  }

  override protected def dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(mPicture.beginRecording(getWidth, getHeight))
    mPicture.endRecording()

    val x = getWidth / 2
    val y = getHeight / 2

    if (false) {
      canvas drawPicture mPicture
    } else {
      drawPict(canvas, 0, 0, x, y,  1,  1)
      drawPict(canvas, x, 0, x, y, -1,  1)
      drawPict(canvas, 0, y, x, y,  1, -1)
      drawPict(canvas, x, y, x, y, -1, -1)
    }
  }

  override def invalidateChildInParent(location: Array[Int],
                                       dirty: Rect): ViewParent = {
    location(0) = getLeft
    location(1) = getTop
    dirty.set(0, 0, getWidth, getHeight)
    getParent
  }

  override protected def onLayout(changed: Boolean,
                                  l: Int, t: Int, r: Int, b: Int) {
    val count = super.getChildCount

    for (i <- 0 until count) {
      val child = getChildAt(i)
      if (child.getVisibility != View.GONE) {
        val childLeft = getPaddingLeft
        val childTop = getPaddingTop
        child.layout(childLeft, childTop,
                     childLeft + child.getMeasuredWidth,
                     childTop + child.getMeasuredHeight)

      }
    }
  }
}
