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
import android.opengl.GLSurfaceView
import android.os.Bundle

/**
 * Wrapper activity demonstrating the use of {@link GLSurfaceView}, a view
 * that uses OpenGL drawing into a dedicated surface.
 */
class GLSurfaceViewActivity extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Create our Preview view and set it as the content of our
    // Activity
    mGLSurfaceView = new GLSurfaceView(this)
    mGLSurfaceView setRenderer new CubeRenderer(false)
    setContentView(mGLSurfaceView)
  }

  override protected def onResume() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onResume()
    mGLSurfaceView.onResume()
  }

  override protected def onPause() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onPause()
    mGLSurfaceView.onPause()
  }

  private var mGLSurfaceView: GLSurfaceView = _
}
