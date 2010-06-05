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

package com.example.android.apis.app

import com.example.android.apis.R

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

/**
 * Sub-activity that is executed by the redirection example when input is needed
 * from the user.
 */
class RedirectGetter extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.redirect_getter)

    // Watch for button clicks.
    val applyButton = findViewById(R.id.apply).asInstanceOf[Button]
    applyButton setOnClickListener mApplyListener

    // The text being set.
    mText = findViewById(R.id.text).asInstanceOf[TextView]
  }

  private final def loadPrefs(): Boolean = {
    // Retrieve the current redirect values.
    // NOTE: because this preference is shared between multiple
    // activities, you must be careful about when you read or write
    // it in order to keep from stepping on yourself.
    val preferences = getSharedPreferences("RedirectData", 0)

    mTextPref = preferences.getString("text", null)
    if (mTextPref != null) {
      mText setText mTextPref
      true
    } else
      false
  }

  private val mApplyListener = new OnClickListener() {
    def onClick(v: View) {
      val preferences = getSharedPreferences("RedirectData", 0)
      val editor = preferences.edit()
      editor.putString("text", mText.getText.toString)

      if (editor.commit()) {
        setResult(Activity.RESULT_OK)
      }

      finish()
    }
  }

  private var mTextPref: String = _
  private var mText: TextView = _
}

