/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.example.android.home

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.{MotionEvent, View, ViewGroup, Window}
import android.widget.{AdapterView, BaseAdapter, Gallery, ImageView}
import android.widget.Gallery.LayoutParams

import java.io.{IOException, InputStream}

/**
 * Wallpaper picker for the Home application. User can choose from
 * a gallery of stock photos.
 */
object Wallpaper {
  private final val LOG_TAG = "Home"

  private final val THUMB_IDS = Array(
    R.drawable.bg_android_icon,
    R.drawable.bg_sunrise_icon,
    R.drawable.bg_sunset_icon)

  private final val IMAGE_IDS = Array(
    R.drawable.bg_android,
    R.drawable.bg_sunrise,
    R.drawable.bg_sunset)
}

class Wallpaper extends Activity
                   with AdapterView.OnItemSelectedListener
                   with AdapterView.OnItemClickListener {
  import Wallpaper._  // companion object

  private var mGallery: Gallery = _
  private var mIsWallpaperSet: Boolean = _
        
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.wallpaper)

    mGallery = findViewById(R.id.gallery).asInstanceOf[Gallery]
    mGallery setAdapter new ImageAdapter(this)
    mGallery setOnItemSelectedListener this
    mGallery setOnItemClickListener this
  }
    
  override protected def onResume() {
    super.onResume()
    mIsWallpaperSet = false
  }

  def onItemSelected(parent: AdapterView[_], v: View, position: Int, id: Long) {
    getWindow setBackgroundDrawableResource IMAGE_IDS(position)
  }
    
  def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
    selectWallpaper(position)
  }

  /*
   * When using touch if you tap an image it triggers both the onItemClick and
   * the onTouchEvent causing the wallpaper to be set twice. Synchronize this
   * method and ensure we only set the wallpaper once.
   */
  private def selectWallpaper(position: Int): Unit = synchronized {
    if (mIsWallpaperSet) {
      return
    }
    mIsWallpaperSet = true
    try {
      val stream = getResources openRawResource IMAGE_IDS(position)
      setWallpaper(stream)
      setResult(Activity.RESULT_OK)
      finish()
    } catch {
      case e: IOException =>
        Log.e(LOG_TAG, "Failed to set wallpaper " + e)
    }
  }

  def onNothingSelected(parent: AdapterView[_]) {
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    selectWallpaper(mGallery.getSelectedItemPosition)
    true
  }

  class ImageAdapter(context: Context) extends BaseAdapter {

    def getCount: Int = {
      THUMB_IDS.length
    }

    def getItem(position: Int): AnyRef = {
      position.asInstanceOf[AnyRef]
    }

    def getItemId(position: Int): Long = {
      position
    }

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val i = new ImageView(context)

      i setImageResource THUMB_IDS(position)
      i setAdjustViewBounds true 
      i setLayoutParams new Gallery.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
      i setBackgroundResource android.R.drawable.picture_frame
      i
    }

  }

}

    
