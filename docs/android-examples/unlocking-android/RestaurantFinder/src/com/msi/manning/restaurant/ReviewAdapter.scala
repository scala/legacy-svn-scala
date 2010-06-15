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

package com.msi.manning.restaurant

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.{View, ViewGroup}
import android.view.ViewGroup.LayoutParams
import android.widget.{BaseAdapter, LinearLayout, TextView}

import com.msi.manning.restaurant.data.Review

/**
 * Custom adapter for "Review" model objects.
 * 
 * @author charliecollins
 */
class ReviewAdapter(context: Context, reviews: List[Review]) extends BaseAdapter {
  import ReviewAdapter._  // companion object

  Log.v(Constants.LOGTAG, " " +CLASSTAG + " reviews size - " + reviews.size)

  def getCount: Int = reviews.size

  def getItem(position: Int): AnyRef = reviews(position).asInstanceOf[AnyRef]

  def getItemId(position: Int): Long = position

  def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val review = reviews(position)
    new ReviewListView(context, review.name, review.rating)
  }

  /**
   * ReviewListView that adapter returns as it's view item per row.
   * 
   * @author charliecollins
   */
  private final class ReviewListView(context: Context,
                                     name: String, rating: String)
              extends LinearLayout(context) {

    setOrientation(LinearLayout.VERTICAL)

    val params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                               LayoutParams.WRAP_CONTENT)
    params.setMargins(5, 3, 5, 0)

    val nameView = new TextView(context)
    nameView setText name
    nameView setTextSize 16f
    nameView setTextColor Color.WHITE
    addView(nameView, params)

    val ratingView = new TextView(context)
    ratingView setText rating
    ratingView setTextSize 16f
    ratingView setTextColor Color.GRAY
    addView(ratingView, params)
  }
}

object ReviewAdapter {
  private final val CLASSTAG = classOf[ReviewAdapter].getSimpleName
}
