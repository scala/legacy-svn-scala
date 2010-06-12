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

package com.example.android.apis.animation

import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.widget.{ListView, ArrayAdapter, AdapterView, ImageView}
import android.view.{View, ViewGroup}
import android.view.animation.{Animation, AccelerateInterpolator,
                               DecelerateInterpolator}

/**
 * This sample application shows how to use layout animation and various
 * transformations on views. The result is a 3D transition between a
 * ListView and an ImageView. When the user clicks the list, it flips to
 * show the picture. When the user clicks the picture, it flips to show the
 * list. The animation is made of two smaller animations: the first half
 * rotates the list by 90 degrees on the Y axis and the second half rotates
 * the picture by 90 degrees on the Y axis. When the first half finishes, the
 * list is made invisible and the picture is set visible.
 */
object Transition3d {
  // Names of the photos we show in the list
  private final val PHOTOS_NAMES = Array(
    "Lyon",
    "Livermore",
    "Tahoe Pier",
    "Lake Tahoe",
    "Grand Canyon",
    "Bodie")

  // Resource identifiers for the photos we want to display
  private final val PHOTOS_RESOURCES = Array(
    R.drawable.photo1,
    R.drawable.photo2,
    R.drawable.photo3,
    R.drawable.photo4,
    R.drawable.photo5,
    R.drawable.photo6)
}

class Transition3d extends Activity
                      with AdapterView.OnItemClickListener
                      with View.OnClickListener {
  import Transition3d._  // companion object

  private var mPhotosList: ListView = _
  private var mContainer: ViewGroup = _
  private var mImageView: ImageView = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.animations_main_screen)

    mPhotosList = findViewById(android.R.id.list).asInstanceOf[ListView]
    mImageView = findViewById(R.id.picture).asInstanceOf[ImageView]
    mContainer = findViewById(R.id.container).asInstanceOf[ViewGroup]

    // Prepare the ListView
    val adapter = new ArrayAdapter[String](this,
                android.R.layout.simple_list_item_1, PHOTOS_NAMES)

    mPhotosList setAdapter adapter
    mPhotosList setOnItemClickListener this

    // Prepare the ImageView
    mImageView setClickable true
    mImageView setFocusable true
    mImageView setOnClickListener this

    // Since we are caching large views, we want to keep their cache
    // between each animation
    mContainer setPersistentDrawingCache ViewGroup.PERSISTENT_ANIMATION_CACHE
  }

  /**
   * Setup a new 3D rotation on the container view.
   *
   * @param position the item that was clicked to show a picture, or -1 to show the list
   * @param start the start angle at which the rotation must begin
   * @param end the end angle of the rotation
   */
  private def applyRotation(position: Int, start: Float, end: Float) {
    // Find the center of the container
    val centerX = mContainer.getWidth / 2.0f
    val centerY = mContainer.getHeight / 2.0f

    // Create a new 3D rotation with the supplied parameter
    // The animation listener is used to trigger the next animation
    val rotation =
      new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true)
    rotation setDuration 500
    rotation setFillAfter true
    rotation setInterpolator new AccelerateInterpolator
    rotation setAnimationListener new DisplayNextView(position)

    mContainer startAnimation rotation
  }

  def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
    // Pre-load the image then start the animation
    mImageView setImageResource PHOTOS_RESOURCES(position)
    applyRotation(position, 0, 90)
  }

  def onClick(v: View) {
    applyRotation(-1, 180, 90)
  }

  /**
   * This class listens for the end of the first half of the animation.
   * It then posts a new action that effectively swaps the views when the
   * container is rotated 90 degrees and thus invisible.
   */
  private final class DisplayNextView(mPosition: Int)
                extends AnyRef with Animation.AnimationListener {

    def onAnimationStart(animation: Animation) {
    }

    def onAnimationEnd(animation: Animation) {
      mContainer post new SwapViews(mPosition)
    }

    def onAnimationRepeat(animation: Animation) {
    }
  }

  /**
   * This class is responsible for swapping the views and start the second
   * half of the animation.
   */
  private final class SwapViews(mPosition: Int) extends Runnable {

    def run() {
      val centerX = mContainer.getWidth / 2.0f
      val centerY = mContainer.getHeight / 2.0f
      val rotation =
        if (mPosition > -1) {
          mPhotosList setVisibility View.GONE
          mImageView setVisibility View.VISIBLE
          mImageView.requestFocus()

          new Rotate3dAnimation(90, 180, centerX, centerY, 310.0f, false)
        } else {
          mImageView setVisibility View.GONE
          mPhotosList setVisibility View.VISIBLE
          mPhotosList.requestFocus()

          new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false)
        }

        rotation setDuration 500
        rotation setFillAfter true
        rotation setInterpolator new DecelerateInterpolator

        mContainer startAnimation rotation
    }
  }

}
