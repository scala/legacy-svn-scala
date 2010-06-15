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

package com.example.android.apis.view

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.{AnimationUtils, Animation, TranslateAnimation}
import android.widget.{AdapterView, ArrayAdapter, Spinner}

class Animation3 extends Activity with AdapterView.OnItemSelectedListener {
  import Animation3._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.animation_3)

    val s = findViewById(R.id.spinner).asInstanceOf[Spinner]
    val adapter = new ArrayAdapter[String](this,
                android.R.layout.simple_spinner_item, INTERPOLATORS);
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    s setAdapter adapter
    s setOnItemSelectedListener this
  }

  def onItemSelected(parent: AdapterView[_], v: View, position: Int, id: Long) {
    val target = findViewById(R.id.target)
    val targetParent = target.getParent.asInstanceOf[View]

    val a = new TranslateAnimation(0.0f,
                targetParent.getWidth - target.getWidth - targetParent.getPaddingLeft -
                targetParent.getPaddingRight, 0.0f, 0.0f)
    a setDuration 1000
    a setStartOffset 300
    a setRepeatMode Animation.RESTART
    a setRepeatCount Animation.INFINITE

    val anim = position match {
      case 0 => android.R.anim.accelerate_interpolator
      case 1 => android.R.anim.decelerate_interpolator
      case 2 => android.R.anim.accelerate_decelerate_interpolator
      case 3 => android.R.anim.anticipate_interpolator
      case 4 => android.R.anim.overshoot_interpolator
      case 5 => android.R.anim.anticipate_overshoot_interpolator
      case 6 => android.R.anim.bounce_interpolator
      case _ => throw new Exception("Illegal position")
    }
    a setInterpolator AnimationUtils.loadInterpolator(this, anim)
    target startAnimation a
  }

  def onNothingSelected(parent: AdapterView[_]) {
  }
}

object Animation3 {

  private final val INTERPOLATORS = Array(
    "Accelerate", "Decelerate", "Accelerate/Decelerate",
    "Anticipate", "Overshoot", "Anticipate/Overshoot",
    "Bounce")
}
