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
import android.hardware.Camera
import android.os.Bundle
import android.view.{SurfaceHolder, SurfaceView, Window}

import java.io.IOException

// ----------------------------------------------------------------------

class CameraPreview extends Activity {    
  private var mPreview: Preview = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Hide the window title.
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    // Create our Preview view and set it as the content of our activity.
    mPreview = new Preview(this)
    setContentView(mPreview)
  }

}

// ----------------------------------------------------------------------

class Preview(context: Context) extends SurfaceView(context)
                                   with SurfaceHolder.Callback {
  private var mCamera: Camera = _
  private val mHolder = getHolder
  // Install a SurfaceHolder.Callback so we get notified when the
  // underlying surface is created and destroyed.
  mHolder addCallback this
  mHolder setType SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS

  def surfaceCreated(holder: SurfaceHolder) {
    // The Surface has been created, acquire the camera and tell it where
    // to draw.
    mCamera = Camera.open()
    try {
      mCamera setPreviewDisplay holder
    } catch {
      case e: IOException =>
        mCamera.release()
        mCamera = null
        // TODO: add more exception handling logic here
    }
  }

  def surfaceDestroyed(holder: SurfaceHolder) {
    // Surface will be destroyed when we return, so stop the preview.
    // Because the CameraDevice object is not a shared resource, it's very
    // important to release it when the activity is paused.
    mCamera.stopPreview()
    mCamera.release()
    mCamera = null
  }

  def surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
    // Now that the size is known, set up the camera parameters and begin
    // the preview.
    val parameters = mCamera.getParameters
    parameters.setPreviewSize(w, h)
    mCamera setParameters parameters
    mCamera.startPreview()
  }

}
