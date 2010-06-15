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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.{AdapterView, ArrayAdapter, Spinner, ViewFlipper}

class Animation2 extends Activity
                    with AdapterView.OnItemSelectedListener {
  import Animation2._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.animation_2)

    mFlipper = findViewById(R.id.flipper).asInstanceOf[ViewFlipper]
    mFlipper.startFlipping()

    val s = findViewById(R.id.spinner).asInstanceOf[Spinner]
    val adapter = new ArrayAdapter[String](this,
                android.R.layout.simple_spinner_item, mStrings);
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    s setAdapter adapter
    s setOnItemSelectedListener this
  }

  def onItemSelected(parent: AdapterView[_], v: View, position: Int, id: Long) {
    val (in, out) = position match {
      case 0 =>
        (R.anim.push_up_in, R.anim.push_up_out)
      case 1 =>
        (R.anim.push_left_in, R.anim.push_left_out)
      case 2 =>
        (android.R.anim.fade_in, android.R.anim.fade_out)
      case _ =>
        (R.anim.hyperspace_in, R.anim.hyperspace_out)
    }
    mFlipper setInAnimation AnimationUtils.loadAnimation(this, in)
    mFlipper setOutAnimation AnimationUtils.loadAnimation(this, out)
  }

  def onNothingSelected(parent: AdapterView[_]) {
  }

  private var mFlipper: ViewFlipper = _

}

object Animation2 {
    private final val mStrings = Array(
      "Push up", "Push left", "Cross fade", "Hyperspace")
}
