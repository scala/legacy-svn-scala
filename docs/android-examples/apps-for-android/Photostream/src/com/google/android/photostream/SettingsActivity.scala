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


package com.google.android.photostream

import android.os.Bundle
import android.preference.PreferenceActivity
import android.content.{Context, Intent}

class SettingsActivity extends PreferenceActivity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    getPreferenceManager setSharedPreferencesName Preferences.NAME
    addPreferencesFromResource(R.xml.preferences)
  }

}

object SettingsActivity {

  /**
   * Starts the PreferencesActivity for the specified user.
   *
   * @param context The application's environment.
   */
  def show(context: Context) {
    val intent = new Intent(context, classOf[SettingsActivity])
    context startActivity intent
  }

}
