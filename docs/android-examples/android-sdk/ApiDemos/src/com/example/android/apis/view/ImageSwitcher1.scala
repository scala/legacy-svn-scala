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

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.{View, ViewGroup, Window}
import android.view.ViewGroup.LayoutParams
import android.view.animation.AnimationUtils
import android.widget.{AdapterView, BaseAdapter, Gallery, ImageSwitcher,
                       ImageView, ViewSwitcher}

class ImageSwitcher1 extends Activity
                        with AdapterView.OnItemSelectedListener
                        with ViewSwitcher.ViewFactory {
  import ImageSwitcher1._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.image_switcher_1)

    mSwitcher = findViewById(R.id.switcher).asInstanceOf[ImageSwitcher]
    mSwitcher setFactory this
    mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                             android.R.anim.fade_in))
    mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                              android.R.anim.fade_out))

    val g = findViewById(R.id.gallery).asInstanceOf[Gallery]
    g setAdapter new ImageAdapter(this)
    g setOnItemSelectedListener this
  }

  def onItemSelected(parent: AdapterView[_], v: View, position: Int, id: Long) {
    mSwitcher setImageResource mImageIds(position)
  }

  def onNothingSelected(parent: AdapterView[_]) {
  }

  def makeView(): View = {
    val i = new ImageView(this)
    i setBackgroundColor 0xFF000000
    i setScaleType ImageView.ScaleType.FIT_CENTER
    i setLayoutParams new /*ImageSwitcher*/ViewGroup.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)
    i
  }

  private var mSwitcher: ImageSwitcher = _

  class ImageAdapter(context: Context) extends BaseAdapter {

    def getCount: Int = mThumbIds.length

    def getItem(position: Int): AnyRef = position.asInstanceOf[AnyRef]

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val i = new ImageView(context)

      i setImageResource mThumbIds(position)
      i setAdjustViewBounds true
      i setLayoutParams new /*Gallery*/ViewGroup.LayoutParams(
                          LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
      i setBackgroundResource R.drawable.picture_frame
      i
    }

    private var mContext: Context = _

  }

}

object ImageSwitcher1 {
  private final val mThumbIds = Array(
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7)

  private final val mImageIds = Array(
    R.drawable.sample_0, R.drawable.sample_1, R.drawable.sample_2,
    R.drawable.sample_3, R.drawable.sample_4, R.drawable.sample_5,
    R.drawable.sample_6, R.drawable.sample_7)

}
