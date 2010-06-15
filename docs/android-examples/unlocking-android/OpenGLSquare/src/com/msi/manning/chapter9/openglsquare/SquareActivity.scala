/*
 * Copyright (C) 2009 Manning Publications Co.
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

package com.msi.manning.chapter9.openglsquare

import android.app.Activity
import android.content.Context
import android.opengl.GLU
import android.os.Bundle
import android.view.{SurfaceHolder, SurfaceView}

import java.nio.{ByteBuffer, ByteOrder, FloatBuffer}

import javax.microedition.khronos.egl._
import javax.microedition.khronos.opengles._

class SquareActivity extends Activity {
  /** Called when the activity is first created. */
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(new DrawingSurfaceView(this))
  }

  private class DrawingSurfaceView(context: Context)
  extends SurfaceView(context) with SurfaceHolder.Callback  {

    private val mHolder = getHolder
    private var mThread: DrawingThread = _

    mHolder addCallback this
    mHolder setType SurfaceHolder.SURFACE_TYPE_GPU

    def surfaceCreated(holder: SurfaceHolder) {
      mThread = new DrawingThread()
      mThread.start()
    }

    def surfaceDestroyed(holder: SurfaceHolder) {
      mThread.waitForExit()
      mThread = null
    }

    def surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
      mThread.onWindowResize(w, h)
    }

    private object DrawingThread {
      private val configSpec = Array(
        EGL10.EGL_RED_SIZE,      5,
        EGL10.EGL_GREEN_SIZE,    6,
        EGL10.EGL_BLUE_SIZE,     5,
        EGL10.EGL_DEPTH_SIZE,   16,
        EGL10.EGL_NONE)
    }

    private class DrawingThread extends Thread {
      import DrawingThread._  // companion object

      private var stopped = false
      private var w = 0
      private var h = 0
      private var changed = true

      override def run() {    
        val egl = EGLContext.getEGL.asInstanceOf[EGL10]
        val dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        val version = new Array[Int](2)
        egl.eglInitialize(dpy, version)

        val configs = new Array[EGLConfig](1)
        val num_config = new Array[Int](1)
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config)
        val config = configs(0)

        val context = egl.eglCreateContext(dpy, config,
                        EGL10.EGL_NO_CONTEXT, null)

        var surface: EGLSurface = null
        var gl: GL10 = null

        // now draw forever until asked to stop
        while (!stopped) {
          var W, H: Int = 0 // copies of the current width and height
          var updated = false
          synchronized {
            updated = this.changed
            W = this.w
            H = this.h
            this.changed = false
          }
          if (updated) {
            /*
             * The window size has changed, so we need to create a new surface.
             */
            if (surface != null) {

              /*
               * unbind and destroy the old EGL surface, if there is one.
               */
              egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE,
                              EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
              egl.eglDestroySurface(dpy, surface)
            }

            surface = egl.eglCreateWindowSurface(dpy, config, mHolder, null)

            egl.eglMakeCurrent(dpy, surface, surface, context)

            gl = context.getGL.asInstanceOf[GL10]

            gl.glDisable(GL10.GL_DITHER)

            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)

            gl.glClearColor(1, 1, 1, 1)
            gl.glEnable(GL10.GL_CULL_FACE)
            gl.glShadeModel(GL10.GL_SMOOTH)
            gl.glEnable(GL10.GL_DEPTH_TEST)

            gl.glViewport(0, 0, W, H)

            val ratio = W.toFloat / H
            gl.glMatrixMode(GL10.GL_PROJECTION)
            gl.glLoadIdentity()
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10)
          }
                
          drawFrame(gl)

          egl.eglSwapBuffers(dpy, surface)

          if (egl.eglGetError == EGL11.EGL_CONTEXT_LOST) {
            getContext match {
              case a: Activity => a.finish()
              case _ =>
            }
          }

          egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT)
          egl.eglDestroySurface(dpy, surface)
          egl.eglDestroyContext(dpy, context)
          egl.eglTerminate(dpy)
        }
      }

      def onWindowResize(w: Int, h: Int) = synchronized {
        this.w = w
        this.h = h
        this.changed = true
      }

      def waitForExit() {
        stopped = true
        try { join() }
        catch { case ex: InterruptedException => }
      }

      private def drawFrame(gl: GL10) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT)

        val square = Array(
          0.25f, 0.25f, 0.0f,
          0.75f, 0.25f, 0.0f,
          0.25f, 0.75f, 0.0f,
          0.75f, 0.75f, 0.0f)

        val bb = ByteBuffer.allocateDirect(square.length*4)
        bb order ByteOrder.nativeOrder()
        val squareBuff = bb.asFloatBuffer()
        squareBuff.put(square)
        squareBuff.position(0)

        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        GLU.gluOrtho2D(gl, 0.0f,1.2f,0.0f,1.0f)

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareBuff)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
        gl.glColor4f(0,1,1,1)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
      }

    } // DrawingThread

  }
}
