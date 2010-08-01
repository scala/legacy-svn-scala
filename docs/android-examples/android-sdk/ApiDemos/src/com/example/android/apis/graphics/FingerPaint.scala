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

package com.example.android.apis.graphics

import android.app.Activity
import android.content.Context
import android.graphics._
import android.os.Bundle
import android.view.{Menu, MenuItem, MotionEvent, View}

class FingerPaint extends GraphicsActivity with OnColorChangedListener {    
  import FingerPaint._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new MyView(this))

    mPaint = new Paint()
    mPaint setAntiAlias true
    mPaint setDither true
    mPaint setColor 0xFFFF0000
    mPaint setStyle Paint.Style.STROKE
    mPaint setStrokeJoin Paint.Join.ROUND
    mPaint setStrokeCap Paint.Cap.ROUND
    mPaint setStrokeWidth 12

    mEmboss = new EmbossMaskFilter(Array[Float](1, 1, 1), 0.4f, 6, 3.5f)

    mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL)
  }

  private var mPaint: Paint = _
  private var mEmboss: MaskFilter = _
  private var mBlur: MaskFilter = _

  def colorChanged(color: Int) {
    mPaint setColor color
  }

  private object MyView {
    // unused
    //private final val MINP = 0.25f
    //private final val MAXP = 0.75f

    private final val TOUCH_TOLERANCE = 4
  }

  private class MyView(context: Context) extends View(context) {
    import MyView._  // companion object

    private val mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888)
    private val mCanvas = new Canvas(mBitmap)
    private val mPath = new Path()
    private val mBitmapPaint = new Paint(Paint.DITHER_FLAG)

    override protected def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
      super.onSizeChanged(w, h, oldw, oldh)
    }

    override protected def onDraw(canvas: Canvas) {
      canvas drawColor 0xFFAAAAAA
      canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint)
      canvas.drawPath(mPath, mPaint)
    }

    private var mX, mY: Float = _

    override def onTouchEvent(event: MotionEvent): Boolean = {
      def touch_start(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
      }
      def touch_move(x: Float, y: Float) {
        val dx = math.abs(x - mX)
        val dy = math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
          mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2)
          mX = x
          mY = y
        }
      }
      def touch_up() {
        mPath.lineTo(mX, mY)
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint)
        // kill this so we don't double draw
        mPath.reset()
      }
      val x = event.getX
      val y = event.getY

      event.getAction match {
        case MotionEvent.ACTION_DOWN =>
          touch_start(x, y)
          invalidate()
        case MotionEvent.ACTION_MOVE =>
          touch_move(x, y)
          invalidate()
        case MotionEvent.ACTION_UP =>
          touch_up()
          invalidate()
        case _ =>
      }
      true
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c')
    menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's')
    menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z')
    menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z')
    menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z')

    /****   Is this the mechanism to extend with filter effects?
    Intent intent = new Intent(null, getIntent().getData());
    intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
    menu.addIntentOptions(Menu.ALTERNATIVE, 0,
                          new ComponentName(this, NotesList.class),
                          null, intent, 0, null);
    *****/
    true
  }
    
  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    super.onPrepareOptionsMenu(menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    mPaint setXfermode null
    mPaint setAlpha 0xFF

    item.getItemId match {
      case COLOR_MENU_ID =>
        new ColorPickerDialog(this, this, mPaint.getColor()).show()
        true
      case EMBOSS_MENU_ID =>
        mPaint.setMaskFilter(
          if (mPaint.getMaskFilter != mEmboss) mEmboss else null)
        true
      case BLUR_MENU_ID =>
        mPaint.setMaskFilter(
          if (mPaint.getMaskFilter != mBlur) mBlur else null)
        true
      case ERASE_MENU_ID =>
        mPaint setXfermode new PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        true
      case SRCATOP_MENU_ID =>
        mPaint setXfermode new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        mPaint setAlpha 0x80
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }

}

object FingerPaint {
  private final val COLOR_MENU_ID = Menu.FIRST
  private final val EMBOSS_MENU_ID = Menu.FIRST + 1
  private final val BLUR_MENU_ID = Menu.FIRST + 2
  private final val ERASE_MENU_ID = Menu.FIRST + 3
  private final val SRCATOP_MENU_ID = Menu.FIRST + 4
}
