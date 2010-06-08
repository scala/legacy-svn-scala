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

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.{Bundle, Handler}
import android.preference.{PreferenceActivity, CheckBoxPreference}
import android.widget.Toast

/**
 * Example that shows finding a preference from the hierarchy and a custom
 * preference type.
 */
object AdvancedPreferences {
  final val KEY_MY_PREFERENCE = "my_preference"
  final val KEY_ADVANCED_CHECKBOX_PREFERENCE = "advanced_checkbox_preference"
}

class AdvancedPreferences extends PreferenceActivity
                             with OnSharedPreferenceChangeListener {
  import AdvancedPreferences._  // companion object

  private var mCheckBoxPreference: CheckBoxPreference = _
  private val mHandler = new Handler
    
  /**
   * This is a simple example of controlling a preference from code.
   */
  private val mForceCheckBoxRunnable = new Runnable {
    def run() {
      if (mCheckBoxPreference != null) {
        mCheckBoxPreference setChecked !mCheckBoxPreference.isChecked
      }
            
      // Force toggle again in a second
      mHandler.postDelayed(this, 1000)
    }
  }

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Load the XML preferences file
    addPreferencesFromResource(R.xml.advanced_preferences)
        
    // Get a reference to the checkbox preference
    mCheckBoxPreference = 
      getPreferenceScreen.findPreference(KEY_ADVANCED_CHECKBOX_PREFERENCE)
                         .asInstanceOf[CheckBoxPreference]
  }

  override protected def onResume() {
    super.onResume()

    // Start the force toggle
    mForceCheckBoxRunnable.run()

    // Set up a listener whenever a key changes
    getPreferenceScreen.getSharedPreferences
      .registerOnSharedPreferenceChangeListener(this)
  }

  override protected def onPause() {
    super.onPause()

    // Unregister the listener whenever a key changes
    getPreferenceScreen.getSharedPreferences
      .unregisterOnSharedPreferenceChangeListener(this)

    mHandler removeCallbacks mForceCheckBoxRunnable
  }

  def onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                key: String) {
    // Let's do something when my counter preference value changes
    if (key.equals(KEY_MY_PREFERENCE)) {
      Toast.makeText(this, "Thanks! You increased my count to "
                     + sharedPreferences.getInt(key, 0), Toast.LENGTH_SHORT).show()
    }
  }
    
}
