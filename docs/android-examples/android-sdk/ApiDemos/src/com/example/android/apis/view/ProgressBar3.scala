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

package com.example.android.apis.view

import com.example.android.apis.R

import android.app.{Activity, Dialog, ProgressDialog}
import android.os.Bundle
import android.view.View
import android.widget.Button

/**
 * Demonstrates the use of progress dialogs.  Uses {@link Activity#onCreateDialog}
 * and {@link Activity#showDialog} to ensure the dialogs will be properly saved
 * and restored.
 */
class ProgressBar3 extends Activity {
  import ProgressBar3._  // companion object

  private var mDialog1: ProgressDialog = _
  private var mDialog2: ProgressDialog = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.progressbar_3)

    var button = findViewById(R.id.showIndeterminate).asInstanceOf[Button]
    button setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(DIALOG1_KEY)
      }
    }

    button = findViewById(R.id.showIndeterminateNoTitle).asInstanceOf[Button]
    button setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(DIALOG2_KEY)
      }
    }
  }

  override protected def onCreateDialog(id: Int): Dialog =
    id match {
      case DIALOG1_KEY =>
        val dialog = new ProgressDialog(this)
        dialog setTitle "Indeterminate"
        dialog setMessage "Please wait while loading..."
        dialog setIndeterminate true
        dialog setCancelable true
        dialog
      case DIALOG2_KEY =>
        val dialog = new ProgressDialog(this)
        dialog setMessage "Please wait while loading..."
        dialog setIndeterminate true
        dialog setCancelable true
        dialog
      case _ =>
        null
    }
}

object ProgressBar3 {
  private final val DIALOG1_KEY = 0
  private final val DIALOG2_KEY = 1
}
