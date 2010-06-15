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

package com.msi.manning.chapter9.bounceyball

import android.app.Activity
import android.os.{Bundle, Handler, Message}
import android.view.Window

object BounceActivity {
  private final val GUIUPDATEIDENTIFIER = 0x101
}

class BounceActivity extends Activity {
  import BounceActivity._  // companion object

  private var myRefreshThread: Thread = _

  /* Our 'ball' is located within this View */
  private var myBounceView: BounceView = _

  private val myGUIUpdateHandler = new Handler() {
    override def handleMessage(msg: Message) {
      msg.what match {
        case BounceActivity.GUIUPDATEIDENTIFIER =>
          /* Repaint the BounceView (where the ball is in) */
          myBounceView.invalidate()
        case _ =>
      }
      super.handleMessage(msg)
    }
  }

  /** Called when the activity is first created. */
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    // Set fullscreen
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    // Create a view
    myBounceView = new BounceView(this)
    setContentView(myBounceView)

    /* create a Thread that will periodically send messages to our Handler */
    new Thread(new RefreshRunner()).start()
  }

  class RefreshRunner extends Runnable {
    override def run() {
      while (! Thread.currentThread.isInterrupted) {
        // Send Message to the Handler which will call the invalidate()
        // method of the BoucneView
        val message = new Message()
        message.what = BounceActivity.GUIUPDATEIDENTIFIER
        myGUIUpdateHandler sendMessage message

        try {
          Thread.sleep(100) // a 10th of a second
        } catch {
          case e: InterruptedException =>
            Thread.currentThread.interrupt()
        }
      }
    }
  }

}
