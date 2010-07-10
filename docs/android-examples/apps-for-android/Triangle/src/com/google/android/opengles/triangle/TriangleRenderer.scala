/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.android.opengles.triangle

import java.io.{IOException, InputStream}
import java.nio.{ByteBuffer, ByteOrder, FloatBuffer, ShortBuffer}

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import android.opengl.{GLU, GLUtils}
import android.os.SystemClock

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.opengles.GL10

class TriangleRenderer(mContext: Context) extends AnyRef with GLView.Renderer {

  def getConfigSpec: Array[Int] =
    // We don't need a depth buffer, and don't care about our color depth.
    Array(EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE)

  def surfaceCreated(gl: GL10) {
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

    gl.glClearColor(.5f, .5f, .5f, 1)
    gl glShadeModel GL10.GL_SMOOTH
    gl glEnable GL10.GL_DEPTH_TEST
    gl glEnable GL10.GL_TEXTURE_2D

    /*
     * Create our texture. This has to be done each time the
     * surface is created.
     */

    val textures = new Array[Int](1)
    gl.glGenTextures(1, textures, 0)

    mTextureID = textures(0)
    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)

    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST)
    gl.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR)

    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE)
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE)

    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_REPLACE)

    val is = mContext.getResources openRawResource R.drawable.tex
    val bitmap = try {
      BitmapFactory.decodeStream(is)
    } finally {
      try { is.close() }
      catch { case e: IOException => /* Ignore.*/ }
    }

    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
    bitmap.recycle()
  }

  def drawFrame(gl: GL10) {
    /*
     * By default, OpenGL enables features that improve quality
     * but reduce performance. One might want to tweak that
     * especially on software renderer.
     */
    gl glDisable GL10.GL_DITHER

    gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                 GL10.GL_MODULATE)

    /*
     * Usually, the first thing one might want to do is to clear
     * the screen. The most efficient way of doing this is to use
     * glClear().
     */

    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

    /*
     * Now we're ready to draw some 3D objects
     */

    gl glMatrixMode GL10.GL_MODELVIEW
    gl.glLoadIdentity()

    GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

    gl glEnableClientState GL10.GL_VERTEX_ARRAY
    gl glEnableClientState GL10.GL_TEXTURE_COORD_ARRAY

    gl.glActiveTexture(GL10.GL_TEXTURE0)
    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
    gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_REPEAT)
    gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_REPEAT)

    val time = SystemClock.uptimeMillis() % 4000L
    val angle = 0.090f * time.toInt

    gl.glRotatef(angle, 0, 0, 1.0f)

    mTriangle draw gl
  }

  def sizeChanged(gl: GL10, w: Int, h: Int) {
    gl.glViewport(0, 0, w, h)

    /*
     * Set our projection matrix. This doesn't have to be done
     * each time we draw, but usually a new projection needs to
     * be set when the viewport is resized.
     */

     val ratio = w.toFloat / h
    gl glMatrixMode GL10.GL_PROJECTION
    gl.glLoadIdentity()
    gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7)
  }

  private val mTriangle = new Triangle()
  private var mTextureID: Int = _
}

protected[triangle] class Triangle {
  import Triangle._  // companion object

  init()

  private def init() {
    // Buffers to be passed to gl*Pointer() functions
    // must be direct, i.e., they must be placed on the
    // native heap where the garbage collector cannot
    // move them.
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

    // A unit-sided equalateral triangle centered on the origin.
    val coords = Array(
      // X, Y, Z
      -0.5f, -0.25f, 0,
       0.5f, -0.25f, 0,
       0.0f,  0.559016994f, 0
    )

    for (i <- 0 until VERTS) {
      for (j <- 0 until 3) {
        mFVertexBuffer.put(coords(i*3+j) * 2.0f)
      }
      for (j <- 0 until 2) {
        mTexBuffer.put(coords(i*3+j) * 2.0f + 0.5f)
      }
      mIndexBuffer put i.toShort
    }

    mFVertexBuffer position 0
    mTexBuffer position 0
    mIndexBuffer position 0
  }

  def draw(gl: GL10) {
    gl glFrontFace GL10.GL_CCW
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer)
    gl glEnable GL10.GL_TEXTURE_2D
    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer)
    gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS,
                GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
  }

  private var mFVertexBuffer: FloatBuffer = _
  private var mTexBuffer: FloatBuffer = _
  private var mIndexBuffer: ShortBuffer = _
}

protected [triangle] object Triangle {
    private final val VERTS = 3
}
