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

class Compass extends GraphicsActivity {
  import Compass._  // companion object

  private var mSensorManager: SensorManager = _
  private var mView: SampleView = _
  private var mValues: Array[Float] = _

  private final val mListener = new SensorEventListener() {
    def onSensorChanged(sensor: SensorEvent) {
      val values = sensor.values
      if (Config.LOGD)
        Log.d(TAG, "sensorChanged "+values.mkString("(", ", ", ")"))
      mValues = values
      if (mView != null) {
        mView.invalidate()
      }
    }

    def onAccuracyChanged(sensor: Sensor, accuracy: Int) {
      // TODO Auto-generated method stub
            
    }
  }

  override protected def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    mSensorManager =
      getSystemService(Context.SENSOR_SERVICE).asInstanceOf[SensorManager]
    mView = new SampleView(this)
    setContentView(mView)
  }

  override protected def onResume() {
    if (Config.LOGD) Log.d(TAG, "onResume")
    super.onResume()

    val sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION)
    val it = sensors.iterator()
    while (it.hasNext) {
      mSensorManager.registerListener(mListener, it.next(),
        SensorManager.SENSOR_DELAY_GAME)
    }
  }
    
  override protected def onStop() {
    if (Config.LOGD) Log.d(TAG, "onStop")
    mSensorManager unregisterListener mListener
    super.onStop()
  }

  private class SampleView(context: Context) extends View(context) {
    private val mPaint = new Paint()
    private val mPath = new Path()
    private var mAnimate: Boolean = _
    private var mNextTime: Long = _

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
      mAnimate = true
      super.onAttachedToWindow()
    }

    override protected def onDetachedFromWindow() {
      mAnimate = false
      super.onDetachedFromWindow()
    }
  }

}

object Compass {
  private final val TAG = "Compass"
}

