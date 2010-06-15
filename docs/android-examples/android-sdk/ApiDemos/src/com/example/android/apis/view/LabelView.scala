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

package com.example.android.apis.view

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.content.Context
import android.content.res.TypedArray
import android.graphics.{Canvas, Paint}
import android.util.AttributeSet
import android.view.View

/**
 * Example of how to write a custom subclass of View. LabelView
 * is used to draw simple text views. Note that it does not handle
 * styled text or right-to-left writing systems.
 *
 * Constructor.  This version is only needed if you will be instantiating
 * the object manually (not from a layout XML file).
 * @param context
 */
class LabelView(context: Context, attrs: AttributeSet) extends View(context, attrs) {
  import View._

  private var mTextPaint: Paint = _
  private var mText: String = _
  private var mAscent: Int = _

  def this(context: Context) = this(context, null)

  initLabelView()

  /**
   * Construct object, initializing with any attributes we understand from a
   * layout file. These attributes are defined in
   * SDK/assets/res/any/classes.xml.
   * 
   * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
   */
  if (attrs != null) {
    val a = context.obtainStyledAttributes(attrs, R.styleable.LabelView)

    val s = a.getString(R.styleable.LabelView_text)
    if (s != null) {
      setText(s.toString)
    }

    // Retrieve the color(s) to be used for this view and apply them.
    // Note, if you only care about supporting a single color, that you
    // can instead call a.getColor() and pass that to setTextColor().
    setTextColor(a.getColor(R.styleable.LabelView_textColor, 0xFF000000))

    val textSize = a.getDimensionPixelOffset(R.styleable.LabelView_textSize, 0)
    if (textSize > 0) {
      setTextSize(textSize)
    }

    a.recycle()
  }

  private final def initLabelView() {
    mTextPaint = new Paint()
    mTextPaint setAntiAlias true
    mTextPaint setTextSize 16
    mTextPaint setColor 0xFF000000
    setPadding(3, 3, 3, 3)
  }

  /**
   * Sets the text to display in this label
   * @param text The text to display. This will be drawn as one line.
   */
  def setText(text: String) {
    mText = text
    requestLayout()
    invalidate()
  }

  /**
   * Sets the text size for this label
   * @param size Font size
   */
  def setTextSize(size: Int) {
    mTextPaint setTextSize size
    requestLayout()
    invalidate()
  }

  /**
   * Sets the text color for this label.
   * @param color ARGB value for the text
   */
  def setTextColor(color: Int) {
    mTextPaint setColor color
    invalidate()
  }

  /**
   * @see android.view.View#measure(int, int)
   */
  override protected def onMeasure(widthMeasureSpec: Int,
                                   heightMeasureSpec: Int) {
    setMeasuredDimension(measureWidth(widthMeasureSpec),
                         measureHeight(heightMeasureSpec))
  }

  /**
   * Determines the width of this view
   * @param measureSpec A measureSpec packed into an int
   * @return The width of the view, honoring constraints from measureSpec
   */
  private def measureWidth(measureSpec: Int): Int = {
    var result = 0
    val specMode = MeasureSpec.getMode(measureSpec)
    val specSize = MeasureSpec.getSize(measureSpec)

    if (specMode == MeasureSpec.EXACTLY) {
      // We were told how big to be
      result = specSize
    } else {
      // Measure the text
      result = mTextPaint.measureText(mText).toInt +
               getPaddingLeft + getPaddingRight
      if (specMode == MeasureSpec.AT_MOST) {
        // Respect AT_MOST value if that was what is called for by measureSpec
        result = math.min(result, specSize);
      }
    }

    result
  }

  /**
   * Determines the height of this view
   * @param measureSpec A measureSpec packed into an int
   * @return The height of the view, honoring constraints from measureSpec
   */
  private def measureHeight(measureSpec: Int): Int = {
    var result = 0
    val specMode = MeasureSpec.getMode(measureSpec)
    val specSize = MeasureSpec.getSize(measureSpec)

    mAscent = mTextPaint.ascent.toInt
    if (specMode == MeasureSpec.EXACTLY) {
      // We were told how big to be
      result = specSize
    } else {
      // Measure the text (beware: ascent is a negative number)
      result = (-mAscent + mTextPaint.descent()).toInt +
               getPaddingTop + getPaddingBottom
      if (specMode == MeasureSpec.AT_MOST) {
        // Respect AT_MOST value if that was what is called for by measureSpec
        result = math.min(result, specSize)
      }
    }
    result
  }

  /**
   * Render the text
   * 
   * @see android.view.View#onDraw(android.graphics.Canvas)
   */
  override protected def onDraw(canvas: Canvas) {
    super.onDraw(canvas);
    canvas.drawText(mText, getPaddingLeft, getPaddingTop - mAscent, mTextPaint)
  }
}
