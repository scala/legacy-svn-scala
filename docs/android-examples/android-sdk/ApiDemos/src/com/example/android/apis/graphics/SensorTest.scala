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
import android.hardware.{Sensor, SensorEvent, SensorEventListener, SensorManager}
import android.os.{Bundle, Handler, Message, SystemClock}
import android.util.{Config, Log}
import android.view.View

class SensorTest extends GraphicsActivity {

  private var mSensorManager: SensorManager = _
  private var mView: SampleView = _
  private var mValues: Array[Float] = _

  private /*static*/ class RunAve(weights: Array[Float]) {
    private final val mWeightScale = weights.foldLeft(0f)(_+_)
    private final val mDepth = weights.length
    private final var mSamples = new Array[Float](mDepth)
    private var mCurr = 0

    def addSample(value: Float) {
      mSamples(mCurr) = value
      mCurr = (mCurr + 1) % mDepth
    }

    def computeAve(): Float = {
      val depth = mDepth
      var index = mCurr
      var sum = 0f
      for (i <- 0 until depth) {
        sum += weights(i) * mSamples(index)
        index -= 1;
        if (index < 0) index -= 1
      }
      sum * mWeightScale
    }
  }

  private final val mListener = new SensorEventListener() {

    private final val mScale = Array(2.0f, 2.5f, 0.5f)   // accel

    private val mPrev = new Array[Float](3)

    def onSensorChanged(event: SensorEvent) {
      val values = event.values
      var show = false
      val diff = new Array[Float](values.length)

      for (i <- 0 until values.length) {
        diff(i) = math.round(mScale(i) * (values(i) - mPrev(i)) * 0.45f)
        if (math.abs(diff(i)) > 0) {
          show = true
        }
        mPrev(i) = values(i)
      }

      if (show) {
        // only shows if we think the delta is big enough, in an attempt
        // to detect "serious" moves left/right or up/down
        android.util.Log.e("test", "sensorChanged " + event +
                           values.mkString(" (", ", ", ")") +
                           diff.mkString(" diff(", " ", ")"))
      }

      val now = android.os.SystemClock.uptimeMillis
      if (now - mLastGestureTime > 1000) {
        mLastGestureTime = 0

        val x = diff(0)
        val y = diff(1)
        val gestX = math.abs(x) > 3
        val gestY = math.abs(y) > 3

        if ((gestX || gestY) && !(gestX && gestY)) {
          if (gestX) {
            if (x < 0) {
              Log.e("test", "<<<<<<<< LEFT <<<<<<<<<<<<")
            } else {
              Log.e("test", ">>>>>>>>> RITE >>>>>>>>>>>")
            }
          } else {
            if (y < -2) {
              Log.e("test", "<<<<<<<< UP <<<<<<<<<<<<")
            } else {
              Log.e("test", ">>>>>>>>> DOWN >>>>>>>>>>>")
            }
          }
          mLastGestureTime = now
        }
      }
    }

    private var mLastGestureTime: Long = _

    def onAccuracyChanged(sensor: Sensor, accuracy: Int) {
      // TODO Auto-generated method stub
            
    }
  }

  override protected def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    mSensorManager = getSystemService(Context.SENSOR_SERVICE).asInstanceOf[SensorManager]
    mView = new SampleView(this)
    setContentView(mView)
//  android.util.Log.d("skia", "create " + mSensorManager)
  }

  override protected def onResume() {
    super.onResume()

    val sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)
    val it = sensors.iterator()
    while (it.hasNext) {
      mSensorManager.registerListener(mListener, it.next(),
        SensorManager.SENSOR_DELAY_FASTEST)
    }
//  android.util.Log.d("skia", "resume " + mSensorManager)
  }

  override protected def onStop() {
    mSensorManager unregisterListener mListener
    super.onStop()
//  android.util.Log.d("skia", "stop " + mSensorManager)
  }

  private class SampleView(context: Context) extends View(context) {
    private val mPaint = new Paint
    private val mPath = new Path
    //private var mAnimate: Boolean = _
    //private var mNextTime: Long = _

    // Construct a wedge-shaped path
    mPath.moveTo(0, -50)
    mPath.lineTo(-20, 60)
    mPath.lineTo(0, 50)
    mPath.lineTo(20, 60)
    mPath.close()

    override protected def onDraw(canvas: Canvas) {
      val paint = mPaint

      canvas drawColor Color.WHITE

      paint setAntiAlias true
      paint setColor Color.BLACK
      paint setStyle Paint.Style.FILL

      val w = canvas.getWidth
      val h = canvas.getHeight
      val cx = w / 2
      val cy = h / 2

      canvas.translate(cx, cy)
      if (mValues != null) {            
        canvas rotate -mValues(0)
      }
      canvas.drawPath(mPath, mPaint)
    }

    override protected def onAttachedToWindow() {
      //mAnimate = true
      super.onAttachedToWindow()
    }

    override protected def onDetachedFromWindow() {
      //mAnimate = false
      super.onDetachedFromWindow()
    }
  }
}

