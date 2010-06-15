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

import android.app.Application

import com.msi.manning.restaurant.data.Review

/**
 * Extend Application for global state information for an application. Access
 *  the application via Activity.getApplication().
 * 
 * There are several ways to store global state information, this is one of
 * them. Another is to create a class with static members and just access it
  *from Activities.
 * 
 * Either approach works, and there is debate about which is better. Either
 * way, make sure to clean up in life-cycle pause or destroy methods if you use
 * resources that need cleaning up (static maps, etc).
 * 
 * @author charliecollins
 */
class RestaurantFinderApplication extends Application {

  private var currentReview: Review = _
  private var reviewCriteriaCuisine: String = _
  private var reviewCriteriaLocation: String = _

  override def onCreate() {
    super.onCreate()
  }

  override def onTerminate() {
    super.onTerminate()
  }

  def getCurrentReview: Review = currentReview

  def getReviewCriteriaCuisine: String = reviewCriteriaCuisine

  def getReviewCriteriaLocation: String = reviewCriteriaLocation

  def setCurrentReview(currentReview: Review) {
    this.currentReview = currentReview
  }

  def setReviewCriteriaCuisine(reviewCriteriaCuisine: String) {
    this.reviewCriteriaCuisine = reviewCriteriaCuisine
  }

  def setReviewCriteriaLocation(reviewCriteriaLocation: String) {
    this.reviewCriteriaLocation = reviewCriteriaLocation
  }
}
