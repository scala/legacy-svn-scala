/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.{ActivityInfo, PackageManager}
import android.content.pm.PackageManager.NameNotFoundException
import android.os.{Bundle, Handler, Message}

/**
 * PurgeableBitmap demonstrates the effects of setting Bitmaps as being
 * purgeable.
 *
 * In the NonPurgeable case, an encoded bitstream is decoded to a different
 * Bitmap over and over again up to 200 times until out-of-memory occurs.
 * In contrast, the Purgeable case shows that the system can complete decoding
 * the encoded bitstream 200 times without hitting the out-of-memory case.
 */
class PurgeableBitmap extends GraphicsActivity {

  private var mView: PurgeableBitmapView = _
  private val mRedrawHandler = new RefreshHandler()

  class RefreshHandler extends Handler {
    override def handleMessage(msg: Message) {
      val index = mView update this
      if (index > 0) {
        showAlertDialog(getDialogMessage(true, index))
      } else if (index < 0) {
        mView.invalidate()
        showAlertDialog(getDialogMessage(false, -index))
      } else {
        mView.invalidate()
      }
    }
    def sleep(delayMillis: Long) {
      this.removeMessages(0)
      sendMessageDelayed(obtainMessage(0), delayMillis)
    }
  }

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    mView = new PurgeableBitmapView(this, detectIfPurgeableRequest())
    mRedrawHandler sleep 0
    setContentView(mView)
  }

  private def detectIfPurgeableRequest(): Boolean = {
    val pm = getPackageManager()
    var labelSeq: CharSequence = null
    try {
      val info = pm.getActivityInfo(this.getComponentName,
              PackageManager.GET_META_DATA)
      labelSeq = info.loadLabel(pm)
    } catch {
      case e: NameNotFoundException =>
        e.printStackTrace()
        return false
    }

    val components = labelSeq.toString split "/"
    components(components.length - 1) equals "Purgeable"
  }

  private def getDialogMessage(isOutOfMemory: Boolean, index: Int): String = {
    val sb = new StringBuilder()
    if (isOutOfMemory) {
      sb.append("Out of memery occurs when the ")
        .append(index)
        .append("th Bitmap is decoded.")
    } else {
      sb.append("Complete decoding ")
        .append(index)
        .append(" bitmaps without running out of memory.")
    }
    sb.toString
  }

  private def showAlertDialog(message: String) {
    val builder = new AlertDialog.Builder(this)
    builder.setMessage(message)
           .setCancelable(false)
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              def onClick(dialog: DialogInterface, id: Int) {
              }
            })
    val alert: AlertDialog = builder.create()
    alert.show()
  }

}
