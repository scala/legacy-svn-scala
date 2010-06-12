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

package com.example.android.home

import android.content.{Context, Intent}
import android.content.res.TypedArray
import android.graphics.{Canvas, Rect}
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.{View, ViewGroup, LayoutInflater}
import android.widget.TextView

/**
 * The ApplicationsStackLayout is a specialized layout used for the purpose of
 * the home screen only. This layout stacks various icons in three distinct
 * areas: the recents, the favorites (or faves) and the button.
 *
 * This layout supports two different orientations: vertical and horizontal.
 *  When horizontal, the areas are laid out this way:
 *
 * [RECENTS][FAVES][BUTTON]
 *
 * When vertical, the layout is the following:
 *
 * [RECENTS]
 * [FAVES]
 * [BUTTON]
 *
 * The layout operates from the "bottom up" (or from right to left.) This means
 * that the button area will first be laid out, then the faves area, then the
 * recents. When there are too many favorites, the recents area is not displayed.
 *
 * The following attributes can be set in XML:
 * 
 * orientation: horizontal or vertical
 * marginLeft: the left margin of each element in the stack
 * marginTop: the top margin of each element in the stack
 * marginRight: the right margin of each element in the stack
 * marginBottom: the bottom margin of each element in the stack
 */
object ApplicationsStackLayout {
  final val HORIZONTAL = 0
  final val VERTICAL = 1
}

class ApplicationsStackLayout(context: Context, attrs: AttributeSet)
extends ViewGroup(context, attrs) with View.OnClickListener {
  import ApplicationsStackLayout._  // companion object

  private var mButton: View = _
  private var mInflater: LayoutInflater = _

  private var mFavoritesEnd: Int = _
  private var mFavoritesStart: Int = _

  private var mFavorites: List[ApplicationInfo] = _
  private var mRecents: List[ApplicationInfo] = _

  private var mOrientation = VERTICAL

  private var mMarginLeft: Int = _
  private var mMarginTop: Int = _
  private var mMarginRight: Int = _
  private var mMarginBottom: Int = _

  private var mDrawRect = new Rect

  private var mBackground: Drawable = _
  private var mIconSize: Int = _

  def this(context: Context) {
    this(context, null)
    initLayout()
  }

  val a: TypedArray =
    context.obtainStyledAttributes(attrs, R.styleable.ApplicationsStackLayout)

  mOrientation =
    a.getInt(R.styleable.ApplicationsStackLayout_stackOrientation, VERTICAL)

  mMarginLeft = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginLeft, 0)
  mMarginTop = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginTop, 0)
  mMarginRight = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginRight, 0)
  mMarginBottom = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginBottom, 0)

  a.recycle()

  mIconSize = 42 //(int) getResources.getDimension(android.R.dimen.app_icon_size)

  initLayout()

  private def initLayout() {
    mInflater = LayoutInflater.from(getContext())
    mButton = mInflater.inflate(R.layout.all_applications_button, this, false)
    addView(mButton)

    mBackground = getBackground()
    setBackgroundDrawable(null)
    setWillNotDraw(false)
  }

  /**
   * Return the current orientation, either VERTICAL (default) or HORIZONTAL.
   * 
   * @return the stack orientation
   */
  def getOrientation: Int = {
    mOrientation
  }

  override protected def onDraw(canvas: Canvas) {
    val background = mBackground

    val right = getWidth
    val bottom = getHeight

    // Draw behind recents
    if (mOrientation == VERTICAL) {
      mDrawRect.set(0, 0, right, mFavoritesStart)
    } else {
      mDrawRect.set(0, 0, mFavoritesStart, bottom)
    }
    background.setBounds(mDrawRect)
    background.draw(canvas)

    // Draw behind favorites
    if (mFavoritesStart > -1) {
      if (mOrientation == VERTICAL) {
        mDrawRect.set(0, mFavoritesStart, right, mFavoritesEnd)
      } else {
        mDrawRect.set(mFavoritesStart, 0, mFavoritesEnd, bottom)
      }
      background.setBounds(mDrawRect)
      background.draw(canvas)
    }

    super.onDraw(canvas)
  }

  override protected def onMeasure(widthMeasureSpec: Int,
                                   heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
    val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

    val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

    if (widthMode != View.MeasureSpec.EXACTLY || heightMode != View.MeasureSpec.EXACTLY) {
      throw new IllegalStateException(
        "ApplicationsStackLayout can only be used with " +
        "measure spec mode=EXACTLY")
    }

    setMeasuredDimension(widthSize, heightSize)
  }

  override protected def onLayout(changed: Boolean,
                                  l: Int, t: Int, r: Int, b: Int) {
    removeAllApplications()

    val layoutParams = mButton.getLayoutParams
    val widthSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.width, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY)
    mButton.measure(widthSpec, heightSpec)

    if (mOrientation == VERTICAL) {
      layoutVertical()
    } else {
      layoutHorizontal()
    }
  }

  private def layoutVertical() {
    val childLeft = 0
    var childTop = getHeight

    val childWidth = mButton.getMeasuredWidth
    val childHeight = mButton.getMeasuredHeight

    childTop -= childHeight + mMarginBottom
    mButton.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
    childTop -= mMarginTop
    mFavoritesEnd = childTop - mMarginBottom

    var oldChildTop = childTop
    childTop = stackApplications(mFavorites, childLeft, childTop)
    mFavoritesStart =
      if (childTop != oldChildTop) childTop + mMarginTop
      else -1

    stackApplications(mRecents, childLeft, childTop)
  }

  private def layoutHorizontal() {
    var childLeft = getWidth
    val childTop = 0

    val childWidth = mButton.getMeasuredWidth
    val childHeight = mButton.getMeasuredHeight

    childLeft -= childWidth;
    mButton.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
    childLeft -= mMarginLeft
    mFavoritesEnd = childLeft - mMarginRight

    val oldChildLeft = childLeft
    childLeft = stackApplications(mFavorites, childLeft, childTop)
    mFavoritesStart =
      if (childLeft != oldChildLeft) childLeft + mMarginLeft
     else -1

    stackApplications(mRecents, childLeft, childTop)
  }

  private def stackApplications(applications: List[ApplicationInfo],
                                childLeft: Int, childTop: Int): Int = {
    var childLeft1 = childLeft
    var childTop1 = childTop

    val isVertical = mOrientation == VERTICAL

    val count = applications.size
    var stop = false
    for (i <- count - 1 to 0 by -1 if !stop) {
      val info = applications(i)
      val view = createApplicationIcon(mInflater, this, info)

      val layoutParams = view.getLayoutParams
      val widthSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.width,
                                                   View.MeasureSpec.EXACTLY)
      val heightSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.height,
                                                    View.MeasureSpec.EXACTLY)
      view.measure(widthSpec, heightSpec)

      val childWidth = view.getMeasuredWidth
      val childHeight = view.getMeasuredHeight

      if (isVertical) {
        childTop1 -= childHeight + mMarginBottom

        if (childTop1 < 0) {
          childTop1 += childHeight + mMarginBottom
          stop = true
        }
      } else {
        childLeft1 -= childWidth + mMarginRight

        if (childLeft1 < 0) {
          childLeft1 += childWidth + mMarginRight
          stop = true
        }
      }//for

      addViewInLayout(view, -1, layoutParams)

      view.layout(childLeft1, childTop1, childLeft1 + childWidth,
                                         childTop1 + childHeight)

      if (isVertical) {
        childTop1 -= mMarginTop
      } else {
        childLeft1 -= mMarginLeft
      }
    }

    if (isVertical) childTop1 else childLeft1
  }

  private def removeAllApplications() {
    val count = getChildCount
    for (i <- count - 1 to 0 by -1) {
      val view: View = getChildAt(i)
      if (view != mButton) {
        removeViewAt(i)
      }
    }
  }

  private def createApplicationIcon(inflater: LayoutInflater,
      group: ViewGroup, info: ApplicationInfo): View = {

    val textView =
      inflater.inflate(R.layout.favorite, group, false).asInstanceOf[TextView]

    info.icon.setBounds(0, 0, mIconSize, mIconSize)
    textView.setCompoundDrawables(null, info.icon, null, null)
    textView setText info.title

    textView setTag info.intent
    textView setOnClickListener this

    textView
  }

  /**
   * Sets the list of favorites.
   *
   * @param applications the applications to put in the favorites area
   */
  def setFavorites(applications: List[ApplicationInfo]) {
    mFavorites = applications
    requestLayout()
  }

  /**
   * Sets the list of recents.
   *
   * @param applications the applications to put in the recents area
   */
  def setRecents(applications: List[ApplicationInfo]) {
    mRecents = applications
    requestLayout()
  }

  def onClick(v: View) {
    getContext startActivity v.getTag.asInstanceOf[Intent]
  }
}
