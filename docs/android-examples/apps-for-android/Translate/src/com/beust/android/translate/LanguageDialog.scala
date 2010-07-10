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

import com.beust.android.translate.Languages.Language

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.{LayoutInflater, View}
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.FILL_PARENT
import android.widget.{Button, LinearLayout, ScrollView}

/**
 * This dialog displays a list of languages and then tells the calling
 * activity which language was selected. 
 */
class LanguageDialog(mActivity: TranslateActivity) extends AlertDialog(mActivity)
                                                      with OnClickListener {
  private var mFrom: Boolean = _

  init()

  private def init() {
    val inflater = mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    val scrollView = inflater.inflate(R.layout.language_dialog, null).asInstanceOf[ScrollView]
    setView(scrollView)
        
    val layout = scrollView.findViewById(R.id.languages).asInstanceOf[LinearLayout]
        
    var current: LinearLayout = null
    val languages = Language.values
    for (i <- 0 until languages.size) {
      if (current != null) {
        layout.addView(current, new LayoutParams(FILL_PARENT, FILL_PARENT))
      }
      current = new LinearLayout(mActivity)
      current setOrientation LinearLayout.HORIZONTAL
      val button = inflater.inflate(R.layout.language_entry, current, false).asInstanceOf[Button]

      val language = Language(i).asInstanceOf[Language.Lang]
      language.configureButton(mActivity, button)
      button setOnClickListener this
      current.addView(button, button.getLayoutParams)
    }
    if (current != null) {
      layout.addView(current, new LayoutParams(FILL_PARENT, FILL_PARENT))
    }
    setTitle(" ")  // set later, but necessary to put a non-empty string here
  }

  private def log(s: String) {
    Log.d(TranslateActivity.TAG, s)
  }

  def onClick(v: View) {
    val lang = v.getTag.asInstanceOf[Language]
    mActivity.setNewLanguage(lang, mFrom, true /* translate */)
    dismiss()
  }

  def setFrom(from: Boolean) {
    log("From set to " + from)
    mFrom = from
    val id = if (from) R.string.translate_from else R.string.translate_to
    setTitle(mActivity getString id) 
  }

}
