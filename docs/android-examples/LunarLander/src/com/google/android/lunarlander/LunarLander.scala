/* 
 * Copyright (C) 2007 Google Inc.
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
 
package com.google.android.lunarlander

import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.view.{Menu, Window}
import _root_.android.widget.TextView

/**
 * This is a simple LunarLander activity
 * that houses a single LunarView. It demonstrates...
 * <ul>
 * <li>animating by calling invalidate() from draw()
 * <li>loading and drawing resources
 * <li>handling onPause() in an animation
 * </ul>
 */
class LunarLander extends Activity {
  private var mLunarView: LunarView = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    // Turn off the title bar
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    // Make our view
    setContentView(R.layout.lunar_layout)
    mLunarView = findViewById(R.id.lunar).asInstanceOf[LunarView]
        
    // Tell the view about the text view
    mLunarView setTextView findViewById(R.id.text).asInstanceOf[TextView]

    if (icicle == null) {
      // We were just launched -- set up a new game
      mLunarView.mode = LunarView.READY 
    } else {
      // We are being restored
      val map = icicle getBundle "lunar-view"
      if (map != null) mLunarView restoreState map
      else mLunarView.mode = LunarView.READY
    }
  }

  override def onPause() {
    super.onPause()
    // Pause the came when our activity pauses
    mLunarView.doPause()
  }

  override def onFreeze(outState: Bundle) {
   // Remember game state
   outState.putBundle("lunar-view", mLunarView.saveState())
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    def run(b: => Unit) = new Runnable { def run() { mLunarView.doStart() } }
        
    menu.add(0, 0, R.string.menu_start, run { mLunarView.doStart() })
    menu.add(0, 0, R.string.menu_stop, run {
      mLunarView.setMode(LunarView.LOSE, LunarLander.this.getText(R.string.message_stopped))
    })
    menu.add(0, 0, R.string.menu_pause, run { mLunarView.doPause() })
    menu.add(0, 0, R.string.menu_resume, run { mLunarView.doResume() })
    menu.addSeparator(0, 0)
    menu.add(0, 0, R.string.menu_easy, run { mLunarView.difficulty = LunarView.EASY })
    menu.add(0, 0, R.string.menu_medium, run { mLunarView.difficulty = LunarView.MEDIUM })
    menu.add(0, 0, R.string.menu_hard, run { mLunarView.difficulty = LunarView.HARD })
    true
  }
}
