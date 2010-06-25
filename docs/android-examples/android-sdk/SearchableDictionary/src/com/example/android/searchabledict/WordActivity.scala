/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.searchabledict

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/**
 * Displays a word and its definition.
 */
class WordActivity extends Activity {

  private var mWord: TextView = _
  private var mDefinition: TextView = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.word)

    mWord = findViewById(R.id.word).asInstanceOf[TextView]
    mDefinition = findViewById(R.id.definition).asInstanceOf[TextView]

    val intent = getIntent

    val word = intent getStringExtra "word"
    val definition = intent getStringExtra "definition"

    mWord setText word
    mDefinition setText definition
  }
}
