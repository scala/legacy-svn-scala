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
import android.content.{Intent, SharedPreferences}
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.{View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{Button, LinearLayout, TextView, Toast}
import android.widget.LinearLayout.LayoutParams

/**
 * Demonstrates launching a PreferenceActivity and grabbing a value it saved.
 */
object LaunchingPreferences {
  private final val REQUEST_CODE_PREFERENCES = 1
}

class LaunchingPreferences extends Activity with OnClickListener {
  import LaunchingPreferences._  // companion object

  private var mCounterText: TextView = _
    
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    /*
     * If this were my app's main activity, I would load the default values
     * so they're set even if the user does not go into the preferences
     * screen. Another good place to call this method would be from a
     * subclass of Application, so your default values would be loaded
     * regardless of entry into your application (for example, a service or
     * activity).
     */  
    PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false)
        
    // Simple layout
    val layout = new LinearLayout(this)
    layout setOrientation LinearLayout.VERTICAL
    setContentView(layout)
        
    // Create a simple button that will launch the preferences
    val launchPreferences = new Button(this)
    launchPreferences setText getString(R.string.launch_preference_activity)
    launchPreferences setOnClickListener this
    layout.addView(launchPreferences,
      new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                       ViewGroup.LayoutParams.WRAP_CONTENT))
        
    mCounterText = new TextView(this)
    layout.addView(mCounterText,
      new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                       ViewGroup.LayoutParams.WRAP_CONTENT))
        
    updateCounterText()
  }

  def onClick(v: View) {
    // When the button is clicked, launch an activity through this intent
    val launchPreferencesIntent =
      new Intent().setClass(this, classOf[AdvancedPreferences])
        
    // Make it a subactivity so we know when it returns
    startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES)
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int,
                                          data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
        
    // The preferences returned if the request code is what we had given
    // earlier in startSubActivity
    if (requestCode == REQUEST_CODE_PREFERENCES) {
      // Read a sample value they have set
      updateCounterText()
    }
  }

  private def updateCounterText() {
    // Since we're in the same package, we can use this context to get
    // the default shared preferences
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
    val counter = sharedPref.getInt(AdvancedPreferences.KEY_MY_PREFERENCE, 0)
    mCounterText.setText(getString(R.string.counter_value_is) + " " + counter)
  }
}
