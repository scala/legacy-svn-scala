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

import android.content.Context
import android.database.DataSetObserver
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, ImageView, TextView}

/**
 * Adapter used to bind data for the main list of photos
 */
class ImageAdapter(mContext: Context) extends BaseAdapter {

  /**
   * Maintains the state of our data
   */
  private val mImageManager = ImageManager.getInstance(mContext)

  private val mObserver = new MyDataSetObserver()
  mImageManager addObserver mObserver

  /**
   * Used by the {@link ImageManager} to report changes in the list back to
   * this adapter.
   */
  private class MyDataSetObserver extends DataSetObserver {
    override def onChanged() {
      notifyDataSetChanged()
    }

    override def onInvalidated() {
      notifyDataSetInvalidated()
    }
  }

  /**
   * Returns the number of images to display
   * 
   * @see android.widget.Adapter#getCount()
   */
  def getCount: Int = mImageManager.size

  /**
   * Returns the image at a specified position
   * 
   * @see android.widget.Adapter#getItem(int)
   */
  def getItem(position: Int): AnyRef = mImageManager get position

  /**
   * Returns the id of an image at a specified position
   * 
   * @see android.widget.Adapter#getItemId(int)
   */
  def getItemId(position: Int): Long = {
    val s: PanoramioItem = mImageManager get position
    s.getId
  }

  /**
   * Returns a view to display the image at a specified position
   * 
   * @param position The position to display
   * @param convertView An existing view that we can reuse. May be null.
   * @param parent The parent view that will eventually hold the view we return.
   * @return A view to display the image at a specified position
   */
  def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = if (convertView == null) {
      // Make up a new view
      val inflater = mContext
                  .getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
      inflater.inflate(R.layout.image_item, null)
    } else {
      // Use convertView if it is available
      convertView
    }
    val s: PanoramioItem = mImageManager get position

    val i = view.findViewById(R.id.image).asInstanceOf[ImageView]
    i setImageBitmap s.getBitmap
    i setBackgroundResource R.drawable.picture_frame

    var t = view.findViewById(R.id.title).asInstanceOf[TextView]
    t setText s.getTitle

    t = view.findViewById(R.id.owner).asInstanceOf[TextView]
    t setText s.getOwner
    view
  }

}
