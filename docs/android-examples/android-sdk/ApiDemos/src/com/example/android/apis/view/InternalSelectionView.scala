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

import android.content.Context
import android.graphics.{Canvas, Color, Paint, Rect}
import android.util.AttributeSet
import android.view.{KeyEvent, View}
import android.view.View.MeasureSpec

/**
 * A view that has a known number of selectable rows, and maintains a notion of
 * which row is selected. The rows take up the entire width of the view.  The
 * height of the view is divided evenly among the rows.
 *
 * Notice what this view does to be a good citizen w.r.t its internal selection:
 * 1) calls {@link View#requestRectangleOnScreen} each time the selection
 *    changes due to internal navigation.
 * 2) overrides {@link View#getFocusedRect} by filling in the rectangle of the
 *    currently selected row
 * 3) overrides {@link View#onFocusChanged} and sets selection appropriately
 *    according to the previously focused rectangle.
 */
class InternalSelectionView(context: Context, numRows: Int,
                            label: String, attrs: AttributeSet)
extends View(context, attrs) {

  private val mPainter = new Paint()
  private val mTextPaint = new Paint()
  private val mTempRect = new Rect()

  private var mSelectedRow = 0
  private val mEstimatedPixelHeight = 10

  private var mDesiredHeight: Option[Int] = None

  def this(context: Context, numRows: Int, label: String) =
    this(context, numRows, label, null)

  def this(context: Context, numRows: Int) =
    this(context, numRows, "", null)

  def this(context: Context, attrs: AttributeSet) =
    this(context, 5, "", attrs)

  setFocusable(true)
  mTextPaint setAntiAlias true
  mTextPaint setTextSize 10
  mTextPaint setColor Color.WHITE

  def getNumRows: Int = numRows

  def getSelectedRow: Int = mSelectedRow

  def setDesiredHeight(desiredHeight: Int) {
    mDesiredHeight = Some(desiredHeight)
  }

  def getLabel: String = label

  override protected def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    setMeasuredDimension(
            measureWidth(widthMeasureSpec),
            measureHeight(heightMeasureSpec))
  }

  private def measureWidth(measureSpec: Int): Int = {
    val specMode = MeasureSpec.getMode(measureSpec)
    val specSize = MeasureSpec.getSize(measureSpec)

    val desiredWidth = 300 + getPaddingLeft + getPaddingRight
    if (specMode == MeasureSpec.EXACTLY) {
      // We were told how big to be
      specSize
    } else if (specMode == MeasureSpec.AT_MOST) {
      if (desiredWidth < specSize) desiredWidth else specSize
    } else {
      desiredWidth
    }
  }

  private def measureHeight(measureSpec: Int): Int = {
    val specMode = MeasureSpec.getMode(measureSpec)
    val specSize = MeasureSpec.getSize(measureSpec)

    val desiredHeight =
      if (!mDesiredHeight.isEmpty) mDesiredHeight.get
      else numRows * mEstimatedPixelHeight + getPaddingTop + getPaddingBottom
    if (specMode == MeasureSpec.EXACTLY) {
      // We were told how big to be
      specSize
    } else if (specMode == MeasureSpec.AT_MOST) {
      if (desiredHeight < specSize) desiredHeight else specSize
    } else {
      desiredHeight
    }
  }

  override protected def onDraw(canvas: Canvas) {
    val rowHeight = getRowHeight

    var rectTop = getPaddingTop
    val rectLeft = getPaddingLeft
    val rectRight = getWidth - getPaddingRight
    for (i <- 0 until numRows) {
      mPainter setColor Color.BLACK
      mPainter setAlpha 0x20

      // draw background rect
      mTempRect.set(rectLeft, rectTop, rectRight, rectTop + rowHeight)
      canvas.drawRect(mTempRect, mPainter)

      // draw forground rect
      if (i == mSelectedRow && hasFocus) {
        mPainter setColor Color.RED
        mPainter setAlpha 0xF0
        mTextPaint setAlpha 0xFF
      } else {
        mPainter setColor Color.BLACK
        mPainter setAlpha 0x40
        mTextPaint setAlpha 0xF0
      }
      mTempRect.set(rectLeft + 2, rectTop + 2,
                    rectRight - 2, rectTop + rowHeight - 2)
      canvas.drawRect(mTempRect, mPainter)

      // draw text to help when visually inspecting
      canvas.drawText(i.toString,
                      rectLeft + 2,
                      rectTop + 2 - mTextPaint.ascent.toInt,
                      mTextPaint)
      rectTop += rowHeight
    }
  }

  private def getRowHeight: Int =
    (getHeight - getPaddingTop - getPaddingBottom) / numRows

  def getRectForRow(rect: Rect, row: Int) {
    val rowHeight = getRowHeight
    val top = getPaddingTop + row * rowHeight
    rect.set(getPaddingLeft,
             top,
             getWidth - getPaddingRight,
             top + rowHeight)
  }

  def ensureRectVisible() {
    getRectForRow(mTempRect, mSelectedRow)
    requestRectangleOnScreen(mTempRect)
  }

  /* (non-Javadoc)
  * @see android.view.KeyEvent.Callback#onKeyDown(int, android.view.KeyEvent)
  */
  override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
    event.getKeyCode match {
      case KeyEvent.KEYCODE_DPAD_UP if mSelectedRow > 0 =>
        mSelectedRow -= 1
        invalidate()
        ensureRectVisible()
        true
      case KeyEvent.KEYCODE_DPAD_DOWN if mSelectedRow < (numRows - 1) =>
        mSelectedRow += 1
        invalidate()
        ensureRectVisible()
        true
      case _ =>
        false
    }

  override def getFocusedRect(r: Rect) {
    getRectForRow(r, mSelectedRow)
  }

  override protected def onFocusChanged(focused: Boolean, direction: Int,
                                        previouslyFocusedRect: Rect) {
    super.onFocusChanged(focused, direction, previouslyFocusedRect)

    if (focused) {
      direction match {
        case View.FOCUS_DOWN =>
          mSelectedRow = 0
        case View.FOCUS_UP =>
          mSelectedRow = numRows - 1
        case View.FOCUS_LEFT | View.FOCUS_RIGHT => // fall through
          // set the row that is closest to the rect
          if (previouslyFocusedRect != null) {
            val y = previouslyFocusedRect.top
                                + (previouslyFocusedRect.height / 2)
            val yPerRow = getHeight / numRows
            mSelectedRow = y / yPerRow
          } else {
            mSelectedRow = 0
          }
        case _ =>
          // can't gleam any useful information about what internal
          // selection should be...
          return
      }
      invalidate()
    }
  }

  override def toString: String =
    if (label != null) label else super.toString
}
