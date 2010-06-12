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

package com.example.android.apis.graphics.spritetext

import android.graphics.{Bitmap, Canvas , Paint, Rect}
import android.graphics.Paint.Style
import android.graphics.drawable.Drawable
import android.opengl.GLUtils

import scala.collection.mutable.ListBuffer

import javax.microedition.khronos.opengles.{GL10, GL11, GL11Ext}

/**
 * An OpenGL text label maker.
 *
 *
 * OpenGL labels are implemented by creating a Bitmap, drawing all the labels
 * into the Bitmap, converting the Bitmap into an Alpha texture, and creating a
 * mesh for each label
 *
 * The benefits of this approach are that the labels are drawn using the high
 * quality anti-aliased font rasterizer, full character set support, and all the
 * text labels are stored on a single texture, which makes it faster to use.
 *
 * The drawbacks are that you can only have as many labels as will fit onto one
 * texture, and you have to recreate the whole texture if any label text
 * changes.
 *
 */
class LabelMaker(fullColor: Boolean, strikeWidth: Int, strikeHeight: Int) {
  import LabelMaker._  // companion object
  /**
   * Create a label maker
   * or maximum compatibility with various OpenGL ES implementations,
   * the strike width and height must be powers of two,
   * We want the strike width to be at least as wide as the widest window.
   *
   * @param fullColor true if we want a full color backing store (4444),
   * otherwise we generate a grey L8 backing store.
   * @param strikeWidth width of strike
   * @param strikeHeight height of strike
   */
  mFullColor = fullColor
  mStrikeWidth = strikeWidth
  mStrikeHeight = strikeHeight
  mTexelWidth = (1.0 / mStrikeWidth).toFloat
  mTexelHeight = (1.0 / mStrikeHeight).toFloat
  mClearPaint = new Paint()
  mClearPaint.setARGB(0, 0, 0, 0)
  mClearPaint.setStyle(Style.FILL)
  mState = STATE_NEW

  /**
   * Call to initialize the class.
   * Call whenever the surface has been created.
   *
   * @param gl
   */
  def initialize(gl: GL10) {
    mState = STATE_INITIALIZED
    val textures = new Array[Int](1)
    gl.glGenTextures(1, textures, 0)
    mTextureID = textures(0)
    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)

    // Use Nearest for performance.
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST)
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_NEAREST)

    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE)
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE)

    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_REPLACE)
  }

  /**
   * Call when the surface has been destroyed
   */
  def shutdown(gl: GL10) {
    if (gl != null) {
      if (mState > STATE_NEW) {
        val textures = new Array[Int](1)
        textures(0) = mTextureID
        gl.glDeleteTextures(1, textures, 0)
        mState = STATE_NEW
      }
    }
  }

  /**
   * Call before adding labels. Clears out any existing labels.
   *
   * @param gl
   */
  def beginAdding(gl: GL10) {
    checkState(STATE_INITIALIZED, STATE_ADDING)
    mLabels.clear()
    mU = 0
    mV = 0
    mLineHeight = 0
    val config =
      if (mFullColor) Bitmap.Config.ARGB_4444
      else Bitmap.Config.ALPHA_8
    mBitmap = Bitmap.createBitmap(mStrikeWidth, mStrikeHeight, config)
    mCanvas = new Canvas(mBitmap)
    mBitmap.eraseColor(0)
  }

  /**
   * Call to add a label
   *
   * @param gl
   * @param text the text of the label
   * @param textPaint the paint of the label
   * @return the id of the label, used to measure and draw the label
   */
  def add(gl: GL10, text: String, textPaint: Paint): Int =
    add(gl, null, text, textPaint)

  /**
   * Call to add a label
   *
   * @param gl
   * @param text the text of the label
   * @param textPaint the paint of the label
   * @return the id of the label, used to measure and draw the label
   */
  def add(gl: GL10, background: Drawable, text: String, textPaint: Paint): Int =
    add(gl, background, text, textPaint, 0, 0)

  /**
   * Call to add a label
   * @return the id of the label, used to measure and draw the label
   */
  def add(gl: GL10, drawable: Drawable, minWidth: Int, minHeight: Int): Int =
    add(gl, drawable, null, null, minWidth, minHeight)

  /**
   * Call to add a label
   *
   * @param gl
   * @param text the text of the label
   * @param textPaint the paint of the label
   * @return the id of the label, used to measure and draw the label
   */
  def add(gl: GL10, background: Drawable, text: String, textPaint: Paint,
          minWidth0: Int, minHeight0: Int): Int = {
    checkState(STATE_ADDING, STATE_ADDING)
    val drawBackground = background != null
    val drawText = (text != null) && (textPaint != null)

    val padding = new Rect()
    var minWidth = minWidth0
    var minHeight = minHeight0
    if (drawBackground) {
      background.getPadding(padding)
      minWidth = math.max(minWidth, background.getMinimumWidth)
      minHeight = math.max(minHeight, background.getMinimumHeight)
    }

    var ascent = 0
    var descent = 0
    var measuredTextWidth = 0
    if (drawText) {
      // Paint.ascent is negative, so negate it.
      ascent = math.ceil(-textPaint.ascent()).toInt
      descent = math.ceil(textPaint.descent()).toInt
      measuredTextWidth = math.ceil(textPaint.measureText(text)).toInt
    }
    val textHeight = ascent + descent
    val textWidth = math.min(mStrikeWidth, measuredTextWidth)

    val padHeight = padding.top + padding.bottom
    val padWidth = padding.left + padding.right
    val height = math.max(minHeight, textHeight + padHeight)
    var width = math.max(minWidth, textWidth + padWidth)
    val effectiveTextHeight = height - padHeight
    val effectiveTextWidth = width - padWidth

    val centerOffsetHeight = (effectiveTextHeight - textHeight) / 2
    val centerOffsetWidth = (effectiveTextWidth - textWidth) / 2

    // Make changes to the local variables, only commit them
    // to the member variables after we've decided not to throw
    // any exceptions.

    var u = mU
    var v = mV
    var lineHeight = mLineHeight

    if (width > mStrikeWidth) {
      width = mStrikeWidth
    }

    // Is there room for this string on the current line?
    if (u + width > mStrikeWidth) {
      // No room, go to the next line:
      u = 0
      v += lineHeight
      lineHeight = 0
    }
    lineHeight = math.max(lineHeight, height)
    if (v + lineHeight > mStrikeHeight) {
      throw new IllegalArgumentException("Out of texture space.")
    }

    val u2 = u + width
    val vBase = v + ascent
    val v2 = v + height

    if (drawBackground) {
      background.setBounds(u, v, u + width, v + height)
      background.draw(mCanvas)
    }

    if (drawText) {
      mCanvas.drawText(text,
              u + padding.left + centerOffsetWidth,
              vBase + padding.top + centerOffsetHeight,
              textPaint);
    }

    val grid = new Grid(2, 2);
    // Grid.set arguments: i, j, x, y, z, u, v

    val texU = u * mTexelWidth
    val texU2 = u2 * mTexelWidth
    val texV = 1.0f - v * mTexelHeight
    val texV2 = 1.0f - v2 * mTexelHeight

    grid.set(0, 0,   0.0f,   0.0f, 0.0f, texU , texV2)
    grid.set(1, 0,  width,   0.0f, 0.0f, texU2, texV2)
    grid.set(0, 1,   0.0f, height, 0.0f, texU , texV )
    grid.set(1, 1,  width, height, 0.0f, texU2, texV )

        // We know there's enough space, so update the member variables
    mU = u + width
    mV = v
    mLineHeight = lineHeight
    mLabels append new Label(grid, width, height, ascent,
                             u, v + height, width, -height)
    mLabels.size - 1
  }

  /**
   * Call to end adding labels. Must be called before drawing starts.
   *
   * @param gl
   */
  def endAdding(gl: GL10) {
    checkState(STATE_ADDING, STATE_INITIALIZED)
    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0)
    // Reclaim storage used by bitmap and canvas.
    mBitmap.recycle()
    mBitmap = null
    mCanvas = null
  }

  /**
   * Get the width in pixels of a given label.
   *
   * @param labelID
   * @return the width in pixels
   */
  def getWidth(labelID: Int): Float =
    mLabels(labelID).width

  /**
   * Get the height in pixels of a given label.
   *
   * @param labelID
   * @return the height in pixels
   */
  def getHeight(labelID: Int): Float =
    mLabels(labelID).height

  /**
   * Get the baseline of a given label. That's how many pixels from the top of
   * the label to the text baseline. (This is equivalent to the negative of
   * the label's paint's ascent.)
   *
   * @param labelID
   * @return the baseline in pixels.
   */
   def getBaseline(labelID: Int): Float =
     mLabels(labelID).baseline

  /**
   * Begin drawing labels. Sets the OpenGL state for rapid drawing.
   *
   * @param gl
   * @param viewWidth
   * @param viewHeight
   */
  def beginDrawing(gl: GL10, viewWidth: Float, viewHeight: Float) {
    checkState(STATE_INITIALIZED, STATE_DRAWING)
    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
    gl.glShadeModel(GL10.GL_FLAT)
    gl.glEnable(GL10.GL_BLEND)
    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
    gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000)
    gl.glMatrixMode(GL10.GL_PROJECTION)
    gl.glPushMatrix()
    gl.glLoadIdentity()
    gl.glOrthof(0.0f, viewWidth, 0.0f, viewHeight, 0.0f, 1.0f)
    gl.glMatrixMode(GL10.GL_MODELVIEW)
    gl.glPushMatrix()
    gl.glLoadIdentity()
    // Magic offsets to promote consistent rasterization.
    gl.glTranslatef(0.375f, 0.375f, 0.0f)
  }

  /**
   * Draw a given label at a given x,y position, expressed in pixels, with the
   * lower-left-hand-corner of the view being (0,0).
   *
   * @param gl
   * @param x
   * @param y
   * @param labelID
   */
  def draw(gl: GL10, x: Float, y: Float, labelID: Int) {
    checkState(STATE_DRAWING, STATE_DRAWING)
    gl.glPushMatrix()
    val snappedX = math.floor(x).toFloat
    val snappedY = math.floor(y).toFloat
    gl.glTranslatef(snappedX, snappedY, 0.0f)
    val label = mLabels(labelID)
    gl.glEnable(GL10.GL_TEXTURE_2D)
    gl.asInstanceOf[GL11].glTexParameteriv(GL10.GL_TEXTURE_2D,
                GL11Ext.GL_TEXTURE_CROP_RECT_OES, label.mCrop, 0)
    gl.asInstanceOf[GL11Ext].glDrawTexiOES(
      snappedX.toInt, snappedY.toInt, 0, label.width.toInt, label.height.toInt)
    gl.glPopMatrix()
  }

  /**
   * Ends the drawing and restores the OpenGL state.
   *
   * @param gl
   */
  def endDrawing(gl: GL10) {
    checkState(STATE_DRAWING, STATE_INITIALIZED)
    gl.glDisable(GL10.GL_BLEND)
    gl.glMatrixMode(GL10.GL_PROJECTION)
    gl.glPopMatrix()
    gl.glMatrixMode(GL10.GL_MODELVIEW)
    gl.glPopMatrix()
  }

  private def checkState(oldState: Int, newState: Int) {
    if (mState != oldState) {
      throw new IllegalArgumentException("Can't call this method now.")
    }
    mState = newState
  }

  private var mStrikeWidth: Int = _
  private var mStrikeHeight: Int = _
  private var mFullColor: Boolean = _
  private var mBitmap: Bitmap = _
  private var mCanvas: Canvas = _
  private var mClearPaint: Paint = _

  private var mTextureID: Int = _

  private var mTexelWidth: Float = _  // Convert texel to U
  private var mTexelHeight: Float = _ // Convert texel to V
  private var mU: Int = _
  private var mV: Int = _
  private var mLineHeight: Int = _
  private val mLabels = new ListBuffer[Label]
}

object LabelMaker {
    private final val STATE_NEW = 0
    private final val STATE_INITIALIZED = 1
    private final val STATE_ADDING = 2
    private final val STATE_DRAWING = 3
    private var mState: Int = _

    private class Label(grid: Grid, val width: Float, val height: Float,
                        val baseline: Float,
                        cropU: Int, cropV: Int, cropW: Int, cropH: Int) {
      val mCrop = Array(cropU, cropV, cropW, cropH)
    }
}
