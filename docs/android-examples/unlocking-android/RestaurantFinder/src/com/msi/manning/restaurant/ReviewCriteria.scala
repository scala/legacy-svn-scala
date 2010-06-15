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

import com.msi.manning.restaurant.Constants._

import android.app.{Activity, AlertDialog}
import android.content.{DialogInterface, Intent}
import android.os.Bundle
import android.util.Log
import android.view.{Menu, MenuItem}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, Button, EditText, Spinner}

/**
 * "Criteria" to select reviews screen - choose Location, Cuisine, and Rating,
 * and then forward to next activity.
 * 
 * @author charliecollins
 */
class ReviewCriteria extends Activity {
  import ReviewCriteria._  // companion object

  private var cuisine: Spinner = _
  private var grabReviews: Button = _
  private var location: EditText = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    Log.v(LOGTAG, " " + CLASSTAG + " onCreate")

    setContentView(R.layout.review_criteria)

    location = findViewById(R.id.location).asInstanceOf[EditText]
    cuisine = findViewById(R.id.cuisine).asInstanceOf[Spinner]
    grabReviews = findViewById(R.id.get_reviews_button).asInstanceOf[Button]

    val cuisines = new ArrayAdapter[String](this, R.layout.spinner_view,
         getResources.getStringArray(R.array.cuisines))
    cuisines setDropDownViewResource R.layout.spinner_view_dropdown
    cuisine setAdapter cuisines

    grabReviews setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        handleGetReviews()
      }
    }
  }   

  override protected def onResume() {
    super.onResume()
    Log.v(LOGTAG, " " + CLASSTAG + " onResume")
  }
    
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu.add(0, MENU_GET_REVIEWS, 0, R.string.menu_get_reviews)
        .setIcon(android.R.drawable.ic_menu_more)
    true
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean =
    item.getItemId match {
      case MENU_GET_REVIEWS =>
        handleGetReviews()
        true
      case _ =>
        super.onMenuItemSelected(featureId, item)
    }
    
  private def handleGetReviews() {
    if (!validate()) {
      return;
    }

    // use the "Application" to store global state (can go beyond primitives
    // and Strings - beyond extras - if needed)
    val application = getApplication.asInstanceOf[RestaurantFinderApplication]
    application setReviewCriteriaCuisine cuisine.getSelectedItem.toString
    application setReviewCriteriaLocation location.getText.toString

    // call next Activity, VIEW_LIST
    val intent = new Intent(Constants.INTENT_ACTION_VIEW_LIST)
    startActivity(intent)
  }

  // validate form fields
  private def validate(): Boolean = {
    var valid = true
    var validationText = new StringBuilder()
    if ((location.getText == null) || location.getText.toString.equals("")) {
      validationText.append(
        getResources getString R.string.location_not_supplied_message)
      valid = false
    }
    if (!valid) {
      new AlertDialog.Builder(this)
        .setTitle(getResources getString R.string.alert_label)
        .setMessage(validationText.toString)
        .setPositiveButton("Continue",
          new android.content.DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, arg1: Int) {
              // in this case, don't need to do anything other than close alert
            }
          }).show()
      validationText = null
    }
    valid
  }
}

object ReviewCriteria {
  private final val CLASSTAG = classOf[ReviewCriteria].getSimpleName
  private final val MENU_GET_REVIEWS = Menu.FIRST
}
