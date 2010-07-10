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

import android.content.Context
import android.util.AttributeSet
import android.view.{SurfaceHolder, SurfaceView}

import scala.collection.mutable.ListBuffer

import java.util.concurrent.Semaphore

import javax.microedition.khronos.egl.{EGL10, EGL11, EGLConfig, EGLContext,
                                       EGLDisplay, EGLSurface}
import javax.microedition.khronos.opengles.{GL, GL10}

/**
 * An implementation of SurfaceView that uses the dedicated surface for
 * displaying an OpenGL animation.  This allows the animation to run in a
 * separate thread, without requiring that it be driven by the update mechanism
 * of the view hierarchy.
 *
 * The application-specific rendering code is delegated to a GLView.Renderer
 * instance.
 */
class GLView(context: Context, attrs: AttributeSet)
extends SurfaceView(context, attrs) with SurfaceHolder.Callback {
  import GLView._  // companion object

  init()

  def this(context: Context) = this(context, null)

  private def init() {
    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed
    mHolder = getHolder
    mHolder addCallback this
    mHolder setType SurfaceHolder.SURFACE_TYPE_GPU
  }

  def setGLWrapper(glWrapper: GLWrapper) {
    mGLWrapper = glWrapper
  }

  def setRenderer(renderer: Renderer) {
    mGLThread = new GLThread(renderer)
    mGLThread.start();
  }

  def surfaceCreated(holder: SurfaceHolder) {
    mGLThread.surfaceCreated()
  }

  def surfaceDestroyed(holder: SurfaceHolder) {
    // Surface will be destroyed when we return
    mGLThread.surfaceDestroyed()
  }

  def surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
    // Surface size or format has changed. This should not happen in this
    // example.
    mGLThread.onWindowResize(w, h)
  }

  /**
   * Inform the view that the activity is paused.
   */
  def onPause() {
    mGLThread.onPause()
  }

  /**
   * Inform the view that the activity is resumed.
   */
  def onResume() {
    mGLThread.onResume()
  }

  /**
   * Inform the view that the window focus has changed.
   */
  override def onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    mGLThread onWindowFocusChanged hasFocus
  }

  /**
   * Queue an "event" to be run on the GL rendering thread.
   * @param r the runnable to be run on the GL rendering thread.
   */
  def queueEvent(r: Runnable) {
    mGLThread queueEvent r
  }

  override protected def onDetachedFromWindow() {
    super.onDetachedFromWindow()
    mGLThread.requestExitAndWait()
  }

  /**
   * An EGL helper class.
   */

  private class EglHelper {

    /**
     * Initialize EGL for a given configuration spec.
     * @param configSpec
     */
    def start(configSpec: Array[Int]) {
      /*
       * Get an EGL instance
       */
      mEgl = EGLContext.getEGL.asInstanceOf[EGL10]

      /*
       * Get to the default display.
       */
      mEglDisplay = mEgl eglGetDisplay EGL10.EGL_DEFAULT_DISPLAY

      /*
       * We can now initialize EGL for that display
       */
      val version = new Array[Int](2)
      mEgl.eglInitialize(mEglDisplay, version)

      val configs = new Array[EGLConfig](1)
      val num_config = new Array[Int](1)
      mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1, num_config)
      mEglConfig = configs(0)

      /*
       * Create an OpenGL ES context. This must be done only once, an
       * OpenGL context is a somewhat heavy object.
       */
      mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig,
                    EGL10.EGL_NO_CONTEXT, null)

      mEglSurface = null
    }

    /*
     * Create and return an OpenGL surface
     */
    def createSurface(holder: SurfaceHolder): GL = {
      /*
       *  The window size has changed, so we need to create a new
       *  surface.
       */
      if (mEglSurface != null) {

        /*
         * Unbind and destroy the old EGL surface, if
         * there is one.
         */
        mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface)
      }

      /*
       * Create an EGL surface we can render into.
       */
      mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay,
                    mEglConfig, holder, null)

      /*
       * Before we can issue GL commands, we need to make sure
       * the context is current and bound to a surface.
       */
      mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)

      val gl = mEglContext.getGL
      if (mGLWrapper != null) mGLWrapper wrap gl else gl
    }

    /**
     * Display the current render surface.
     * @return false if the context has been lost.
     */
    def swap(): Boolean = {
      mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)

      /*
       * Always check for EGL_CONTEXT_LOST, which means the context
       * and all associated data were lost (For instance because
       * the device went to sleep). We need to sleep until we
       * get a new surface.
       */
      mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST
    }

    def finish() {
      if (mEglSurface != null) {
        mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT)
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface)
        mEglSurface = null
      }
      if (mEglContext != null) {
        mEgl.eglDestroyContext(mEglDisplay, mEglContext)
        mEglContext = null
      }
      if (mEglDisplay != null) {
        mEgl eglTerminate mEglDisplay
        mEglDisplay = null
      }
    }

    private var mEgl: EGL10 = _
    private var mEglDisplay: EGLDisplay = _
    private var mEglSurface: EGLSurface = _
    private var mEglConfig: EGLConfig = _
    private var mEglContext: EGLContext = _
  }

  /**
   * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
   * to a Renderer instance to do the actual drawing.
   *
   */

  private class GLThread(mRenderer: Renderer) extends Thread {
    import GLThread._  // companion object

    setName("GLThread")

    override def run() {
      /*
       * When the android framework launches a second instance of
       * an activity, the new instance's onCreate() method may be
       * called before the first instance returns from onDestroy().
       *
       * This semaphore ensures that only one instance at a time
       * accesses EGL.
       */
      try {
        try {
          sEglSemaphore.acquire()
        } catch {
          case e: InterruptedException => return
        }
        guardedRun()
      } catch {
        case e: InterruptedException =>
          // fall thru and exit normally
      } finally {
        sEglSemaphore.release()
      }
    }

    @throws (classOf[InterruptedException])
    private def guardedRun() {
      mEglHelper = new EglHelper()
      /*
       * Specify a configuration for our opengl session
       * and grab the first configuration that matches is
       */
      val configSpec = mRenderer.getConfigSpec
      mEglHelper start configSpec

      var gl: GL10 = null
      var tellRendererSurfaceCreated = true
      var tellRendererSurfaceChanged = true

      /*
       * This is our main activity thread's loop, we go until
       * asked to quit.
       */
      while (!mDone) {

        /*
         *  Update the asynchronous state (window size)
         */
        var w, h = 0
        var changed = false
        var needStart = false
        synchronized {
          var r = getEvent
          while (r != null) {
            r.run()
            r = getEvent
          }
          if (mPaused) {
            mEglHelper.finish()
            needStart = true
          }
          if (needToWait()) {
            while (needToWait()) {
               wait()
            }
          }
          if (!mDone) {
            changed = mSizeChanged
            w = mWidth
            h = mHeight
            mSizeChanged = false
          }
        } //synchronized
        if (!mDone) {
          if (needStart) {
            mEglHelper.start(configSpec)
            tellRendererSurfaceCreated = true
            changed = true
          }
          if (changed) {
            gl = mEglHelper.createSurface(mHolder).asInstanceOf[GL10]
            tellRendererSurfaceChanged = true
          }
          if (tellRendererSurfaceCreated) {
            mRenderer.surfaceCreated(gl)
            tellRendererSurfaceCreated = false
          }
          if (tellRendererSurfaceChanged) {
            mRenderer.sizeChanged(gl, w, h)
            tellRendererSurfaceChanged = false
          }
          if ((w > 0) && (h > 0)) {
            /* draw a frame here */
            mRenderer drawFrame gl

            /*
             * Once we're done with GL, we need to call swapBuffers()
             * to instruct the system to display the rendered frame
             */
            mEglHelper.swap()
          }
        }
      } //while

      /*
       * clean-up everything...
       */
      mEglHelper.finish()
    }

    private def needToWait(): Boolean =
      (mPaused || (! mHasFocus) || (! mHasSurface) || mContextLost) && (! mDone)

    def surfaceCreated() = synchronized {
      mHasSurface = true
      mContextLost = false
      notify()
    }

    def surfaceDestroyed() = synchronized {
      mHasSurface = false
      notify()
    }

    def onPause() = synchronized {
      mPaused = true
    }

    def onResume() = synchronized {
      mPaused = false
      notify()
    }

    def onWindowFocusChanged(hasFocus: Boolean) = synchronized {
      mHasFocus = hasFocus
      if (mHasFocus) notify()
    }

    def onWindowResize(w: Int, h: Int) = synchronized {
      mWidth = w
      mHeight = h
      mSizeChanged = true
    }

    def requestExitAndWait() {
      // don't call this from GLThread thread or it is a guaranteed
      // deadlock!
       synchronized {
         mDone = true
         notify()
       }
       try {
         join()
       } catch {
         case ex: InterruptedException =>
           Thread.currentThread.interrupt()
       }
    }

    /**
     * Queue an "event" to be run on the GL rendering thread.
     * @param r the runnable to be run on the GL rendering thread.
     */
    def queueEvent(r: Runnable) = synchronized {
      mEventQueue += r
    }

    private def getEvent: Runnable = synchronized {
      if (mEventQueue.size > 0) mEventQueue remove 0
      else null
    }

    private var mDone: Boolean = false
    private var mPaused: Boolean = _
    private var mHasFocus: Boolean = _
    private var mHasSurface: Boolean = _
    private var mContextLost: Boolean = _
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val mEventQueue = new ListBuffer[Runnable]()
    private var mEglHelper: EglHelper = _
  }

  private object GLThread {
    private final val sEglSemaphore = new Semaphore(1)
  }

  private var mSizeChanged = true

  private var mHolder: SurfaceHolder = _
  private var mGLThread: GLThread = _
  private var mGLWrapper: GLWrapper = _
}

object GLView {

  trait GLWrapper {
    def wrap(gl: GL): GL
  }

  /**
   * A generic renderer interface.
   */
  trait Renderer {

    /**
     * @return the EGL configuration specification desired by the renderer.
     */
    def getConfigSpec: Array[Int]

    /**
     * Surface created.
     * Called when the surface is created. Called when the application
     * starts, and whenever the GPU is reinitialized. This will
     * typically happen when the device awakes after going to sleep.
     * Set your textures here.
     */
    def surfaceCreated(gl: GL10)

    /**
     * Surface changed size.
     * Called after the surface is created and whenever
     * the OpenGL ES surface size changes. Set your viewport here.
     * @param gl
     * @param width
     * @param height
     */
    def sizeChanged(gl: GL10, width: Int, height: Int)
    /**
     * Draw the current frame.
     * @param gl
     */
    def drawFrame(gl: GL10)
  }

}

