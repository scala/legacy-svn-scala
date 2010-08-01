/*
 * Copyright (C) 2008 The Android Open Source Project
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

import javax.microedition.khronos.egl.{EGL10, EGLConfig}
import javax.microedition.khronos.opengles.GL10

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent

/**
 * Wrapper activity demonstrating the use of {@link GLSurfaceView}, a view
 * that uses OpenGL drawing into a dedicated surface.
 *
 * Shows:
 * + How to redraw in response to user input.
 */
class TouchRotateActivity extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Create our Preview view and set it as the content of our
    // Activity
    mGLSurfaceView = new TouchSurfaceView(this)
    setContentView(mGLSurfaceView)
    mGLSurfaceView.requestFocus()
    mGLSurfaceView setFocusableInTouchMode true
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

/**
 * Implement a simple rotation control.
 *
 */
class TouchSurfaceView(context: Context) extends GLSurfaceView(context) {
  private val mRenderer = new CubeRenderer()
  setRenderer(mRenderer)
  setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)

  override def onTrackballEvent(event: MotionEvent): Boolean = {
    mRenderer.mAngleX += event.getX * TRACKBALL_SCALE_FACTOR
    mRenderer.mAngleY += event.getY * TRACKBALL_SCALE_FACTOR
    requestRender()
    true
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    val x = event.getX
    val y = event.getY
    event.getAction match {
      case MotionEvent.ACTION_MOVE =>
        val dx = x - mPreviousX
        val dy = y - mPreviousY
        mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR
        mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR
        requestRender()
      case _ =>
    }
    mPreviousX = x
    mPreviousY = y
    true
  }

  /**
   * Render a cube.
   */
  private class CubeRenderer extends AnyRef with GLSurfaceView.Renderer {
    private val mCube = new Cube()

    def onDrawFrame(gl: GL10) {
      /*
       * Usually, the first thing one might want to do is to clear
       * the screen. The most efficient way of doing this is to use
       * glClear().
       */

      gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT)

      /*
       * Now we're ready to draw some 3D objects
       */

      gl glMatrixMode GL10.GL_MODELVIEW
      gl.glLoadIdentity()
      gl.glTranslatef(0, 0, -3.0f)
      gl.glRotatef(mAngleX, 0, 1, 0)
      gl.glRotatef(mAngleY, 1, 0, 0)

      gl glEnableClientState GL10.GL_VERTEX_ARRAY
      gl glEnableClientState GL10.GL_COLOR_ARRAY

      mCube draw gl
    }

    def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
      gl.glViewport(0, 0, width, height)

      /*
       * Set our projection matrix. This doesn't have to be done
       * each time we draw, but usually a new projection needs to
       * be set when the viewport is resized.
       */

      val ratio = width.toFloat / height
      gl glMatrixMode GL10.GL_PROJECTION
      gl.glLoadIdentity()
      gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10)
    }

    def onSurfaceCreated(gl: GL10, config: EGLConfig) {
      /*
       * By default, OpenGL enables features that improve quality
       * but reduce performance. One might want to tweak that
       * especially on software renderer.
       */
      gl glDisable GL10.GL_DITHER

      /*
       * Some one-time OpenGL initialization can be made here
       * probably based on features of this particular context
       */
      gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)

      gl.glClearColor(1,1,1,1)
      gl glEnable GL10.GL_CULL_FACE
      gl glShadeModel GL10.GL_SMOOTH
      gl glEnable GL10.GL_DEPTH_TEST
    }

    var mAngleX: Float = _
    var mAngleY: Float = _
  }

  private final val TOUCH_SCALE_FACTOR = 180.0f / 320
  private final val TRACKBALL_SCALE_FACTOR = 36.0f
  private var mPreviousX: Float = _
  private var mPreviousY :Float = _
}


