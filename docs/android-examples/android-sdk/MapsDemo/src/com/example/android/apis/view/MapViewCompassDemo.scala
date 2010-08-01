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

import com.google.android.maps.{MapActivity, MapView, MyLocationOverlay}

import android.content.Context
import android.graphics.{Bitmap, Canvas, DrawFilter, Matrix, Paint, Path, Picture, PorterDuff, Rect, RectF, Region}
import android.hardware.{Sensor, SensorEvent, SensorEventListener, SensorManager}
import android.os.Bundle
import android.view.{MotionEvent, View, ViewGroup}
import android.view.View.MeasureSpec

import java.util.List
import javax.microedition.khronos.opengles.GL

/**
 * Example of how to use an {@link com.google.android.maps.MapView}
 * in conjunction with the {@link com.hardware.SensorManager}
 * <h3>MapViewCompassDemo</h3>

<p>This demonstrates creating a Map based Activity.</p>

<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com.example.android.apis/view/MapViewCompassDemo.java</td>
 *             <td >The Alert Dialog Samples implementation</td>
 *         </tr>
 * </table>
 */
class MapViewCompassDemo extends MapActivity {
  import MapViewCompassDemo._  // companion object

  private var mSensorManager: SensorManager = _
  private var mRotateView: RotateView = _
  private var mMapView: MapView = _
  private var mMyLocationOverlay: MyLocationOverlay = _

  private class RotateView(context: Context) extends ViewGroup(context)
                                                with SensorEventListener {
    private val mCanvas = new SmoothCanvas()
    private var mHeading = 0f

    def onSensorChanged(event: SensorEvent) {
      val values = event.values;
      //Log.d(TAG, "x: " + values(0) + "y: " + values(1) + "z: " + values(2))
      this synchronized {
        mHeading = values(0)
        invalidate()
      }
    }

    override protected def dispatchDraw(canvas: Canvas) {
      canvas save Canvas.MATRIX_SAVE_FLAG
      canvas.rotate(-mHeading, getWidth * 0.5f, getHeight * 0.5f)
      mCanvas.delegate = canvas
      super.dispatchDraw(mCanvas)
      canvas.restore()
    }

    override protected def onLayout(changed: Boolean,
                                    l: Int, t: Int, r: Int, b: Int) {
      val width = getWidth
      val height = getHeight
      val count = getChildCount
      for (i <- 0 until count) {
        val view = getChildAt(i)
        val childWidth = view.getMeasuredWidth
        val childHeight = view.getMeasuredHeight
        val childLeft = (width - childWidth) / 2
        val childTop = (height - childHeight) / 2
        view.layout(childLeft, childTop, childLeft + childWidth,
                                         childTop + childHeight)
      }
    }

    override protected def onMeasure(widthMeasureSpec: Int,
                                     heightMeasureSpec: Int) {
      val w = View.getDefaultSize(getSuggestedMinimumWidth, widthMeasureSpec)
      val h = View.getDefaultSize(getSuggestedMinimumHeight, heightMeasureSpec)
      val sizeSpec = {
        val x = (if (w > h) w else h).toInt
        MeasureSpec.makeMeasureSpec(x, MeasureSpec.EXACTLY)
      }
      val count = getChildCount
      for (i <- 0 until count) {
        getChildAt(i).measure(sizeSpec, sizeSpec)
      }
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override def dispatchTouchEvent(event: MotionEvent): Boolean = {
      // TODO: rotate events too
      super.dispatchTouchEvent(event)
    }

    def onAccuracyChanged(sensor: Sensor, accuracy: Int) {
      // TODO Auto-generated method stub   
    }
  }

  private object RotateView {
    private final val SQ2 = 1.414213562373095f
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    mSensorManager =
      getSystemService(Context.SENSOR_SERVICE).asInstanceOf[SensorManager]
    mRotateView = new RotateView(this)
    mMapView = new MapView(this, "MapViewCompassDemo_DummyAPIKey")
    mRotateView addView mMapView
    setContentView(mRotateView)

    mMyLocationOverlay = new MyLocationOverlay(this, mMapView)
    mMyLocationOverlay runOnFirstFix new Runnable() {
      def run() {
        mMapView.getController animateTo mMyLocationOverlay.getMyLocation
      }
    }
    mMapView.getOverlays add mMyLocationOverlay
    mMapView.getController setZoom 18
    mMapView setClickable true
    mMapView setEnabled true
  }

  override protected def onResume() {
    super.onResume()
    mSensorManager.registerListener(mRotateView,
      mSensorManager.getDefaultSensor(SensorManager.SENSOR_ORIENTATION),
      SensorManager.SENSOR_DELAY_UI)
    mMyLocationOverlay.enableMyLocation()
  }

  override protected def onStop() {
    mSensorManager.unregisterListener(mRotateView)
    mMyLocationOverlay.disableMyLocation()
    super.onStop()
  }

  override protected def isRouteDisplayed: Boolean = false
}

object MapViewCompassDemo {

  private final val TAG = "MapViewCompassDemo"

  final class SmoothCanvas extends Canvas {
    var delegate: Canvas = _

    private final val mSmooth = new Paint(Paint.FILTER_BITMAP_FLAG)

    override def setBitmap(bitmap: Bitmap) {
      delegate setBitmap bitmap
    }

    override def setViewport(width: Int, height: Int) {
      delegate.setViewport(width, height)
    }

    override def isOpaque: Boolean = delegate.isOpaque

    override def getWidth: Int = delegate.getWidth

    override def getHeight: Int = delegate.getHeight

    override def save: Int = delegate.save

    override def save(saveFlags: Int): Int = delegate save saveFlags 

    override def saveLayer(bounds: RectF, paint: Paint, saveFlags: Int): Int =
      delegate.saveLayer(bounds, paint, saveFlags)

    override def saveLayer(left: Float, top: Float, right: Float, bottom: Float,
                           paint: Paint, saveFlags: Int): Int =
      delegate.saveLayer(left, top, right, bottom, paint, saveFlags)

    override def saveLayerAlpha(bounds: RectF, alpha: Int, saveFlags: Int): Int =
      delegate.saveLayerAlpha(bounds, alpha, saveFlags)

    override def saveLayerAlpha(left: Float, top: Float, right: Float, bottom: Float,
                                alpha: Int, saveFlags: Int): Int =
      delegate.saveLayerAlpha(left, top, right, bottom, alpha, saveFlags)

    override def restore() {
      delegate.restore()
    }

    override def getSaveCount: Int = delegate.getSaveCount

    override def restoreToCount(saveCount: Int) {
      delegate.restoreToCount(saveCount)
    }

    override def translate(dx: Float, dy: Float) {
      delegate.translate(dx, dy)
    }

    override def scale(sx: Float, sy: Float) {
      delegate.scale(sx, sy)
    }

    override def rotate(degrees: Float) {
      delegate.rotate(degrees)
    }

    override def skew(sx: Float, sy: Float) {
      delegate.skew(sx, sy)
    }

    override def concat(matrix: Matrix) {
      delegate.concat(matrix)
    }

    override def setMatrix(matrix: Matrix) {
      delegate.setMatrix(matrix)
    }

    override def getMatrix(ctm: Matrix) {
      delegate.getMatrix(ctm)
    }

    override def clipRect(rect: RectF, op: Region.Op): Boolean =
      delegate.clipRect(rect, op)

    override def clipRect(rect: Rect, op: Region.Op): Boolean =
      delegate.clipRect(rect, op)

    override def clipRect(rect: RectF): Boolean =
      delegate.clipRect(rect)

    override def clipRect(rect: Rect): Boolean =
      delegate.clipRect(rect)

    override def clipRect(left: Float, top: Float, right: Float,
                          bottom: Float, op: Region.Op): Boolean =
      delegate.clipRect(left, top, right, bottom, op)

    override def clipRect(left: Float, top: Float,
                          right: Float, bottom: Float): Boolean =
      delegate.clipRect(left, top, right, bottom)

    override def clipRect(left: Int, top: Int, right: Int, bottom: Int): Boolean =
      delegate.clipRect(left, top, right, bottom)

    override def clipPath(path: Path, op: Region.Op): Boolean =
      delegate.clipPath(path, op)

    override def clipPath(path: Path): Boolean = delegate.clipPath(path)

    override def clipRegion(region: Region, op: Region.Op): Boolean =
      delegate.clipRegion(region, op)

    override def clipRegion(region: Region): Boolean =
      delegate.clipRegion(region)

    override def getDrawFilter: DrawFilter = delegate.getDrawFilter

    override def setDrawFilter(filter: DrawFilter) {
      delegate setDrawFilter filter
    }

    override def getGL: GL = delegate.getGL

    override def quickReject(rect: RectF, typ: Canvas.EdgeType): Boolean =
      delegate.quickReject(rect, typ)

    override def quickReject(path: Path, typ: Canvas.EdgeType): Boolean =
      delegate.quickReject(path, typ)

    override def quickReject(left: Float, top: Float, right: Float,
                             bottom: Float, typ: Canvas.EdgeType): Boolean =
      delegate.quickReject(left, top, right, bottom, typ)

    override def getClipBounds(bounds: Rect): Boolean =
      delegate.getClipBounds(bounds)

    override def drawRGB(r: Int, g: Int, b: Int) {
      delegate.drawRGB(r, g, b)
    }

    override def drawARGB(a: Int, r: Int, g: Int, b: Int) {
      delegate.drawARGB(a, r, g, b)
    }

    override def drawColor(color: Int) {
      delegate.drawColor(color)
    }

    override def drawColor(color: Int, mode: PorterDuff.Mode) {
      delegate.drawColor(color, mode)
    }

    override def drawPaint(paint: Paint) {
      delegate.drawPaint(paint)
    }

    override def drawPoints(pts: Array[Float], offset: Int, count: Int, paint: Paint) {
      delegate.drawPoints(pts, offset, count, paint)
    }

    override def drawPoints(pts: Array[Float], paint: Paint) {
      delegate.drawPoints(pts, paint)
    }

    override def drawPoint(x: Float, y: Float, paint: Paint) {
      delegate.drawPoint(x, y, paint)
    }

    override def drawLine(startX: Float, startY: Float, stopX: Float,
                          stopY: Float, paint: Paint) {
      delegate.drawLine(startX, startY, stopX, stopY, paint)
    }

    override def drawLines(pts: Array[Float], offset: Int, count: Int, paint: Paint) {
      delegate.drawLines(pts, offset, count, paint)
    }

    override def drawLines(pts: Array[Float], paint: Paint) {
      delegate.drawLines(pts, paint)
    }

    override def drawRect(rect: RectF, paint: Paint) {
      delegate.drawRect(rect, paint)
    }

    override def drawRect(r: Rect, paint: Paint) {
      delegate.drawRect(r, paint);
    }

    override def drawRect(left: Float, top: Float, right: Float, bottom: Float,
                          paint: Paint) {
      delegate.drawRect(left, top, right, bottom, paint);
    }

    override def drawOval(oval: RectF, paint: Paint) {
      delegate.drawOval(oval, paint)
    }

    override def drawCircle(cx: Float, cy: Float, radius: Float, paint: Paint) {
      delegate.drawCircle(cx, cy, radius, paint)
    }

    override def drawArc(oval: RectF, startAngle: Float, sweepAngle: Float,
                         useCenter: Boolean, paint: Paint) {
      delegate.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
    }

    override def drawRoundRect(rect: RectF, rx: Float, ry: Float, paint: Paint) {
      delegate.drawRoundRect(rect, rx, ry, paint)
    }

    override def drawPath(path: Path, paint: Paint) {
      delegate.drawPath(path, paint)
    }

    override def drawBitmap(bitmap: Bitmap, left: Float, top: Float, paint: Paint) {
      val p = if (paint == null)
        mSmooth
      else {
        paint setFilterBitmap true
        paint
      }
      delegate.drawBitmap(bitmap, left, top, p)
    }

    override def drawBitmap(bitmap: Bitmap, src: Rect, dst: RectF, paint: Paint) {
      val p = if (paint == null)
        mSmooth
      else {
        paint setFilterBitmap true
        paint
      }
      delegate.drawBitmap(bitmap, src, dst, p)
    }

    override def drawBitmap(bitmap: Bitmap, src: Rect, dst: Rect, paint: Paint) {
      val p = if (paint == null) 
        mSmooth
      else {
        paint setFilterBitmap true
        paint
      }
      delegate.drawBitmap(bitmap, src, dst, p)
    }

    override def drawBitmap(colors: Array[Int], offset: Int, stride: Int,
                            x: Int, y: Int, width: Int, height: Int,
                            hasAlpha: Boolean, paint: Paint) {
      val p = if (paint == null)
        mSmooth
      else {
        paint setFilterBitmap true
        paint
      }
      delegate.drawBitmap(colors, offset, stride, x, y, width,
                          height, hasAlpha, p)
    }

    override def drawBitmap(bitmap: Bitmap, matrix: Matrix, paint: Paint) {
      val p = if (paint == null)
        mSmooth
      else {
        paint setFilterBitmap true
        paint
      }
      delegate.drawBitmap(bitmap, matrix, p)
    }

    override def drawBitmapMesh(bitmap: Bitmap, meshWidth: Int, meshHeight: Int,
                                verts: Array[Float], vertOffset: Int,
                                colors: Array[Int], colorOffset: Int,
                                paint: Paint) {
      delegate.drawBitmapMesh(bitmap, meshWidth, meshHeight,
                              verts, vertOffset, colors,
                              colorOffset, paint)
    }

    override def drawVertices(mode: Canvas.VertexMode, vertexCount: Int,
                              verts: Array[Float], vertOffset: Int,
                              texs: Array[Float], texOffset: Int,
                              colors: Array[Int], colorOffset: Int,
                              indices: Array[Short], indexOffset: Int,
                              indexCount: Int, paint: Paint) {
      delegate.drawVertices(mode, vertexCount, verts, vertOffset,
                            texs, texOffset, colors, colorOffset,
                            indices, indexOffset, indexCount, paint)
    }

    override def drawText(text: Array[Char], index: Int, count: Int,
                 x: Float, y: Float, paint: Paint) {
      delegate.drawText(text, index, count, x, y, paint)
    }

    override def drawText(text: String, x: Float, y: Float, paint: Paint) {
      delegate.drawText(text, x, y, paint)
    }

    override def drawText(text: String, start: Int, end: Int, x: Float,
                 y: Float, paint: Paint) {
      delegate.drawText(text, start, end, x, y, paint);
    }

    override def drawText(text: CharSequence, start: Int, end: Int,
                 x: Float, y: Float, paint: Paint) {
      delegate.drawText(text, start, end, x, y, paint);
    }

    override def drawPosText(text: Array[Char], index: Int, count: Int,
                    pos: Array[Float], paint: Paint) {
      delegate.drawPosText(text, index, count, pos, paint);
    }

    override def drawPosText(text: String, pos: Array[Float], paint: Paint) {
      delegate.drawPosText(text, pos, paint)
    }

    override def drawTextOnPath(text: Array[Char], index: Int, count: Int, path: Path,
                       hOffset: Float, vOffset: Float, paint: Paint) {
      delegate.drawTextOnPath(text, index, count, path, hOffset, vOffset, paint)
    }

    override def drawTextOnPath(text: String, path: Path,
                       hOffset: Float, vOffset: Float, paint: Paint) {
      delegate.drawTextOnPath(text, path, hOffset, vOffset, paint)
    }

    override def drawPicture(picture: Picture) {
      delegate.drawPicture(picture)
    }

    override def drawPicture(picture: Picture, dst: RectF) {
      delegate.drawPicture(picture, dst)
    }

    override def drawPicture(picture: Picture, dst: Rect) {
      delegate.drawPicture(picture, dst)
    }
  }
}
