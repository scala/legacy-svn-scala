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
import android.content.Context
import android.os.Bundle
import android.view.{View, ViewGroup}
import android.view.ViewGroup.LayoutParams
import android.widget.{BaseAdapter, Button, ImageView, AbsListView}

import scala.collection.mutable.ListBuffer


/**
 * A list view that demonstrates the use of setEmptyView. This example alos uses
 * a custom layout file that adds some extra buttons to the screen.
 */
class List8 extends ListActivity {

  private var mAdapter: PhotoAdapter = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Use a custom layout file
    setContentView(R.layout.list_8)

    // Tell the list view which view to display when the list is empty
    getListView setEmptyView findViewById(R.id.empty)

    // Set up our adapter
    mAdapter = new PhotoAdapter(this)
    setListAdapter(mAdapter)

    // Wire up the clear button to remove all photos
    val clear = findViewById(R.id.clear).asInstanceOf[Button]
    clear setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        mAdapter.clearPhotos()
      }
    }

    // Wire up the add button to add a new photo
    val add = findViewById(R.id.add).asInstanceOf[Button]
    add setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        mAdapter.addPhotos()
      }
    }
  }

  /**
   * A simple adapter which maintains an ArrayList of photo resource Ids. 
   * Each photo is displayed as an image. This adapter supports clearing the
   * list of photos and adding a new photo.
   *
   */
  class PhotoAdapter(context: Context) extends BaseAdapter {
    import PhotoAdapter._  // companion object

    private val mPhotos = new ListBuffer[Int]

    def getCount: Int = mPhotos.size

    def getItem(position: Int): AnyRef = position.asInstanceOf[AnyRef]

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      // Make an ImageView to show a photo
      val i = new ImageView(context)

      i setImageResource mPhotos(position)
      i setAdjustViewBounds true
      i setLayoutParams new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT)
      // Give it a nice background
      i setBackgroundResource R.drawable.picture_frame
      i
    }

    def clearPhotos() {
      mPhotos.clear()
      notifyDataSetChanged()
    }
        
    def addPhotos() {
      val whichPhoto = math.round(math.random * (mPhotoPool.length - 1)).toInt
      val newPhoto = mPhotoPool(whichPhoto)
      mPhotos append newPhoto
      notifyDataSetChanged()
    }

  }

  object PhotoAdapter {

    private val mPhotoPool = Array(
      R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
      R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
      R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
      R.drawable.sample_thumb_6, R.drawable.sample_thumb_7)
  }
}
