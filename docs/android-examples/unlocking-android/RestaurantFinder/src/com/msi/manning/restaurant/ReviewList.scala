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

import android.app.{ListActivity, ProgressDialog}
import android.content.Intent
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.{Menu, MenuItem, View}
import android.widget.{ListView, TextView}

import com.msi.manning.restaurant.Constants._
import com.msi.manning.restaurant.data.{Review, ReviewFetcher}

import scala.collection.JavaConversions._

/**
 * "List" of reviews screen - show reviews that match Criteria user selected.
 * Users ReviewFetcher which makes a Google Base call via Rome.
 * 
 * @author charliecollins
 */
class ReviewList extends ListActivity {
  import ReviewList._  // companion object

  private var empty: TextView = _
  private var progressDialog: ProgressDialog = _
  private var reviewAdapter: ReviewAdapter = _
  private var reviews: List[Review] = _
    
  private final val handler = new Handler() {
    override def handleMessage(msg: Message) {
      Log.v(LOGTAG, " " + CLASSTAG + " worker thread done, setup ReviewAdapter")
      progressDialog.dismiss()
      if ((reviews == null) || (reviews.size == 0)) {
        empty setText "No Data"
      } else {
        reviewAdapter = new ReviewAdapter(ReviewList.this, reviews)
        setListAdapter(reviewAdapter)
      }
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    Log.v(LOGTAG, " " + CLASSTAG + " onCreate")

    // NOTE* This Activity MUST contain a ListView named "@android:id/list"
    // (or "list" in code) in order to be customized
    // http://code.google.com/android/reference/android/app/ListActivity.html
    setContentView(R.layout.review_list)

    empty = findViewById(R.id.empty).asInstanceOf[TextView]

    // set list properties
    val listView = getListView
    listView setItemsCanFocus false
    listView setChoiceMode ListView.CHOICE_MODE_SINGLE
    listView setEmptyView empty
  }   

  override protected def onResume() {
    super.onResume()
    Log.v(LOGTAG, " " + CLASSTAG + " onResume")
    // get the current review criteria from the Application
    // (global state placed there)
    val application = getApplication.asInstanceOf[RestaurantFinderApplication]
    val criteriaCuisine = application.getReviewCriteriaCuisine
    val criteriaLocation = application.getReviewCriteriaLocation

    // get start from, an int, from extras
    val startFrom = getIntent.getIntExtra(STARTFROM_EXTRA, 1)

    loadReviews(criteriaLocation, criteriaCuisine, startFrom)
  }    
   
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu.add(0, MENU_GET_NEXT_PAGE, 0, R.string.menu_get_next_page)
        .setIcon(android.R.drawable.ic_menu_more)
    menu.add(0, MENU_CHANGE_CRITERIA, 0, R.string.menu_change_criteria)
        .setIcon(android.R.drawable.ic_menu_edit)
    true
  }    

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean = {
    var intent: Intent = null
    item.getItemId match {
      case MENU_GET_NEXT_PAGE =>
        // increment the startFrom value and call this Activity again
        intent = new Intent(INTENT_ACTION_VIEW_LIST)
        intent.putExtra(STARTFROM_EXTRA,
                        getIntent.getIntExtra(STARTFROM_EXTRA, 1)
                        + NUM_RESULTS_PER_PAGE)
        startActivity(intent)
        true
      case MENU_CHANGE_CRITERIA =>
        intent = new Intent(this, classOf[ReviewCriteria])
        startActivity(intent)
        true
      case _ =>
        super.onMenuItemSelected(featureId, item)
    }
  }
    
  override protected def onListItemClick(l: ListView, v: View,
                                         position: Int, id: Long) {
    // set the current review to the Application (global state placed there)
    val application = getApplication.asInstanceOf[RestaurantFinderApplication]
    application setCurrentReview reviews(position)

    // startFrom page is not stored in application, for example purposes it's
    // a simple "extra"
    val intent = new Intent(INTENT_ACTION_VIEW_DETAIL)
    intent.putExtra(STARTFROM_EXTRA, getIntent.getIntExtra(STARTFROM_EXTRA, 1))
    startActivity(intent)
  }    
    
  private def loadReviews(location: String, cuisine: String, startFrom: Int) {
    Log.v(LOGTAG, " " + CLASSTAG + " loadReviews")

    val rf = new ReviewFetcher(location, cuisine, "ALL", startFrom,
                               NUM_RESULTS_PER_PAGE)

    progressDialog = ProgressDialog.show(this, " Working...",
                                         " Retrieving reviews", true, false)

    // get reviews in a separate thread for ProgressDialog/Handler
    // when complete send "empty" message to handler
    val th = new Thread() {
      override def run() {
        reviews = new JListWrapper(rf.getReviews).toList
        handler sendEmptyMessage 0
      }
    }
    th.start()
  }
}

object ReviewList {
  private final val CLASSTAG = classOf[ReviewList].getSimpleName
  private final val MENU_CHANGE_CRITERIA = Menu.FIRST + 1
  private final val MENU_GET_NEXT_PAGE = Menu.FIRST
  private final val NUM_RESULTS_PER_PAGE = 8
}
