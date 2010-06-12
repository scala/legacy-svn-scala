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
import android.app.Activity._
import android.content.{Intent, SharedPreferences}
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

/**
 * Entry into our redirection example, describing what will happen.
 */
object RedirectMain {
  final val INIT_TEXT_REQUEST = 0
  final val NEW_TEXT_REQUEST = 1
}

class RedirectMain extends Activity {
  import RedirectMain._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.redirect_main)

    // Watch for button clicks.
    val clearButton = findViewById(R.id.clear).asInstanceOf[Button]
    clearButton setOnClickListener mClearListener
    val newButton = findViewById(R.id.newView).asInstanceOf[Button]
    newButton setOnClickListener mNewListener

    // Retrieve the current text preference.  If there is no text
    // preference set, we need to get it from the user by invoking the
    // activity that retrieves it.  To do this cleanly, we will
    // temporarily hide our own activity so it is not displayed until the
    // result is returned.
    if (!loadPrefs()) {
      val intent = new Intent(this, classOf[RedirectGetter])
      startActivityForResult(intent, INIT_TEXT_REQUEST)
    }
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int,
		                          data: Intent) {
    if (requestCode == INIT_TEXT_REQUEST) {

      // If the request was cancelled, then we are cancelled as well.
      if (resultCode == RESULT_CANCELED) {
        finish()

        // Otherwise, there now should be text...  reload the prefs,
        // and show our UI.  (Optionally we could verify that the text
        // is now set and exit if it isn't.)
      } else {
        loadPrefs()
      }

    } else if (requestCode == NEW_TEXT_REQUEST) {

      // In this case we are just changing the text, so if it was
      // cancelled then we can leave things as-is.
      if (resultCode != RESULT_CANCELED) {
        loadPrefs()
      }

    }
  }

  private final def loadPrefs(): Boolean = {
    // Retrieve the current redirect values.
    // NOTE: because this preference is shared between multiple
    // activities, you must be careful about when you read or write
    // it in order to keep from stepping on yourself.
    val preferences = getSharedPreferences("RedirectData", 0)

    mTextPref = preferences.getString("text", null)
    if (mTextPref != null) {
      val text = findViewById(R.id.text).asInstanceOf[TextView]
      text setText mTextPref
      true
    } else
      false
  }

  private val mClearListener = new OnClickListener {
    def onClick(v: View) {
      // Erase the preferences and exit!
      val preferences = getSharedPreferences("RedirectData", 0)
      preferences.edit().remove("text").commit()
      finish()
    }
  }

  private val mNewListener = new OnClickListener {
    def onClick(v: View) {
      // Retrieve new text preferences.
      val intent = new Intent(RedirectMain.this, classOf[RedirectGetter])
      startActivityForResult(intent, NEW_TEXT_REQUEST)
    }
  }

  private var mTextPref: String = _
}
