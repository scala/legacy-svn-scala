/*
 * Copyright (C) 2009 Manning Publications Co.
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

package com.msi.manning.chapter9.xmlanimate

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView

import java.util.{Timer, TimerTask}

class XMLAnimate extends Activity {

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)

    val img: ImageView = findView(R.id.simple_anim)
    img setBackgroundResource R.anim.simple_animation
        
    val mar = new MyAnimationRoutine()
    val mar2 = new MyAnimationRoutine2()

    val t = new Timer(false)
    t.schedule(mar, 100)
    val t2 = new Timer(false)
    t2.schedule(mar2, 5000) 
  }

  private class MyAnimationRoutine extends TimerTask {
    override def run() {
      val img: ImageView = findView(R.id.simple_anim)
      val frameAnimation = img.getBackground.asInstanceOf[AnimationDrawable]
      frameAnimation.start()
    }
  }

  private class MyAnimationRoutine2 extends TimerTask {
    override def run() {
      val img: ImageView = findView(R.id.simple_anim)
      val frameAnimation = img.getBackground.asInstanceOf[AnimationDrawable]
      frameAnimation.stop()
    }
  }

  @inline
  private final def findView[V <: View](id: Int) =
    findViewById(id).asInstanceOf[V]

}
