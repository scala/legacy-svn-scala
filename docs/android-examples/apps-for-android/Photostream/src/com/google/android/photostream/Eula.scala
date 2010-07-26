/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.google.android.photostream

import scala.android.app.Activity

import android.app.AlertDialog
import android.content.{DialogInterface, SharedPreferences}

import java.io.{BufferedReader, Closeable, InputStreamReader, IOException}

/**
 * Displays an EULA ("End User License Agreement") that the user has to accept
 * before using the application. Your application should call
 * {@link Eula#showEula(android.app.Activity)} in the onCreate() method of the
 * first activity. If the user accepts the EULA, it will never
 * be shown again. If the user refuses, {@link android.app.Activity#finish()}
 * is invoked on your activity.
 */
object Eula {

  private final val PREFERENCE_EULA_ACCEPTED = "eula.accepted"
  private final val PREFERENCES_EULA = "eula"

  /**
   * Displays the EULA if necessary. This method should be called from the
   * onCreate() method of your main Activity.
   *
   * @param activity The Activity to finish if the user rejects the EULA.
   */
  def showEula(activity: Activity) {
    val preferences =
      activity.getSharedPreferences(PREFERENCES_EULA, Activity.MODE_PRIVATE)
    if (!preferences.getBoolean(PREFERENCE_EULA_ACCEPTED, false)) {
      val builder = new AlertDialog.Builder(activity)
      builder setTitle R.string.eula_title
      builder setCancelable true
      builder.setPositiveButton(R.string.eula_accept,
        new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which: Int) {
            accept(preferences)
          }
        })
      builder.setNegativeButton(R.string.eula_refuse,
        new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which: Int) {
            refuse(activity)
          }
        })
      builder setOnCancelListener new DialogInterface.OnCancelListener() {
        def onCancel(dialog: DialogInterface) {
          refuse(activity)
        }
      }
      // UNCOMMENT TO ENABLE EULA
      //builder setMessage readFile(activity, R.raw.eula)
      builder.create().show()
    }
  }

  private def accept(preferences: SharedPreferences) {
    preferences.edit().putBoolean(PREFERENCE_EULA_ACCEPTED, true).commit()
  }

  private def refuse(activity: Activity) {
    activity.finish()
  }

  def showDisclaimer(activity: Activity) {
    val builder = new AlertDialog.Builder(activity)
    builder setMessage readFile(activity, R.raw.disclaimer)
    builder setCancelable true
    builder setTitle R.string.disclaimer_title
    builder.setPositiveButton(R.string.disclaimer_accept, null)
    builder.create().show()
  }

  private def readFile(activity: Activity, id: Int): CharSequence = {
    var in: BufferedReader = null
    try {
      in = new BufferedReader(
        new InputStreamReader(activity.getResources openRawResource id))
      val buffer = new StringBuilder()
      var line = in.readLine()
      while (line != null) {
        buffer.append(line).append('\n')
        line = in.readLine()
      }
      buffer
    } catch {
      case e: IOException => ""
    } finally {
      closeStream(in)
    }
  }

  /**
   * Closes the specified stream.
   *
   * @param stream The stream to close.
   */
  private def closeStream(stream: Closeable) {
    if (stream != null) {
      try {
        stream.close()
      } catch {
        case e: IOException => // Ignore
      }
    }
  }
}
