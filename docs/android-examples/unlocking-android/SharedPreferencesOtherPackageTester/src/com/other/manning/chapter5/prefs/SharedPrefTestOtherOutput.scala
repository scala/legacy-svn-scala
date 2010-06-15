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

package com.other.manning.chapter5.prefs

import android.app.Activity
import android.content.{Context, SharedPreferences}
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView

class SharedPrefTestOtherOutput extends Activity {
  import SharedPrefTestOtherOutput._  // companion object

  private var outputPrivate: TextView = _
  private var outputWorldRead: TextView = _
  private var outputWorldWrite: TextView = _
  private var outputWorldReadWrite: TextView = _

  private var prefsPrivate: SharedPreferences = _
  private var prefsWorldRead: SharedPreferences = _
  private var prefsWorldWrite: SharedPreferences = _
  private var prefsWorldReadWrite: SharedPreferences = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.shared_preftest_output)

    outputPrivate = findView(R.id.output_private)
    outputWorldRead = findView(R.id.output_worldread)
    outputWorldWrite = findView(R.id.output_worldwrite)
    outputWorldReadWrite = findView(R.id.output_worldreadwrite)
  }

  override def onStart() {
    Log.v(SharedPrefTestOtherOutput.LOGTAG, "onStart")
    super.onStart()
    var otherAppsContext: Context = null;
    try {
      otherAppsContext = createPackageContext("com.msi.manning.chapter5.prefs", Context.MODE_WORLD_WRITEABLE);
    } catch {
      case e: NameNotFoundException =>
        Log.e(SharedPrefTestOtherOutput.LOGTAG, e.getLocalizedMessage)
    }

    prefsPrivate = otherAppsContext.getSharedPreferences(
      SharedPrefTestOtherOutput.PREFS_PRIVATE, 0)
    prefsWorldRead = otherAppsContext.getSharedPreferences(
      SharedPrefTestOtherOutput.PREFS_WORLD_READ, 0)
    prefsWorldWrite = otherAppsContext.getSharedPreferences(
      SharedPrefTestOtherOutput.PREFS_WORLD_WRITE, 0)
    prefsWorldReadWrite = otherAppsContext.getSharedPreferences(
      SharedPrefTestOtherOutput.PREFS_WORLD_READ_WRITE, 0)

    outputPrivate setText prefsPrivate.getString(SharedPrefTestOtherOutput.KEY_PRIVATE, "NA")
    outputWorldRead setText prefsWorldRead.getString(SharedPrefTestOtherOutput.KEY_WORLD_READ, "NA")
    outputWorldWrite setText prefsWorldWrite.getString(SharedPrefTestOtherOutput.KEY_WORLD_WRITE, "NA")
    outputWorldReadWrite setText prefsWorldReadWrite.getString(SharedPrefTestOtherOutput.KEY_WORLD_READ_WRITE, "NA")
  }

  @inline
  private final def findView[V <: View](id: Int) =
    findViewById(id).asInstanceOf[V]
}

object SharedPrefTestOtherOutput {

  final val PREFS_PRIVATE = "PREFS_PRIVATE"
  final val PREFS_WORLD_READ = "PREFS_WORLD_READABLE"
  final val PREFS_WORLD_WRITE = "PREFS_WORLD_WRITABLE"
  final val PREFS_WORLD_READ_WRITE = "PREFS_WORLD_READABLE_WRITABLE"

  final val KEY_PRIVATE = "KEY_PRIVATE"
  final val KEY_WORLD_READ = "KEY_WORLD_READ"
  final val KEY_WORLD_WRITE = "KEY_WORLD_WRITE"
  final val KEY_WORLD_READ_WRITE = "KEY_WORLD_READ_WRITE"

  private final val LOGTAG = "SharedPrefTestOutput"
}
