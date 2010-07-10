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

package com.beust.android.translate

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Performs language translation.
 * 
 * @author Daniel Rall
 */
class TranslateService extends Service {
  import TranslateService._  // companion object

  private final val mBinder = new ITranslate.Stub() {
    /**
     * Translates text from a given language to another given language
     * using Google Translate.
     * 
     * @param text The text to translate.
     * @param from The language code to translate from.
     * @param to The language code to translate to.
     * @return The translated text, or <code>null</code> on error.
     */
    def translate(text: String, from: String, to: String): String =
      try {
        Translate.translate(text, from, to)
      } catch {
        case e: Exception =>
          Log.e(TAG, "Failed to perform translation: " + e.getMessage)
          null
      }

    /**
     * @return The service version number.
     */
    def getVersion: Int = 1
  }

  override def onBind(intent: Intent): IBinder = {
    for (i <- 0 until TRANSLATE_ACTIONS.length) {
      if (TRANSLATE_ACTIONS(i) equals intent.getAction) {
        return mBinder
      }
    }
    null
  }

}

object TranslateService {

  final val TAG = "TranslateService"

  private final val TRANSLATE_ACTIONS = Array(
    Intent.ACTION_GET_CONTENT,
    Intent.ACTION_PICK,
    Intent.ACTION_VIEW
  )

}
