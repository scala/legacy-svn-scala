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

package com.msi.manning.windwaves

import android.app.Activity
import android.content.Intent
import android.os.{Bundle, Handler, Message}
import android.util.Log

/**
 * Splash screen startup Activity.
 * 
 * @author charliecollins
 * 
 */
class StartActivity extends Activity {
  import StartActivity._  // companion object

  private final val handler = new Handler() {
    override def handleMessage(msg: Message) {
      startActivity(new Intent(StartActivity.this, classOf[MapViewActivity]))
    }
  }

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onCreate")

    setContentView(R.layout.start_activity)
  }

  override def onStart() {
    super.onStart();
    // move to the next screen via a delayed message
    val th = new Thread() {
      override def run() {
        handler.sendMessageDelayed(handler.obtainMessage, 3000)
      }
    }
    th.start()
  }

  override def onPause() {
    super.onPause()
  }

}

object StartActivity {
  private final val CLASSTAG = classOf[StartActivity].getSimpleName
}
