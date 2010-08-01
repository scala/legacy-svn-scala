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

import scala.android.app.Activity

import android.os.Bundle
import android.util.Log
import android.view.{Menu, MenuItem, Window}
import android.widget.TextView

/** <p>
 *    This is a simple LunarLander activity that houses a single LunarView.
 *  </p>
 *  <ul>
 *    <li>animating by calling invalidate() from draw()</li>
 *    <li>loading and drawing resources</li>
 *    <li>handling onPause() in an animation</li>
 *  </ul>
 */
class LunarLander extends Activity {
  import LunarThread._

  private object MenuId extends Enumeration {
    val EASY, HARD, MEDIUM, PAUSE, RESUME, START, STOP = Value
  }
  private type MenuId = MenuId.Value

  /** A handle to the thread that's actually running the animation. */
  private var mLunarThread: LunarThread = null

  /** A handle to the View in which the game is running. */
  private var mLunarView: LunarView = null

  /** Invoked during init to give the Activity a chance to set up its Menu.
   * 
   * @param menu the Menu to which entries may be added
   * @return true
   */
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    menu.add(0, MenuId.START.id,  0, R.string.menu_start)
    menu.add(0, MenuId.STOP.id,   0, R.string.menu_stop)
    menu.add(0, MenuId.PAUSE.id,  0, R.string.menu_pause)
    menu.add(0, MenuId.RESUME.id, 0, R.string.menu_resume)
    menu.add(0, MenuId.EASY.id,   0, R.string.menu_easy)
    menu.add(0, MenuId.MEDIUM.id, 0, R.string.menu_medium)
    menu.add(0, MenuId.HARD.id,   0, R.string.menu_hard)

    true
  }

  /** Invoked when the user selects an item from the Menu.
   * 
   *  @param item the Menu entry which was selected
   *  @return true if the Menu item was legit (and we consumed it), false
   *          otherwise
   */
  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case MenuId.START =>
        mLunarThread.doStart()
        true
      case MenuId.STOP =>
        mLunarThread.setState(State.LOSE, getText(R.string.message_stopped))
        true
      case MenuId.PAUSE =>
        mLunarThread.pause()
        true
      case MenuId.RESUME =>
        mLunarThread.unpause()
        true
      case MenuId.EASY =>
        mLunarThread setDifficulty Difficulty.EASY
        true
      case MenuId.MEDIUM =>
        mLunarThread setDifficulty Difficulty.MEDIUM
        true
      case MenuId.HARD =>
        mLunarThread setDifficulty Difficulty.HARD
        true
      case _ =>
        false
    }

  /** Invoked when the Activity is created.
   * 
   *  @param savedInstanceState a Bundle containing state saved from a previous
   *        execution, or null if this is a new execution
   */
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // turn off the window's title bar
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    // tell system to use the layout defined in our XML file
    setContentView(R.layout.lunar_layout)

    // get handles to the LunarView from XML, and its LunarThread
    mLunarView = findView(R.id.lunar)
    mLunarThread = mLunarView.getThread

    // give the LunarView a handle to the TextView used for messages
    mLunarView setTextView findView(R.id.text)

    if (savedInstanceState == null) {
      // we were just launched: set up a new game
      mLunarThread setState State.READY
      Log.w(this.getClass.getName, "SIS is null")
    } else {
      // we are being restored: resume a previous game
      mLunarThread restoreState savedInstanceState
      Log.w(this.getClass.getName, "SIS is nonnull")
    }
  }

  /** Invoked when the Activity loses user focus.
   */
  override protected def onPause() {
    super.onPause()
    mLunarView.getThread.pause() // pause game when Activity pauses
  }

  /** Notification that something is about to happen, to give the Activity a
   *  chance to save state.
   * 
   * @param outState a Bundle into which this Activity should save its state
   */
  override protected def onSaveInstanceState(outState: Bundle) {
    // just have the View's thread save its state into our Bundle
    super.onSaveInstanceState(outState)
    mLunarThread saveState outState
    Log.w(this.getClass.getName, "SIS called")
  }

}
