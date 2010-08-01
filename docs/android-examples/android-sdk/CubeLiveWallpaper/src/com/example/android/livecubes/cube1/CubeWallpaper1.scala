/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.livecubes.cube1

import android.graphics.{Canvas, Paint, Rect}
import android.os.{Handler, SystemClock}
import android.service.wallpaper.WallpaperService
import android.util.Log;
import android.view.{MotionEvent, SurfaceHolder}

/*
 * This animated wallpaper draws a rotating wireframe cube.
 */
class CubeWallpaper1 extends WallpaperService {

  private final val mHandler = new Handler

  override def onCreate() {
    super.onCreate()
  }

  override def onDestroy() {
    super.onDestroy()
  }

  override def onCreateEngine(): Engine = {
    new CubeEngine()
  }

  class CubeEngine extends Engine {

    // Create a Paint to draw the lines for our cube
    private final val mPaint = new Paint
    mPaint setColor 0xffffffff
    mPaint setAntiAlias true
    mPaint setStrokeWidth 2
    mPaint setStrokeCap Paint.Cap.ROUND
    mPaint setStyle Paint.Style.STROKE
    private var mOffset: Float = _
    private var mTouchX: Float = -1
    private var mTouchY: Float = -1
    private val mStartTime = SystemClock.elapsedRealtime
    private var mCenterX: Float = _
    private var mCenterY: Float = _

    private final val mDrawCube = new Runnable() {
      def run() {
        drawFrame()
      }
    }
    private var mVisible: Boolean = _

    override def onCreate(surfaceHolder: SurfaceHolder) {
      super.onCreate(surfaceHolder)

      // By default we don't get touch events, so enable them.
      setTouchEventsEnabled(true)
    }

    override def onDestroy() {
      super.onDestroy()
      mHandler removeCallbacks mDrawCube
    }

    override def onVisibilityChanged(visible: Boolean) {
      mVisible = visible
      if (visible)
        drawFrame()
      else
        mHandler removeCallbacks mDrawCube
    }

    override def onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
      super.onSurfaceChanged(holder, format, width, height)
      // store the center of the surface, so we can draw the cube in the right spot
      mCenterX = width / 2.0f
      mCenterY = height / 2.0f
      drawFrame()
    }

    override def onSurfaceCreated(holder: SurfaceHolder) {
      super.onSurfaceCreated(holder)
    }

    override def onSurfaceDestroyed(holder: SurfaceHolder) {
      super.onSurfaceDestroyed(holder)
      mVisible = false
      mHandler removeCallbacks mDrawCube
    }

    override def onOffsetsChanged(xOffset: Float, yOffset: Float,
                                  xStep: Float, yStep: Float,
                                  xPixels: Int, yPixels: Int) {
      mOffset = xOffset
      drawFrame()
    }

    /*
     * Store the position of the touch event so we can use it for drawing later
     */
    override def onTouchEvent(event: MotionEvent) {
      if (event.getAction == MotionEvent.ACTION_MOVE) {
        mTouchX = event.getX
        mTouchY = event.getY
      } else {
        mTouchX = -1
        mTouchY = -1
      }
      super.onTouchEvent(event)
    }

    /*
     * Draw one frame of the animation. This method gets called repeatedly
     * by posting a delayed Runnable. You can do any drawing you want in
     * here. This example draws a wireframe cube.
     */
    def drawFrame() {
      val holder = getSurfaceHolder

      var c: Canvas = null
      try {
        c = holder.lockCanvas()
        if (c != null) {
          // draw something
          drawCube(c)
          drawTouchPoint(c)
        }
      } finally {
        if (c != null) holder unlockCanvasAndPost c
      }

      // Reschedule the next redraw
      mHandler removeCallbacks mDrawCube
      if (mVisible) mHandler.postDelayed(mDrawCube, 1000 / 25)
    }

    /*
     * Draw a wireframe cube by drawing 12 3 dimensional lines between
     * adjacent corners of the cube
     */
    def drawCube(c: Canvas) {
      c.save()
      c.translate(mCenterX, mCenterY)
      c drawColor 0xff000000
      drawLine(c, -400, -400, -400,  400, -400, -400)
      drawLine(c,  400, -400, -400,  400,  400, -400)
      drawLine(c,  400,  400, -400, -400,  400, -400)
      drawLine(c, -400,  400, -400, -400, -400, -400)

      drawLine(c, -400, -400,  400,  400, -400,  400)
      drawLine(c,  400, -400,  400,  400,  400,  400)
      drawLine(c,  400,  400,  400, -400,  400,  400)
      drawLine(c, -400,  400,  400, -400, -400,  400)

      drawLine(c, -400, -400,  400, -400, -400, -400)
      drawLine(c,  400, -400,  400,  400, -400, -400)
      drawLine(c,  400,  400,  400,  400,  400, -400)
      drawLine(c, -400,  400,  400, -400,  400, -400)
      c.restore()
    }

    /*
     * Draw a 3 dimensional line on to the screen
     */
    def drawLine(c: Canvas, x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int) {
      val now = SystemClock.elapsedRealtime
      val xrot = (now - mStartTime) / 1000.0f
      val yrot = (0.5f - mOffset) * 2.0f
      val zrot = 0.0f

      // 3D transformations

      // rotation around X-axis
      val newy1 = (math.sin(xrot) * z1 + math.cos(xrot) * y1).toFloat
      val newy2 = (math.sin(xrot) * z2 + math.cos(xrot) * y2).toFloat
      var newz1 = (math.cos(xrot) * z1 - math.sin(xrot) * y1).toFloat
      var newz2 = (math.cos(xrot) * z2 - math.sin(xrot) * y2).toFloat

      // rotation around Y-axis
      val newx1 = (math.sin(yrot) * newz1 + math.cos(yrot) * x1).toFloat
      val newx2 = (math.sin(yrot) * newz2 + math.cos(yrot) * x2).toFloat
      newz1 = (math.cos(yrot) * newz1 - math.sin(yrot) * x1).toFloat
      newz2 = (math.cos(yrot) * newz2 - math.sin(yrot) * x2).toFloat

      // 3D-to-2D projection
      val startX = newx1 / (4 - newz1 / 400)
      val startY = newy1 / (4 - newz1 / 400)
      val stopX =  newx2 / (4 - newz2 / 400)
      val stopY =  newy2 / (4 - newz2 / 400)

      c.drawLine(startX, startY, stopX, stopY, mPaint)
    }

    /*
     * Draw a circle around the current touch point, if any.
     */
    def drawTouchPoint(c: Canvas) {
      if (mTouchX >=0 && mTouchY >= 0)
        c.drawCircle(mTouchX, mTouchY, 80, mPaint)
    }

  }
}
