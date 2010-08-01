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

package com.example.android.livecubes.cube2

import android.content.SharedPreferences
import android.graphics.{Canvas, Paint, Rect}
import android.os.{Handler, SystemClock}
import android.service.wallpaper.WallpaperService
import android.view.{MotionEvent, SurfaceHolder}

/*
 * This animated wallpaper draws a rotating wireframe shape. It is similar to
 * example #1, but has a choice of 2 shapes, which are user selectable and
 * defined in resources instead of in code.
 */

object CubeWallpaper2 {

  final val SHARED_PREFS_NAME = "cube2settings"

  class ThreeDPoint(var x: Float, var y: Float, var z: Float) {
    def this() = this(0, 0, 0)
  }

  case class ThreeDLine(startPoint: Int, endPoint: Int)

}

class CubeWallpaper2 extends WallpaperService {

  override def onCreate() {
    super.onCreate()
  }

  override def onDestroy() {
    super.onDestroy()
  }

  override def onCreateEngine(): Engine = {
    new CubeEngine()
  }

  class CubeEngine extends Engine 
        with SharedPreferences.OnSharedPreferenceChangeListener {
    import CubeWallpaper2._  // companion object

    private final val mHandler = new Handler

    var mOriginalPoints: Array[ThreeDPoint] = _
    var mRotatedPoints: Array[ThreeDPoint] = _
    var mLines: Array[ThreeDLine] = _
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
    private val mPrefs = CubeWallpaper2.this.getSharedPreferences(SHARED_PREFS_NAME, 0)
    mPrefs registerOnSharedPreferenceChangeListener this
    onSharedPreferenceChanged(mPrefs, null)

    def onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
      val shape = prefs.getString("cube2_shape", "cube")

      // read the 3D model from the resource
      readModel(shape)
    }

    private def readModel(prefix: String) {
      // Read the model definition in from a resource.

      // get the resource identifiers for the arrays for the selected shape
      val pid = getResources.getIdentifier(prefix + "points", "array", getPackageName)
      val lid = getResources.getIdentifier(prefix + "lines", "array", getPackageName)

      val p = getResources.getStringArray(pid)
      val numpoints = p.length
      mOriginalPoints = new Array[ThreeDPoint](numpoints)
      mRotatedPoints = new Array[ThreeDPoint](numpoints)

      for (i <- 0 until numpoints) {
        val coord = p(i).split(" ")
        mOriginalPoints(i) = new ThreeDPoint(coord(0).toFloat, coord(1).toFloat, coord(2).toFloat)
        mRotatedPoints(i) = new ThreeDPoint
      }

      val l = getResources.getStringArray(lid)
      val numlines = l.length
      mLines = new Array[ThreeDLine](numlines)

      for (i <- 0 until numlines) {
        val idx = l(i).split(" ")
        mLines(i) = new ThreeDLine(idx(0).toInt, idx(1).toInt)
      }
    }

    override def onCreate(surfaceHolder: SurfaceHolder) {
      super.onCreate(surfaceHolder)
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

    override def onSurfaceChanged(holder: SurfaceHolder, format: Int,
                                  width: Int, height: Int) {
      super.onSurfaceChanged(holder, format, width, height)
      // store the center of the surface, so we can draw the cube in the right spot
      mCenterX = width / 2.0f
      mCenterY = height / 2.0f
      drawFrame()
    }

    override def onSurfaceCreated(holder: SurfaceHolder) {
      super.onSurfaceCreated(holder);
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
      val frame: Rect = holder.getSurfaceFrame
      val width = frame.width
      val height = frame.height

      var c: Canvas = null
      try {
        c = holder.lockCanvas()
        if (c != null) {
          // draw something
          drawCube(c)
          drawTouchPoint(c)
        }
      } finally {
        if (c != null) holder.unlockCanvasAndPost(c);
      }

      mHandler removeCallbacks mDrawCube
      if (mVisible)
        mHandler.postDelayed(mDrawCube, 1000 / 25)
    }

    def drawCube(c: Canvas) {
      c.save()
      c.translate(mCenterX, mCenterY)
      c drawColor 0xff000000

      val now = SystemClock.elapsedRealtime
      val xrot = (now - mStartTime) / 1000.0f
      val yrot = (0.5f - mOffset) * 2.0f
      rotateAndProjectPoints(xrot, yrot)
      drawLines(c)
      c.restore()
    }

    def rotateAndProjectPoints(xrot: Float, yrot: Float) {
      val n = mOriginalPoints.length
      for (i <- 0 until n) {
        // rotation around X-axis
        val p = mOriginalPoints(i)
        val x = p.x
        val y = p.y
        val z = p.z
        val newy = (math.sin(xrot) * z + math.cos(xrot) * y).toFloat
        var newz = (math.cos(xrot) * z - math.sin(xrot) * y).toFloat

        // rotation around Y-axis
        val newx = (math.sin(yrot) * newz + math.cos(yrot) * x).toFloat
        newz = (math.cos(yrot) * newz - math.sin(yrot) * x).toFloat

        // 3D-to-2D projection
        val screenX = newx / (4 - newz / 400)
        val screenY = newy / (4 - newz / 400)

        mRotatedPoints(i).x = screenX
        mRotatedPoints(i).y = screenY
        mRotatedPoints(i).z = 0
      }
    }

    def drawLines(c: Canvas) {
      val n = mLines.length
      for (i <- 0 until n) {
        val l = mLines(i)
        val start = mRotatedPoints(l.startPoint)
        val end = mRotatedPoints(l.endPoint)
        c.drawLine(start.x, start.y, end.x, end.y, mPaint)
      }
    }

    def drawTouchPoint(c: Canvas) {
      if (mTouchX >=0 && mTouchY >= 0) {
        c.drawCircle(mTouchX, mTouchY, 80, mPaint)
      }
    }
  }
}
