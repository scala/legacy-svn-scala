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

import android.app.{Activity, AlertDialog}
import android.content.{DialogInterface, Intent}
import android.content.DialogInterface.OnClickListener
import android.graphics.{Bitmap, BitmapFactory}
import android.net.Uri
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.{Menu, MenuItem, View}
import android.view.animation.AnimationUtils
import android.widget.{ImageView, TextView}

import com.msi.manning.restaurant.data.Review

import java.io.{BufferedInputStream, IOException}
import java.net.{URL, URLConnection}

/**
 * Show Review detail for review item user selected.
 * 
 * @author charliecollins
 */
class ReviewDetail extends Activity {
  import ReviewDetail._  // companion object
    
  private var imageLink: String = _
  private var link: String = _
  private var location: TextView = _
  private var name: TextView = _
  private var phone: TextView = _
  private var rating: TextView = _
  private var review: TextView = _
  private var reviewImage: ImageView = _
    
  private val handler = new Handler() {
    override def handleMessage(msg: Message) {
      if ((imageLink != null) && !imageLink.equals("")) {
        try {
          val url = new URL(imageLink)
          val conn = url.openConnection()
          conn.connect()
          val bis = new BufferedInputStream(conn.getInputStream)
          val bm = BitmapFactory.decodeStream(bis)
          bis.close()
          reviewImage setImageBitmap bm
        } catch {
          case e: IOException =>
            Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
        }
      } else {
        reviewImage setImageResource R.drawable.no_review_image
      }
    }
  }

  private def find[V <: View](id: Int) = findViewById(id).asInstanceOf[V]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onCreate")
    // inflate layout
    setContentView(R.layout.review_detail)
    // reference XML defined views that we will touch in code
    name = find(R.id.name_detail)
    name startAnimation AnimationUtils.loadAnimation(this, R.anim.scaler)
    rating = find(R.id.rating_detail)
    location = find(R.id.location_detail)
    phone = find(R.id.phone_detail)
    review = find(R.id.review_detail)
    reviewImage = find(R.id.review_image)
    // get the current review from the Application (global state placed there)
    val application = getApplication.asInstanceOf[RestaurantFinderApplication]
    val currentReview = application.getCurrentReview
    link = currentReview.link
    imageLink = currentReview.imageLink
    name setText currentReview.name
    rating setText currentReview.rating
    location setText currentReview.location
    review setText currentReview.content
    if ((currentReview.phone != null) && !currentReview.phone.equals("")) {
      phone setText currentReview.phone
    } else {
      phone setText "NA"
    }
  }
    
  override protected def onResume() {
    super.onResume()
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onResume")
    // tell handler to load image
    handler sendEmptyMessage 1
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu.add(0, MENU_WEB_REVIEW, 0, R.string.menu_web_review)
        .setIcon(android.R.drawable.ic_menu_info_details)
    menu.add(0, MENU_MAP_REVIEW, 1, R.string.menu_map_review)
        .setIcon(android.R.drawable.ic_menu_mapmode)
    menu.add(0, MENU_CALL_REVIEW, 2, R.string.menu_call_review)
        .setIcon(android.R.drawable.ic_menu_call)
    true
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean = {
    var intent: Intent = null
    item.getItemId match {
      case MENU_WEB_REVIEW =>
        Log.v(Constants.LOGTAG, " " + CLASSTAG + " WEB - " + link)
        if ((link != null) && !link.equals("")) {
          intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link))
          startActivity(intent)
        } else {
          new AlertDialog.Builder(this)
            .setTitle(getResources getString R.string.alert_label)
            .setMessage(R.string.no_link_message)
            .setPositiveButton("Continue", new OnClickListener() {
              def onClick(dialog: DialogInterface, arg1: Int) {
              }
            }).show()
        }
        true
      case MENU_MAP_REVIEW =>
        Log.v(Constants.LOGTAG, " " + CLASSTAG + " MAP ");
        if ((location.getText != null) && !location.getText.equals("")) {
          intent = new Intent(Intent.ACTION_VIEW,
            Uri.parse("geo:0,0?q=" + location.getText.toString))
          startActivity(intent)
        } else {
          new AlertDialog.Builder(this)
            .setTitle(getResources getString R.string.alert_label)
            .setMessage(R.string.no_location_message)
            .setPositiveButton("Continue", new OnClickListener() {
              def onClick(dialog: DialogInterface, arg1: Int) {
              }
            }).show()
        }
        true
      case MENU_CALL_REVIEW =>
        Log.v(Constants.LOGTAG, " " + CLASSTAG + " PHONE ")
        if ((phone.getText != null) && !phone.getText.equals("")
            && !phone.getText.equals("NA")) {
          Log.v(Constants.LOGTAG, " " + CLASSTAG +
                " phone - " + this.phone.getText.toString)
          val phoneString = parsePhone(this.phone.getText.toString)
          intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneString))
          startActivity(intent)
        } else {
          new AlertDialog.Builder(this)
            .setTitle(getResources getString R.string.alert_label)
            .setMessage(R.string.no_phone_message)
            .setPositiveButton("Continue", new OnClickListener() {
              def onClick(dialog: DialogInterface, arg1: Int) {
              }
            }).show()
        }
        true
      case _ =>
        super.onMenuItemSelected(featureId, item)
    }    
  }
}

object ReviewDetail {
  private final val CLASSTAG = classOf[ReviewDetail].getSimpleName
  private final val MENU_CALL_REVIEW = Menu.FIRST + 2
  private final val MENU_MAP_REVIEW = Menu.FIRST + 1
  private final val MENU_WEB_REVIEW = Menu.FIRST

  private def parsePhone(p: String): String = {
    var tempP = p
    tempP = tempP.replaceAll("\\D", "")
    tempP = tempP.replaceAll("\\s", "")
    tempP.trim()
  }
}
