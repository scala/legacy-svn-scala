/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.android.panoramio

import android.app.ListActivity
import android.content.{Context, Intent}
import android.database.DataSetObserver
import android.os.Bundle
import android.view.{LayoutInflater, View, Window}
import android.widget.ListView

/**
 * Activity which displays the list of images.
 */
class ImageList extends ListActivity {
    
  var mImageManager: ImageManager = _

  private var mObserver = new MyDataSetObserver()

  /**
   * The zoom level the user chose when picking the search area
   */
  private var mZoom: Int = _

  /**
   * The latitude of the center of the search area chosen by the user
   */
  private var mLatitudeE6: Int = _

  /**
   * The longitude of the center of the search area chosen by the user
   */
  private var mLongitudeE6: Int = _

  /**
   * Observer used to turn the progress indicator off when the
   * {@link ImageManager} is done downloading.
   */
  private class MyDataSetObserver extends DataSetObserver {
    override def onChanged() {
      if (!mImageManager.isLoading) {
        getWindow.setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                                Window.PROGRESS_VISIBILITY_OFF)
      }
    }

    override def onInvalidated() {
    }
  }
    
  override def onCreate(savedInstanceState: Bundle) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    super.onCreate(savedInstanceState)
    mImageManager = ImageManager.getInstance(this)
    val listView = getListView
    val inflater =
      getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    val footer = inflater.inflate(R.layout.list_footer, listView, false)
    listView.addFooterView(footer, null, false)
    setListAdapter(new ImageAdapter(this))

    // Theme.Light sets a background on our list.
    listView setBackgroundDrawable null
    if (mImageManager.isLoading) {
      getWindow.setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                              Window.PROGRESS_VISIBILITY_ON)
      mImageManager addObserver mObserver
    }
        
    // Read the user's search area from the intent
    val i = getIntent
    mZoom = i.getIntExtra(ImageManager.ZOOM_EXTRA, Int.MinValue)
    mLatitudeE6 = i.getIntExtra(ImageManager.LATITUDE_E6_EXTRA, Int.MinValue)
    mLongitudeE6 = i.getIntExtra(ImageManager.LONGITUDE_E6_EXTRA, Int.MinValue)
  }

  override protected def onListItemClick(l: ListView, v: View,
                                         position: Int, id: Long) {
    val item = mImageManager get position

    // Create an intent to show a particular item.
    // Pass the user's search area along so the next activity can use it
    val i = new Intent(this, classOf[ViewImage])
    i.putExtra(ImageManager.PANORAMIO_ITEM_EXTRA, item)
    i.putExtra(ImageManager.ZOOM_EXTRA, mZoom)
    i.putExtra(ImageManager.LATITUDE_E6_EXTRA, mLatitudeE6)
    i.putExtra(ImageManager.LONGITUDE_E6_EXTRA, mLongitudeE6)
    startActivity(i)
  }   

}
