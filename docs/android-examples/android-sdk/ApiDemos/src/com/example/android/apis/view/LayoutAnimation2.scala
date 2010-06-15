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

import android.app.ListActivity
import android.os.Bundle
import android.view.animation.{AlphaAnimation, Animation, AnimationSet,
                               LayoutAnimationController, TranslateAnimation}
import android.widget.{ArrayAdapter, ListView}

class LayoutAnimation2 extends ListActivity {
  import LayoutAnimation2._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setListAdapter(new ArrayAdapter[String](this,
                android.R.layout.simple_list_item_1, mStrings))

    val set = new AnimationSet(true)

    var animation: Animation = new AlphaAnimation(0.0f, 1.0f)
    animation setDuration 50
    set addAnimation animation

    animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        )
    animation setDuration 100
    set addAnimation animation

    val controller = new LayoutAnimationController(set, 0.5f)        
    getListView setLayoutAnimation controller
  }

}

object LayoutAnimation2 {

  private val mStrings = Array(
    "Bordeaux", "Lyon", "Marseille", "Nancy",
    "Paris", "Toulouse", "Strasbourg")
}
