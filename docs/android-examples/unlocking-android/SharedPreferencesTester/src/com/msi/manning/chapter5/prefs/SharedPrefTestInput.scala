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
import android.content.{Context, Intent, SharedPreferences}
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, Toast}

class SharedPrefTestInput extends Activity {
  import SharedPrefTestInput._  // companion object

  private var inputPrivate: EditText = _
  private var inputWorldRead: EditText = _
  private var inputWorldWrite: EditText = _
  private var inputWorldReadWrite: EditText = _
  private var button: Button = _

  private var prefsPrivate: SharedPreferences = _
  private var prefsWorldRead: SharedPreferences = _
  private var prefsWorldWrite: SharedPreferences = _
  private var prefsWorldReadWrite: SharedPreferences = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.shared_preftest_input)

    inputPrivate = findView(R.id.input_private)
    inputWorldRead = findView(R.id.input_worldread)
    inputWorldWrite = findView(R.id.input_worldwrite)
    inputWorldReadWrite = findView(R.id.input_worldreadwrite)
    button = findView(R.id.prefs_test_button)

    button setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        val valid = validate()
        if (valid) {
          prefsPrivate =
            getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE)
          prefsWorldRead =
            getSharedPreferences(PREFS_WORLD_READ, Context.MODE_WORLD_READABLE)
          prefsWorldWrite =
            getSharedPreferences(PREFS_WORLD_WRITE, Context.MODE_WORLD_WRITEABLE)
          prefsWorldReadWrite =
            getSharedPreferences(PREFS_WORLD_READ_WRITE,
              Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)

          val prefsPrivateEditor = prefsPrivate.edit()
          val prefsWorldReadEditor = prefsWorldRead.edit()
          val prefsWorldWriteEditor = prefsWorldWrite.edit()
          val prefsWorldReadWriteEditor = prefsWorldReadWrite.edit()

          prefsPrivateEditor.putString(KEY_PRIVATE,
            inputPrivate.getText.toString)
          prefsWorldReadEditor.putString(KEY_WORLD_READ,
            inputWorldRead.getText.toString)
          prefsWorldWriteEditor.putString(KEY_WORLD_WRITE,
            inputWorldWrite.getText.toString)
          prefsWorldReadWriteEditor.putString(KEY_WORLD_READ_WRITE,
            inputWorldReadWrite.getText.toString)

          prefsPrivateEditor.commit()
          prefsWorldReadEditor.commit()
          prefsWorldWriteEditor.commit()
          prefsWorldReadWriteEditor.commit()

          val intent = new Intent(SharedPrefTestInput.this,
                                  classOf[SharedPrefTestOutput])
          startActivity(intent)
        }
      }
    }
  }

  private def validate(): Boolean = {
    var valid = true
    val sb = new StringBuffer()
    sb append "Validation failed: \n"

    if (isEmpty(inputPrivate)) {
      sb.append("First input, private pref, must be present.\n")
      valid = false
    }
    if (isEmpty(inputWorldRead)) {
      sb.append("Second input, world read pref, must be present.\n")
      valid = false
    }
    if (isEmpty(inputWorldWrite)) {
      sb.append("Third input, world write pref, must be present.\n")
      valid = false
    }
    if (isEmpty(inputWorldReadWrite)) {
      sb.append("Fourth input, world read write pref, must be present.\n")
      valid = false
    }

    if (!valid) {
      Toast.makeText(this, sb.toString, Toast.LENGTH_SHORT).show()
    }
    valid
  }

  @inline
  private final def findView[V <: View](id: Int) =
    findViewById(id).asInstanceOf[V]
}

object SharedPrefTestInput {
  final val PREFS_PRIVATE = "PREFS_PRIVATE"
  final val PREFS_WORLD_READ = "PREFS_WORLD_READABLE"
  final val PREFS_WORLD_WRITE = "PREFS_WORLD_WRITABLE"
  final val PREFS_WORLD_READ_WRITE = "PREFS_WORLD_READABLE_WRITABLE"

  final val KEY_PRIVATE = "KEY_PRIVATE"
  final val KEY_WORLD_READ = "KEY_WORLD_READ"
  final val KEY_WORLD_WRITE = "KEY_WORLD_WRITE"
  final val KEY_WORLD_READ_WRITE = "KEY_WORLD_READ_WRITE"

  private def isEmpty(et: EditText): Boolean = {
    val s = et.getText.toString
    (et == null) || (s == null) || s.equals("")
  }
}
