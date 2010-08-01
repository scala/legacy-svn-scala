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

package com.example.android.lunarlander

import android.content.Context
import android.os.{Bundle, Handler, Message}
import android.util.AttributeSet
import android.view.{KeyEvent, SurfaceHolder, SurfaceView, View}
import android.widget.TextView


/** View that draws, takes keystrokes, etc. for a simple LunarLander game.
 * 
 *  Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 *  current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 *  updatePhysics() advances the physics based on realtime. draw() renders the
 *  ship, and does an invalidate() to prompt another draw() as soon as possible
 *  by the system.
 */
class LunarView(context: Context, attrs: AttributeSet)
extends SurfaceView(context, attrs) with SurfaceHolder.Callback {

  /** Pointer to the text view to display "Paused.." etc. */
  private var mStatusText: TextView = null

  /** The thread that actually draws the animation */
  private var thread: LunarThread = null

  initLunarView()

  private def initLunarView() {
    // register our interest in hearing about changes to our surface
    val holder = getHolder
    holder addCallback this

    // create thread only; it's started in surfaceCreated()
    thread = new LunarThread(holder, context, new Handler() {
      override def handleMessage(m: Message) {
        mStatusText setVisibility m.getData.getInt("viz")
        mStatusText setText m.getData.getString("text")
      }
    })
    setFocusable(true) // make sure we get key events
  }

  /** Fetches the animation thread corresponding to this LunarView.
   * 
   * @return the animation thread
   */
  def getThread: LunarThread = thread

  /** Standard override to get key-press events.
   */
  override def onKeyDown(keyCode: Int, msg: KeyEvent): Boolean =
    thread.doKeyDown(keyCode, msg)

  /** Standard override for key-up. We actually care about these, so we can
   *  turn off the engine or stop rotating.
   */
  override def onKeyUp(keyCode: Int, msg: KeyEvent): Boolean =
    thread.doKeyUp(keyCode, msg)

  /** Standard window-focus override. Notice focus lost so we can pause on
   *  focus lost. e.g. user switches to take a call.
   */
  override def onWindowFocusChanged(hasWindowFocus: Boolean) {
    if (!hasWindowFocus) thread.pause()
  }

  /** Installs a pointer to the text view used for messages.
   */
  def setTextView(textView: TextView) {
    mStatusText = textView
  }

  /* Callback invoked when the surface dimensions change. */
  def surfaceChanged(holder: SurfaceHolder, format: Int,
                     width: Int, height: Int) {
    thread.setSurfaceSize(width, height)
  }

  /** Callback invoked when the Surface has been created and is ready to be
   *  used.
   */
  def surfaceCreated(holder: SurfaceHolder) {
    // start the thread here so that we don't busy-wait in run()
    // waiting for the surface to be created
    thread setRunning true
    thread.start()
  }

  /** Callback invoked when the Surface has been destroyed and must no longer
   *  be touched. WARNING: after this method returns, the Surface/Canvas must
   *  never be touched again!
   */
  def surfaceDestroyed(holder: SurfaceHolder) {
    // we have to tell thread to shut down & wait for it to finish, or else
    // it might touch the Surface after we return and explode
    var retry = true
    thread setRunning false
    while (retry) {
      try {
        thread.join()
        retry = false
      } catch {
        case e: InterruptedException =>
      }
    }
  }

}
