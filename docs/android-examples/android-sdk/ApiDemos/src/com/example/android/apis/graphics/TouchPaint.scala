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
import android.graphics.{Bitmap, Canvas, Paint, Rect}
import android.os.{Bundle, Handler, Message}
import android.view.{Menu, MenuItem, MotionEvent, View}

//Need the following import to get access to the app resources, since this
//class is in a sub-package.


/**
 * Demonstrates the handling of touch screen and trackball events to
 * implement a simple painting app.
 */
object TouchPaint {
  /** Used as a pulse to gradually fade the contents of the window. */
  private final val FADE_MSG = 1

  /** Menu ID for the command to clear the window. */
  private final val CLEAR_ID = Menu.FIRST
  /** Menu ID for the command to toggle fading. */
  private final val FADE_ID = Menu.FIRST+1

  /** How often to fade the contents of the window (in ms). */
  private final val FADE_DELAY = 100
}

class TouchPaint extends GraphicsActivity {
  import TouchPaint._ // companion object

  /** The view responsible for drawing the window. */
  private var mView: MyView = _
  /** Is fading mode enabled? */
  private var mFading: Boolean = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Create and attach the view that is responsible for painting.
    mView = new MyView(this)
    setContentView(mView)
    mView.requestFocus()

    // Restore the fading option if we are being thawed from a
    // previously saved state.  Note that we are not currently remembering
    // the contents of the bitmap.
    mFading = if (savedInstanceState != null) savedInstanceState.getBoolean("fading", true) else true
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(0, CLEAR_ID, 0, "Clear")
    menu.add(0, FADE_ID, 0, "Fade") setCheckable true
    super.onCreateOptionsMenu(menu)
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    menu.findItem(FADE_ID) setChecked mFading
    super.onPrepareOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case CLEAR_ID =>
        mView.clear()
        true
      case FADE_ID =>
        mFading = !mFading
        if (mFading) {
          startFading()
        } else {
          stopFading()
        }
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }

  override protected def onResume() {
    super.onResume();
    // If fading mode is enabled, then as long as we are resumed we want
    // to run pulse to fade the contents.
    if (mFading) {
      startFading()
    }
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // Save away the fading state to restore if needed later.  Note that
    // we do not currently save the contents of the display.
    outState.putBoolean("fading", mFading)
  }

  override protected def onPause() {
    super.onPause()
    // Make sure to never run the fading pulse while we are paused or
    // stopped.
    stopFading()
  }

  /**
   * Start up the pulse to fade the screen, clearing any existing pulse to
   * ensure that we don't have multiple pulses running at a time.
   */
  def startFading() {
    mHandler removeMessages FADE_MSG
    mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_MSG), FADE_DELAY)
  }

  /**
   * Stop the pulse to fade the screen.
   */
  def stopFading() {
    mHandler.removeMessages(FADE_MSG)
  }

  private val mHandler: Handler = new Handler() {
    override def handleMessage(msg: Message) {
      msg.what match {
        // Upon receiving the fade pulse, we have the view perform a
        // fade and then enqueue a new message to pulse at the desired
        // next time.
        case FADE_MSG =>
          mView.fade()
          mHandler.sendMessageDelayed(
            mHandler.obtainMessage(FADE_MSG), FADE_DELAY)
        case _ =>
          super.handleMessage(msg)
      }
    }
  }

  object MyView {
    private final val FADE_ALPHA = 0x06
    private final val MAX_FADE_STEPS = 256/FADE_ALPHA + 4
  }

  class MyView(context: Context) extends View(context) {
    import MyView._  // companion object

    private var mBitmap: Bitmap = _
    private var mCanvas: Canvas = _
    private final val mRect = new Rect
    private final val mPaint = new Paint
    private final val mFadePaint = new Paint
    private var mCurDown: Boolean = _
    private var mCurX: Int = _
    private var mCurY: Int = _
    private var mCurPressure: Float = _
    private var mCurSize: Float = _
    private var mCurWidth: Int = _
    private var mFadeSteps = MAX_FADE_STEPS

    mPaint setAntiAlias true
    mPaint.setARGB(255, 255, 255, 255)
    mFadePaint setDither true
    mFadePaint.setARGB(FADE_ALPHA, 0, 0, 0)

    def clear() {
      if (mCanvas != null) {
        mPaint.setARGB(0xff, 0, 0, 0)
        mCanvas.drawPaint(mPaint)
        invalidate()
        mFadeSteps = MAX_FADE_STEPS
      }
    }

    def fade() {
      if (mCanvas != null && mFadeSteps < MAX_FADE_STEPS) {
        mCanvas.drawPaint(mFadePaint)
        invalidate()
        mFadeSteps += 1
      }
    }

    override protected def onSizeChanged(w: Int, h: Int,
                                         oldw: Int, oldh: Int) {
      var (curW, curH) =
        if (mBitmap != null) (mBitmap.getWidth, mBitmap.getHeight)
        else (0, 0)
      if (curW >= w && curH >= h) {
        return
      }

      if (curW < w) curW = w
      if (curH < h) curH = h

      val newBitmap = Bitmap.createBitmap(curW, curH,
                                          Bitmap.Config.RGB_565)
      val newCanvas = new Canvas
      newCanvas setBitmap newBitmap
      if (mBitmap != null) {
        newCanvas.drawBitmap(mBitmap, 0, 0, null)
      }
      mBitmap = newBitmap
      mCanvas = newCanvas
      mFadeSteps = MAX_FADE_STEPS
    }

    override protected def onDraw(canvas: Canvas) {
      if (mBitmap != null) {
        canvas.drawBitmap(mBitmap, 0, 0, null)
      }
    }

    override def onTrackballEvent(event: MotionEvent): Boolean = {
      var oldDown = mCurDown
      mCurDown = true
      val N = event.getHistorySize
      var baseX = mCurX;
      var baseY = mCurY;
      val scaleX = event.getXPrecision
      val scaleY = event.getYPrecision
      for (i <- 0 until N) {
        //Log.i("TouchPaint", "Intermediate trackball #" + i
        //        + ": x=" + event.getHistoricalX(i)
        //        + ", y=" + event.getHistoricalY(i));
        drawPoint(baseX+event.getHistoricalX(i)*scaleX,
                        baseY+event.getHistoricalY(i)*scaleY,
                        event.getHistoricalPressure(i),
                        event.getHistoricalSize(i))
      }
      //Log.i("TouchPaint", "Trackball: x=" + event.getX()
      //        + ", y=" + event.getY());
      drawPoint(baseX+event.getX*scaleX, baseY+event.getY*scaleY,
                event.getPressure, event.getSize)
      mCurDown = oldDown
      true
    }

    override def onTouchEvent(event: MotionEvent): Boolean = {
      val action = event.getAction
      mCurDown = action == MotionEvent.ACTION_DOWN ||
                 action == MotionEvent.ACTION_MOVE
      val N = event.getHistorySize
      for (i <- 0 until N) {
        //Log.i("TouchPaint", "Intermediate pointer #" + i);
        drawPoint(event.getHistoricalX(i), event.getHistoricalY(i),
                  event.getHistoricalPressure(i),
                  event.getHistoricalSize(i))
      }
      drawPoint(event.getX, event.getY, event.getPressure, event.getSize)
      true
    }

    private def drawPoint(x: Float, y: Float, pressure: Float, size: Float) {
      //Log.i("TouchPaint", "Drawing: " + x + "x" + y + " p="
      //        + pressure + " s=" + size)
      mCurX = x.toInt
      mCurY = y.toInt
      mCurPressure = pressure
      mCurSize = size
      mCurWidth = (mCurSize*(getWidth/3)).toInt
      if (mCurWidth < 1) mCurWidth = 1
      if (mCurDown && mBitmap != null) {
        val pressureLevel = (mCurPressure*255).toInt
        mPaint.setARGB(pressureLevel, 255, 255, 255)
        mCanvas.drawCircle(mCurX, mCurY, mCurWidth, mPaint)
        mRect.set(mCurX-mCurWidth-2, mCurY-mCurWidth-2,
                  mCurX+mCurWidth+2, mCurY+mCurWidth+2)
        invalidate(mRect)
      }
      mFadeSteps = 0
    }
  }
}
