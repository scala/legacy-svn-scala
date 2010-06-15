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

package com.msi.manning.windwaves

import android.app.AlertDialog
import android.content.{Context, DialogInterface, Intent}
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.{LayoutInflater, MotionEvent, View}
import android.widget.TextView

import com.google.android.maps.{ItemizedOverlay, MapView}
import com.msi.manning.windwaves.data.BuoyData

/**
 * Map Overlay class that extends ItemizedOverlay - puts the same marker on
 * the map for each OverlayItem, and handles tap events, etc.
 * 
 * @author charliecollins
 * 
 */
class BuoyItemizedOverlay(items: List[BuoyOverlayItem],
                          defaultMarker: Drawable, context: Context)
extends ItemizedOverlay[BuoyOverlayItem](defaultMarker) {
  import BuoyItemizedOverlay._  // companion object

  // after the list is ready, you have to call populate (before draw is
  // automatically invoked)
  populate()

  override def createItem(i: Int): BuoyOverlayItem = items(i)

  override def onTrackballEvent(e: MotionEvent, v: MapView): Boolean = {
    // Toast.makeText(this.context, "trackballEvent", Toast.LENGTH_SHORT).show()
    true
  }

  override protected def onTap(i: Int): Boolean = {
    Log.d(Constants.LOGTAG, CLASSTAG + " item with index " + i + " tapped")
    val bd = items(i).buoyData
    Log.d(Constants.LOGTAG, CLASSTAG + " selected buoyData - " + bd)

    val inflater = LayoutInflater.from(context)
    val bView = inflater.inflate(R.layout.buoy_selected, null)
    val title = bView.findViewById(R.id.buoy_title).asInstanceOf[TextView]
    title setText bd.title
    val date = bView.findViewById(R.id.buoy_date).asInstanceOf[TextView]
    date setText bd.dateString
    val location = bView.findViewById(R.id.buoy_location).asInstanceOf[TextView]
    location setText bd.location

    val airTemp = if (bd.airTemp != null) bd.airTemp else "NA"
    val waterTemp = if (bd.waterTemp != null) bd.waterTemp else "NA"
    val windSpeed = if (bd.windSpeed != null) bd.windSpeed else "NA"
    val waveHeight = if (bd.waveHeight != null) bd.waveHeight else "NA"

    val atView = bView.findViewById(R.id.cond_airtemp).asInstanceOf[TextView]
    atView.setText("Air temp: " + airTemp)
    val wtView = bView.findViewById(R.id.cond_watertemp).asInstanceOf[TextView]
    wtView.setText("Water temp: " + waterTemp)
    val wsView = bView.findViewById(R.id.cond_windspeed).asInstanceOf[TextView]
    wsView.setText("Wind speed: " + windSpeed)
    val whView = bView.findViewById(R.id.cond_waveheight).asInstanceOf[TextView]
    whView.setText("Wave height: " + waveHeight)

    new AlertDialog.Builder(context)
      .setView(bView)
      .setPositiveButton("More Detail", new DialogInterface.OnClickListener() {
        def onClick(di: DialogInterface, what: Int) {
          val intent = new Intent(context, classOf[BuoyDetailActivity])
          // quick and dirty hack to set data on another activity
          // (not really ideal, but don't need a Parcelable here either, and
          // don't want to pass in Bundle separately, etc)
          BuoyDetailActivity.buoyData = bd
          context startActivity intent
        }
      })
      .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        def onClick(di: DialogInterface, what: Int) {
          di.dismiss()
        }
      }).show()

    true
  }

  override def size: Int = items.size

  override def draw(canvas: Canvas, mapView: MapView, b: Boolean) {
    super.draw(canvas, mapView, false)
    // example of manual drawing it, here we are letting ItemizedOverlay handle it
    // val buoy = BitmapFactory.decodeResource(mapView.getResources, R.drawable.buoy_30)
    // bitmap, x, y, Paint
    // canvas.drawBitmap(buoy, 50, 50, null);       
  }
}

object BuoyItemizedOverlay {
  private final val CLASSTAG = classOf[BuoyItemizedOverlay].getSimpleName
}
