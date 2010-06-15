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

import android.app.ListActivity
import android.os.Bundle
import android.widget.ArrayAdapter

class LayoutAnimation3 extends ListActivity {
  import LayoutAnimation3._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.layout_animation_3)
    setListAdapter(new ArrayAdapter[String](this,
                android.R.layout.simple_list_item_1, mStrings))
  }

}

object LayoutAnimation3 {
  private val mStrings = Array(
    "Bordeaux", "Lyon", "Marseille", "Nancy", "Paris", "Toulouse", "Strasbourg")
}
