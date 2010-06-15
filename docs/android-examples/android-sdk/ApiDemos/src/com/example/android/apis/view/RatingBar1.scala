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

import android.app.Activity
import android.os.Bundle
import android.widget.{RatingBar, SeekBar, TextView}

import com.example.android.apis.R

/**
 * Demonstrates how to use a rating bar
 */
class RatingBar1 extends Activity with RatingBar.OnRatingBarChangeListener {
  private var mSmallRatingBar: RatingBar = _
  private var mIndicatorRatingBar: RatingBar = _
  private var mRatingText: TextView = _
    
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.ratingbar_1)
        
    mRatingText = findViewById(R.id.rating).asInstanceOf[TextView]

    // We copy the most recently changed rating on to these indicator-only
    // rating bars
    mIndicatorRatingBar = findViewById(R.id.indicator_ratingbar).asInstanceOf[RatingBar]
    mSmallRatingBar = findViewById(R.id.small_ratingbar).asInstanceOf[RatingBar]
        
    // The different rating bars in the layout. Assign the listener to us.
    val rb1 = findViewById(R.id.ratingbar1).asInstanceOf[RatingBar]
    val rb2 = findViewById(R.id.ratingbar2).asInstanceOf[RatingBar]
    rb1 setOnRatingBarChangeListener this
    rb2 setOnRatingBarChangeListener this
  }

  def onRatingChanged(ratingBar: RatingBar, rating: Float, fromTouch: Boolean) {
    val numStars = ratingBar.getNumStars
    val text = getString(R.string.ratingbar_rating)
    mRatingText.setText(text + " " + rating + "/" + numStars)

    // Since this rating bar is updated to reflect any of the other rating
    // bars, we should update it to the current values.
    if (mIndicatorRatingBar.getNumStars != numStars) {
      mIndicatorRatingBar setNumStars numStars
      mSmallRatingBar setNumStars numStars
    }
    if (mIndicatorRatingBar.getRating != rating) {
      mIndicatorRatingBar setRating rating
      mSmallRatingBar setRating rating
    }
    val ratingBarStepSize = ratingBar.getStepSize
    if (mIndicatorRatingBar.getStepSize != ratingBarStepSize) {
      mIndicatorRatingBar setStepSize ratingBarStepSize
      mSmallRatingBar setStepSize ratingBarStepSize
    }
  }

}
