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

import android.opengl.GLES10._

import java.io.{IOException, InputStream}
import java.nio.{ByteBuffer, ByteOrder, FloatBuffer, ShortBuffer}

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import android.opengl.{GLSurfaceView, GLU, GLUtils}
import android.os.SystemClock

import com.example.android.apis.R

/**
 * A GLSurfaceView.Renderer that uses the Android-specific
 * android.opengl.GLESXXX static OpenGL ES APIs. The static APIs
 * expose more of the OpenGL ES features than the
 * javax.microedition.khronos.opengles APIs, and also
 * provide a programming model that is closer to the C OpenGL ES APIs, which
 * may make it easier to reuse code and documentation written for the
 * C OpenGL ES APIs.
 *
 */
class StaticTriangleRenderer(context: Context) extends AnyRef
                                                  with GLSurfaceView.Renderer {
  import StaticTriangleRenderer._  // companion object

  private val mContext = context
  private val mTriangle = new Triangle()
  private var mTextureID: Int = _

  def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    /*
     * By default, OpenGL enables features that improve quality
     * but reduce performance. One might want to tweak that
     * especially on software renderer.
     */
    glDisable(GL_DITHER)

    /*
     * Some one-time OpenGL initialization can be made here
     * probably based on features of this particular context
     */
    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST)

    glClearColor(.5f, .5f, .5f, 1)
    glShadeModel(GL_SMOOTH)
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_TEXTURE_2D)

    /*
     * Create our texture. This has to be done each time the
     * surface is created.
     */

    val textures = new Array[Int](1)
    glGenTextures(1, textures, 0)

    mTextureID = textures(0)
    glBindTexture(GL_TEXTURE_2D, mTextureID)

    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE)

    val is = mContext.getResources.openRawResource(R.drawable.robot)
    val bitmap: Bitmap = try {
      BitmapFactory.decodeStream(is)
    } finally {
      try { is.close() }
      catch { case e: IOException => /* Ignore. */ }
    }
    GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
    bitmap.recycle()
  }

  def onDrawFrame(gl: GL10) {
    /*
     * By default, OpenGL enables features that improve quality
     * but reduce performance. One might want to tweak that
     * especially on software renderer.
     */
    glDisable(GL_DITHER)

    glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE)

    /*
     * Usually, the first thing one might want to do is to clear
     * the screen. The most efficient way of doing this is to use
     * glClear().
     */

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    /*
     * Now we're ready to draw some 3D objects
     */

    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()

    GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

    glEnableClientState(GL_VERTEX_ARRAY)
    glEnableClientState(GL_TEXTURE_COORD_ARRAY)

    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, mTextureID)
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

    val time = SystemClock.uptimeMillis % 4000L
    val angle = 0.090f * time.toInt

    glRotatef(angle, 0, 0, 1.0f)

    mTriangle.draw(gl)
  }

  def onSurfaceChanged(gl: GL10, w: Int, h: Int) {
    glViewport(0, 0, w, h)

    /*
     * Set our projection matrix. This doesn't have to be done
     * each time we draw, but usually a new projection needs to
     * be set when the viewport is resized.
     */

    val ratio = w.toFloat / h
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glFrustumf(-ratio, ratio, -1, 1, 3, 7)
  }

}

object StaticTriangleRenderer {

  private object Triangle {
    private final val VERTS = 3
  }

  private /*static*/ class Triangle {
    import Triangle._  // companion object

    {
      // Buffers to be passed to gl*Pointer() functions must be direct, i.e.,
      // they must be placed on the native heap where the garbage collector
      // cannot move them.
      //
      // Buffers with multi-byte datatypes (e.g., short, int, float)
      // must have their byte order set to native order
      val vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4)
      vbb order ByteOrder.nativeOrder()
      mFVertexBuffer = vbb.asFloatBuffer()

      val tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4)
      tbb order ByteOrder.nativeOrder()
      mTexBuffer = tbb.asFloatBuffer()

      val ibb = ByteBuffer.allocateDirect(VERTS * 2)
      ibb order ByteOrder.nativeOrder()
      mIndexBuffer = ibb.asShortBuffer()

      // A unit-sided equilateral triangle centered on the origin.
      val coords = Array(
        // X, Y, Z
        -0.5f, -0.25f, 0,
         0.5f, -0.25f, 0,
         0.0f,  0.559016994f, 0
      )

      for (i <- 0 until VERTS; j <- 0 until 3) {
        mFVertexBuffer.put(coords(i*3+j) * 2.0f)
      }

      for (i <- 0 until VERTS; j <- 0 until 2) {
        mTexBuffer.put(coords(i*3+j) * 2.0f + 0.5f)
      }

      for (i <- 0 until VERTS) {
        mIndexBuffer put i.toShort
      }

      mFVertexBuffer position 0
      mTexBuffer position 0
      mIndexBuffer position 0
    }

    def draw(gl: GL10) {
      glFrontFace(GL_CCW)
      glVertexPointer(3, GL_FLOAT, 0, mFVertexBuffer)
      glEnable(GL_TEXTURE_2D)
      glTexCoordPointer(2, GL_FLOAT, 0, mTexBuffer)
      glDrawElements(GL_TRIANGLE_STRIP, VERTS,
                     GL_UNSIGNED_SHORT, mIndexBuffer)
    }

    private var mFVertexBuffer: FloatBuffer = _
    private var mTexBuffer: FloatBuffer = _
    private var mIndexBuffer: ShortBuffer = _
  }
}
