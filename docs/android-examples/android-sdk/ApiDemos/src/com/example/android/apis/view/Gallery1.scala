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
import android.content.res.TypedArray
import android.os.Bundle
import android.view.{ContextMenu, MenuItem, View, ViewGroup}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.{AdapterView, BaseAdapter, Gallery, ImageView, Toast}
import android.widget.AdapterView.{AdapterContextMenuInfo, OnItemClickListener}

class Gallery1 extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.gallery_1)

    // Reference the Gallery view
    val g = findViewById(R.id.gallery).asInstanceOf[Gallery]
    // Set the adapter to our custom adapter (below)
    g setAdapter new ImageAdapter(this)
        
    // Set a item click listener, and just Toast the clicked position
    g setOnItemClickListener new OnItemClickListener() {
      def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
        Toast.makeText(Gallery1.this, "" + position, Toast.LENGTH_SHORT).show()
      }
    }
        
    // We also want to show context menu for longpressed items in the gallery
    registerForContextMenu(g)
  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
    menu.add(R.string.gallery_2_text)
  }
    
  override def onContextItemSelected(item: MenuItem): Boolean = {
    val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
    Toast.makeText(this, "Longpress: " + info.position, Toast.LENGTH_SHORT).show()
    true
  }

  class ImageAdapter(context: Context) extends BaseAdapter {
    private var mGalleryItemBackground: Int = _
        
    { // init
      // See res/values/attrs.xml for the <declare-styleable> that defines
      // Gallery1.
      val a = obtainStyledAttributes(R.styleable.Gallery1)
      mGalleryItemBackground = a.getResourceId(
                    R.styleable.Gallery1_android_galleryItemBackground, 0)
      a.recycle()
    }

    def getCount: Int = mImageIds.length

    def getItem(position: Int): AnyRef = position.asInstanceOf[AnyRef]

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val i = new ImageView(context)

      i setImageResource mImageIds(position)
      i setScaleType ImageView.ScaleType.FIT_XY
      i setLayoutParams new Gallery.LayoutParams(136, 88)

      // The preferred Gallery item background
      i setBackgroundResource mGalleryItemBackground

      i
    }

    private val mImageIds = Array(
      R.drawable.gallery_photo_1,
      R.drawable.gallery_photo_2,
      R.drawable.gallery_photo_3,
      R.drawable.gallery_photo_4,
      R.drawable.gallery_photo_5,
      R.drawable.gallery_photo_6,
      R.drawable.gallery_photo_7,
      R.drawable.gallery_photo_8)
  }

}
