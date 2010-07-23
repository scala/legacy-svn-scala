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

import scala.android.app.Activity

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.{ViewGroup, View}
import android.view.View.MeasureSpec
import android.view.animation.GridLayoutAnimationController

/**
 * A GridLayout positions its children in a static grid, defined by a fixed
 * number of rows and columns. The size of the rows and columns is dynamically
 * computed depending on the size of the GridLayout itself. As a result,
 * GridLayout children's layout parameters are ignored.
 *
 * The number of rows and columns are specified in XML using the attributes
 * android:numRows and android:numColumns.
 *
 * The GridLayout cannot be used when its size is unspecified.
 *
 * @attr ref com.google.android.photostream.R.styleable#GridLayout_numColumns
 * @attr ref com.google.android.photostream.R.styleable#GridLayout_numRows  
 */
class GridLayout(context: Context, attrs: AttributeSet, defStyle: Int)
extends ViewGroup(context, attrs, defStyle) {
  private var mNumColumns: Int = _
  private var mNumRows: Int = _

  private var mColumnWidth: Int = _
  private var mRowHeight: Int = _

  def this(context: Context, attrs: AttributeSet) =
    this(context, attrs, 0)

  def this(context: Context) =
    this(context, null, 0)

  init()

  private def init() {
    val a = context.obtainStyledAttributes(attrs, R.styleable.GridLayout, defStyle, 0)

    mNumColumns = a.getInt(R.styleable.GridLayout_numColumns, 1)
    mNumRows = a.getInt(R.styleable.GridLayout_numRows, 1)

    a.recycle()
  }

  override protected def attachLayoutAnimationParameters(child: View,
            params: ViewGroup.LayoutParams, index: Int, count: Int) {

    var animationParams =
      params.layoutAnimationParameters.asInstanceOf[GridLayoutAnimationController.AnimationParameters]

    if (animationParams == null) {
      animationParams = new GridLayoutAnimationController.AnimationParameters()
      params.layoutAnimationParameters = animationParams
    }

    animationParams.count = count
    animationParams.index = index
    animationParams.columnsCount = mNumColumns
    animationParams.rowsCount = mNumRows

    animationParams.column = index % mNumColumns
    animationParams.row = index / mNumColumns
  }

  override protected def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
    val widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec)

    val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
    val heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec)

    if (widthSpecMode == MeasureSpec.UNSPECIFIED ||
        heightSpecMode == MeasureSpec.UNSPECIFIED) {
      throw new RuntimeException("GridLayout cannot have UNSPECIFIED dimensions")
    }

    val width = widthSpecSize - getPaddingLeft - getPaddingRight
    val height = heightSpecSize - getPaddingTop - getPaddingBottom

    val columnWidth, mColumnWidth = width / mNumColumns
    val rowHeight, mRowHeight = height / mNumRows

    val count = getChildCount

    for (i <- 0 until count) {
      val child = getChildAt(i)

      val childWidthSpec = MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY)
      val childheightSpec = MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY)

      child.measure(childWidthSpec, childheightSpec)
    }

    setMeasuredDimension(widthSpecSize, heightSpecSize)
  }

  override protected def onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    val columns = mNumColumns
    val paddingLeft = getPaddingLeft
    val paddingTop = getPaddingTop
    val columnWidth = mColumnWidth
    val rowHeight = mRowHeight
    val count = getChildCount

    for (i <- 0 until count) {
      val child = getChildAt(i)
      if (child.getVisibility != View.GONE) {
        val column = i % columns
        val row = i / columns

        val childLeft = paddingLeft + column * columnWidth
        val childTop = paddingTop + row * rowHeight

        child.layout(childLeft, childTop,
                     childLeft + child.getMeasuredWidth,
                     childTop + child.getMeasuredHeight)
      }
    }
  }
}


