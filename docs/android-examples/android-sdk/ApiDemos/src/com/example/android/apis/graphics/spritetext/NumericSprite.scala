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

package com.example.android.apis.graphics.spritetext

import javax.microedition.khronos.opengles.GL10

import android.graphics.Paint

class NumericSprite {
  import NumericSprite._  // companion object

  def initialize(gl: GL10, paint: Paint) {
    val height = roundUpPower2(paint.getFontSpacing.toInt)
    val interDigitGaps = 9 * 1.0f
    val width = roundUpPower2((interDigitGaps + paint.measureText(sStrike)).toInt)
    mLabelMaker = new LabelMaker(true, width, height)
    mLabelMaker initialize gl
    mLabelMaker beginAdding gl
    for (i <- 0 until mLabelId.length) {
      val digit = sStrike.substring(i, i+1)
      mLabelId(i) = mLabelMaker.add(gl, digit, paint)
      mWidth(i) = math.ceil(mLabelMaker getWidth i).toInt
    }
    mLabelMaker.endAdding(gl)
  }

  def shutdown(gl: GL10) {
    mLabelMaker.shutdown(gl)
    mLabelMaker = null
  }

  def setValue(value: Int) { mText = format(value) }

  def draw(gl: GL10, x0: Float, y: Float, viewWidth: Float, viewHeight: Float) {
    var x = x0
    mLabelMaker.beginDrawing(gl, viewWidth, viewHeight)
    for (i <- 0 until mText.length) {
      val c = mText charAt i
      val digit = c - '0'
      mLabelMaker.draw(gl, x, y, mLabelId(digit))
      x += mWidth(digit)
    }
    mLabelMaker endDrawing gl
  }

  def width: Float = {
    var width = 0.0f
    for (i <- 0 until mText.length) {
      val c = mText charAt i
      width += mWidth(c - '0')
    }
    width
  }

  private def format(value: Int) = Integer.toString(value)

  private var mLabelMaker: LabelMaker = _
  private var mText = ""
  private val mWidth = new Array[Int](10)
  private val mLabelId = new Array[Int](10)
}

object NumericSprite {
  private final val sStrike = "0123456789"

  /**
   * Find the smallest power of two >= the input value.
   * (Doesn't work for negative numbers.)
   */
  private def roundUpPower2(x0: Int): Int = {
    var x = x0 - 1
    x = x | (x >> 1)
    x = x | (x >> 2)
    x = x | (x >> 4)
    x = x | (x >> 8)
    x = x | (x >>16)
    x + 1
  }
}
