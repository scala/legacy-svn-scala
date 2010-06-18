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

package com.example.android.gesture.builder

import android.app.Activity
import android.app.Activity._
import android.gesture.{Gesture, GestureLibrary, GestureOverlayView}
import android.os.{Bundle, Environment}
import android.view.{View, MotionEvent}
import android.widget.{TextView, Toast}

import java.io.File

object CreateGestureActivity {
  private final val LENGTH_THRESHOLD = 120.0f
}

class CreateGestureActivity extends Activity {
  import CreateGestureActivity._  // companion object

  private var mGesture: Gesture = _
  private var mDoneButton: View = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
        
    setContentView(R.layout.create_gesture)

    mDoneButton = findViewById(R.id.done)

    val overlay =
      findViewById(R.id.gestures_overlay).asInstanceOf[GestureOverlayView]
    overlay addOnGestureListener new GesturesProcessor
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
        
    if (mGesture != null) {
      outState.putParcelable("gesture", mGesture)
    }
  }

  override protected def onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
        
    mGesture = savedInstanceState getParcelable "gesture"
    if (mGesture != null) {
      val overlay =
        findViewById(R.id.gestures_overlay).asInstanceOf[GestureOverlayView]
      overlay post new Runnable {
        def run() {
          overlay setGesture mGesture
        }
      }

      mDoneButton setEnabled true
    }
  }

  @SuppressWarnings(Array("UnusedDeclaration"))
  def addGesture(v: View) {
    if (mGesture != null) {
      val input = findViewById(R.id.gesture_name).asInstanceOf[TextView]
      val name = input.getText
      if (name.length == 0) {
        input setError getString(R.string.error_missing_name)
        return
      }

      val store: GestureLibrary = GestureBuilderActivity.getStore
      store.addGesture(name.toString, mGesture)
      store.save()

      setResult(RESULT_OK)

      val path = new File(Environment.getExternalStorageDirectory,
                          "gestures").getAbsolutePath
      Toast.makeText(this, getString(R.string.save_success, path),
                     Toast.LENGTH_LONG).show()
    } else {
      setResult(RESULT_CANCELED)
    }

    finish()
  }
    
  @SuppressWarnings(Array("UnusedDeclaration"))
  def cancelGesture(v: View) {
    setResult(RESULT_CANCELED)
    finish()
  }
    
  private class GesturesProcessor extends GestureOverlayView.OnGestureListener {
    def onGestureStarted(overlay: GestureOverlayView, event: MotionEvent) {
      mDoneButton setEnabled false
      mGesture = null
    }

    def onGesture(overlay: GestureOverlayView, event: MotionEvent) {
    }

    def onGestureEnded(overlay: GestureOverlayView, event: MotionEvent) {
      mGesture = overlay.getGesture
      if (mGesture.getLength < LENGTH_THRESHOLD) {
        overlay clear false
      }
      mDoneButton setEnabled true
    }

    def onGestureCancelled(overlay: GestureOverlayView, event: MotionEvent) {
    }
  }
}
