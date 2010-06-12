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

package com.example.android.apis.app

import com.example.android.apis.R

import android.app.Activity
import android.app.Activity._
import android.content.Intent
import android.content.pm.{PackageManager, ResolveInfo}
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, Button, ListView}

/**
 * Sample code that invokes the speech recognition intent API.
 */
object VoiceRecognition {
  private final val VOICE_RECOGNITION_REQUEST_CODE = 1234
}

class VoiceRecognition extends Activity with OnClickListener {
  import VoiceRecognition._  // companion object
    
  private var mList: ListView = _

  /**
   * Called with the activity is first created.
   */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Inflate our UI from its XML layout description.
    setContentView(R.layout.voice_recognition)

    // Get display items for later interaction
    val speakButton = findViewById(R.id.btn_speak).asInstanceOf[Button]
        
    mList = findViewById(R.id.list).asInstanceOf[ListView]

    // Check to see if a recognition activity is present
    val pm = getPackageManager
    val activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
    if (activities.size() != 0) {
      speakButton setOnClickListener this
    } else {
      speakButton setEnabled false
      speakButton setText "Recognizer not present"
    }
  }

  /**
   * Handle the click on the start recognition button.
   */
  def onClick(v: View) {
    if (v.getId() == R.id.btn_speak) {
      startVoiceRecognitionActivity()
    }
  }

  /**
   * Fire an intent to start the speech recognition activity.
   */
  private def startVoiceRecognitionActivity() {
    val intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo")
    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
  }

  /**
   * Handle the results from the recognition activity.
   */
  override protected def onActivityResult(requestCode: Int, resultCode: Int,
                                          data: Intent) {
    if (requestCode == VOICE_RECOGNITION_REQUEST_CODE &&
        resultCode == RESULT_OK) {
      // Fill the list view with the strings the recognizer thought it could have heard
      val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      mList.setAdapter(
        new ArrayAdapter[String](this, android.R.layout.simple_list_item_1,
                    matches))
    }

    super.onActivityResult(requestCode, resultCode, data)
  }
}
