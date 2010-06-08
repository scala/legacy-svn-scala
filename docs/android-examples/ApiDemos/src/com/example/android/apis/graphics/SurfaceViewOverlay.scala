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

package com.example.android.apis.graphics

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R

/**
 * Demonstration of overlays placed on top of a SurfaceView.
 */
class SurfaceViewOverlay extends Activity {
  private var mVictimContainer: View = _
  private var mVictim1: View = _
  private var mVictim2: View = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.surface_view_overlay)

    val glSurfaceView =
      findViewById(R.id.glsurfaceview).asInstanceOf[GLSurfaceView]
    glSurfaceView setRenderer new CubeRenderer(false)

    // Find the views whose visibility will change
    mVictimContainer = findViewById(R.id.hidecontainer)
    mVictim1 = findViewById(R.id.hideme1)
    mVictim1 setOnClickListener new HideMeListener(mVictim1)
    mVictim2 = findViewById(R.id.hideme2)
    mVictim2 setOnClickListener new HideMeListener(mVictim2)

    // Find our buttons
    val visibleButton = findViewById(R.id.vis).asInstanceOf[Button]
    val invisibleButton = findViewById(R.id.invis).asInstanceOf[Button]
    val goneButton = findViewById(R.id.gone).asInstanceOf[Button]

    // Wire each button to a click listener
    visibleButton setOnClickListener mVisibleListener
    invisibleButton setOnClickListener mInvisibleListener
    goneButton setOnClickListener mGoneListener
  }

  override protected def onResume() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onResume()
  }

  override protected def onPause() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onPause()
  }

  private class HideMeListener(val target: View) extends OnClickListener {
    def onClick(v: View) {
      target setVisibility View.INVISIBLE
    }
  }

  private val mVisibleListener = new OnClickListener() {
    def onClick(v: View) {
      mVictim1 setVisibility View.VISIBLE
      mVictim2 setVisibility View.VISIBLE
      mVictimContainer setVisibility View.VISIBLE
    }
  }

  private val mInvisibleListener = new OnClickListener() {
    def onClick(v: View) {
      mVictim1 setVisibility View.INVISIBLE
      mVictim2 setVisibility View.INVISIBLE
      mVictimContainer setVisibility View.INVISIBLE
    }
  }

  private val mGoneListener = new OnClickListener() {
    def onClick(v: View) {
      mVictim1 setVisibility View.GONE
      mVictim2 setVisibility View.GONE
      mVictimContainer setVisibility View.GONE
    }
  }
}
