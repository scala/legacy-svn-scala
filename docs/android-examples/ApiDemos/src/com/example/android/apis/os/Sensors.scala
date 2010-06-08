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

package com.example.android.apis.os

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.hardware.{Sensor, SensorEvent, SensorManager, SensorEventListener}
import android.util.Log
import android.graphics.{Bitmap, Canvas, Color, Paint, Path, RectF}

/**
 * <h3>Application that displays the values of the acceleration sensor graphically.</h3>

<p>This demonstrates the {@link android.hardware.SensorManager android.hardware.SensorManager} class.

<h4>Demo</h4>
OS / Sensors
 
<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com.example.android.apis/os/Sensors.java</td>
 *             <td >Sensors</td>
 *         </tr>
 * </table> 
 */
class Sensors extends Activity {
  import Sensors._  // companion object

  private var mSensorManager: SensorManager = _
  private var mGraphView: GraphView = _

  private class GraphView(context: Context) extends View(context)
                                               with SensorEventListener {
    private var mBitmap: Bitmap = _
    private val mPaint = new Paint
    private val mCanvas = new Canvas
    private val mPath = new Path
    private val mRect = new RectF
    private val mLastValues = new Array[Float](3*2)
    private val mOrientationValues = new Array[Float](3)
    private var mLastX: Float = _
    private val mScale = new Array[Float](2)
    private var mYOffset: Float = _
    private var mMaxX: Float = _
    private var mSpeed: Float = 1.0f
    private var mWidth: Float = _
    private var mHeight: Float = _
        
    val mColors = Array(
      Color.argb(192, 255, 64, 64),
      Color.argb(192, 64, 128, 64),
      Color.argb(192, 64, 64, 255),
      Color.argb(192, 64, 255, 255),
      Color.argb(192, 128, 64, 128),
      Color.argb(192, 255, 255, 64))

    mPaint.setFlags(Paint.ANTI_ALIAS_FLAG)
    mRect.set(-0.5f, -0.5f, 0.5f, 0.5f)
    mPath.arcTo(mRect, 0, 180)

    override protected def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
      mCanvas setBitmap mBitmap
      mCanvas drawColor 0xFFFFFFFF
      mYOffset = h * 0.5f
      mScale(0) = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)))
      mScale(1) = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)))
      mWidth = w
      mHeight = h
      mMaxX = if (mWidth < mHeight) w else w - 50
      mLastX = mMaxX;
      super.onSizeChanged(w, h, oldw, oldh)
    }

    override protected def onDraw(canvas: Canvas) {
      synchronized {
        if (mBitmap != null) {
          val paint = mPaint
          val path = mPath
          val outer = 0xFFC0C0C0
          val inner = 0xFFff7010

          if (mLastX >= mMaxX) {
            mLastX = 0
            val cavas = mCanvas
            val yoffset = mYOffset
            val maxx = mMaxX
            val oneG = SensorManager.STANDARD_GRAVITY * mScale(0)
            paint.setColor(0xFFAAAAAA)
            cavas.drawColor(0xFFFFFFFF)
            cavas.drawLine(0, yoffset,      maxx, yoffset,      paint)
            cavas.drawLine(0, yoffset+oneG, maxx, yoffset+oneG, paint)
            cavas.drawLine(0, yoffset-oneG, maxx, yoffset-oneG, paint)
          }
          canvas.drawBitmap(mBitmap, 0, 0, null)

          val values = mOrientationValues
          if (mWidth < mHeight) {
            val w0 = mWidth * 0.333333f
            val w  = w0 - 32
            var x = w0 * 0.5f
            for (i <- 0 until 3) {
              canvas.save(Canvas.MATRIX_SAVE_FLAG)
              canvas.translate(x, w*0.5f + 4.0f)
              canvas.save(Canvas.MATRIX_SAVE_FLAG)
              paint.setColor(outer)
              canvas.scale(w, w)
              canvas.drawOval(mRect, paint)
              canvas.restore()
              canvas.scale(w-5, w-5)
              paint.setColor(inner)
              canvas.rotate(-values(i))
              canvas.drawPath(path, paint)
              canvas.restore()
              x += w0
            }
          } else {
            val h0 = mHeight * 0.333333f
            val h  = h0 - 32
            var y = h0 * 0.5f
            for (i <- 0 until 3) {
              canvas.save(Canvas.MATRIX_SAVE_FLAG)
              canvas.translate(mWidth - (h*0.5f + 4.0f), y)
              canvas.save(Canvas.MATRIX_SAVE_FLAG)
              paint.setColor(outer)
              canvas.scale(h, h)
              canvas.drawOval(mRect, paint)
              canvas.restore()
              canvas.scale(h-5, h-5)
              paint.setColor(inner)
              canvas.rotate(-values(i))
              canvas.drawPath(path, paint)
              canvas.restore()
              y += h0
            }
          }

        }
      }
    }

    def onSensorChanged(sensor: SensorEvent) {
      //Log.d(TAG, "sensor: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
      synchronized {
        if (mBitmap != null) {
          val canvas = mCanvas
          val paint = mPaint
          if (sensor == SensorManager.SENSOR_ORIENTATION) {
            for (i <- 0 until 3) {
              mOrientationValues(i) = sensor.values(i)
            }
          } else {
            val deltaX = mSpeed
            val newX = mLastX + deltaX

            val j = if (sensor == SensorManager.SENSOR_MAGNETIC_FIELD) 1 else 0
            for (i <- 0  until 3) {
              val k = i+j*3
              val v = mYOffset + sensor.values(i) * mScale(j)
              paint setColor mColors(k)
              canvas.drawLine(mLastX, mLastValues(k), newX, v, paint)
              mLastValues(k) = v
            }
            if (sensor == SensorManager.SENSOR_MAGNETIC_FIELD)
              mLastX += mSpeed
          }
          invalidate()
        }
      }
    }

    def onAccuracyChanged(sensor: Sensor, accuracy: Int) {
      // TODO Auto-generated method stub
            
    }
  }
    
  /**
   * Initialization of the Activity after it is first created.  Must at least
   * call {@link android.app.Activity#setContentView setContentView()} to
   * describe what is to be displayed in the screen.
   */
  override protected def onCreate(savedInstanceState: Bundle) {
    // Be sure to call the super class.
    super.onCreate(savedInstanceState)

    mSensorManager = getSystemService(SENSOR_SERVICE).asInstanceOf[SensorManager]
    mGraphView = new GraphView(this)
    setContentView(mGraphView)
  }

  override protected def onResume() {
    super.onResume()

    val sensors = mSensorManager.getSensorList(
      Sensor.TYPE_ACCELEROMETER | Sensor.TYPE_MAGNETIC_FIELD |
      Sensor.TYPE_ORIENTATION)
    val it = sensors.iterator()
    while (it.hasNext) {
      mSensorManager.registerListener(mGraphView, it.next(),
        SensorManager.SENSOR_DELAY_FASTEST)
    }
  }
    
  override protected def onStop() {
    mSensorManager unregisterListener mGraphView
    super.onStop()
  }
}

object Sensors {
  /** Tag string for our debug logs */
  private final val TAG = "Sensors"

  private final val SENSOR_SERVICE = Context.SENSOR_SERVICE
}
