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

package com.msi.manning.chapter5.prefs

import android.app.Activity
import android.content.{Context, SharedPreferences}
import android.os.Bundle
import android.view.View
import android.widget.TextView

class SharedPrefTestOutput extends Activity {

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
    import SharedPrefTestInput._
    super.onStart()
    prefsPrivate = getSharedPreferences(
      PREFS_PRIVATE, Context.MODE_PRIVATE)
    prefsWorldRead = getSharedPreferences(
      PREFS_WORLD_READ, Context.MODE_WORLD_READABLE)
    prefsWorldWrite = getSharedPreferences(
      PREFS_WORLD_WRITE, Context.MODE_WORLD_WRITEABLE)
    prefsWorldReadWrite = getSharedPreferences(
      PREFS_WORLD_READ_WRITE,
      Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)

    outputPrivate setText
      prefsPrivate.getString(KEY_PRIVATE, "NA")
    outputWorldRead setText
      prefsWorldRead.getString(KEY_WORLD_READ, "NA")
    outputWorldWrite setText
      prefsWorldWrite.getString(KEY_WORLD_WRITE, "NA")
    outputWorldReadWrite setText
      prefsWorldReadWrite.getString(KEY_WORLD_READ_WRITE, "NA")
  }

  @inline
  private final def findView[V <: View](id: Int) =
    findViewById(id).asInstanceOf[V]
}
