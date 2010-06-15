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
import android.view.{View, ViewGroup}
import android.widget.{AbsListView, BaseAdapter, GridView, ImageView}

/**
 * A grid that displays a set of framed photos.
 *
 */
class Grid2 extends Activity {
  import Grid2._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.grid_2)

    val g = findViewById(R.id.myGrid).asInstanceOf[GridView]
    g setAdapter new ImageAdapter(this)
  }

  private class ImageAdapter(context: Context) extends BaseAdapter {

    def getCount: Int = mThumbIds.length

    def getItem(position: Int): AnyRef = position.asInstanceOf[AnyRef]

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val imageView = if (convertView == null) {
        val i = new ImageView(context)
        i setLayoutParams new AbsListView.LayoutParams(45, 45)
        i setAdjustViewBounds false
        i setScaleType ImageView.ScaleType.CENTER_CROP
        i.setPadding(8, 8, 8, 8)
        i
      } else {
        convertView.asInstanceOf[ImageView]
      }

      imageView setImageResource mThumbIds(position)
      imageView
    }

  }

}

object Grid2 {

  private val mThumbIds = Array(
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
    R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
    R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
    R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
    R.drawable.sample_thumb_6, R.drawable.sample_thumb_7)

}
