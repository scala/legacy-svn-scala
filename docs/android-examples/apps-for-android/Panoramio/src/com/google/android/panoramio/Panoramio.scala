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

import com.google.android.maps.{GeoPoint, MapActivity, MapView, MyLocationOverlay}

import android.content.Intent
import android.os.Bundle
import android.view.{Gravity, View}
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams
import android.widget.{Button, FrameLayout}

/**
 * Activity which lets the user select a search area
 *
 */
class Panoramio extends MapActivity with OnClickListener {
  import Panoramio._  // companion object

  private var mMapView: MapView = _
  private var mMyLocationOverlay: MyLocationOverlay = _
  private var mImageManager: ImageManager = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
        
    setContentView(R.layout.main)
        
    mImageManager = ImageManager.getInstance(this)
        
    val frame = findViewById(R.id.frame).asInstanceOf[FrameLayout]
    val goButton = findViewById(R.id.go).asInstanceOf[Button]
    goButton setOnClickListener this
       
    // Add the map view to the frame
    mMapView = new MapView(this, "Panoramio_DummyAPIKey")
    frame.addView(mMapView, 
                new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT))

    // Create an overlay to show current location
    mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
    mMyLocationOverlay runOnFirstFix new Runnable() {
      def run() {
        mMapView.getController animateTo mMyLocationOverlay.getMyLocation
      }
    }

    mMapView.getOverlays add mMyLocationOverlay
    mMapView.getController setZoom 15
    mMapView setClickable true
    mMapView setEnabled true
    mMapView setSatellite true

    //addZoomControls(frame) // deprecated since API level 4
    mMapView setBuiltInZoomControls true
  }

  override protected def onResume() {
    super.onResume()
    mMyLocationOverlay.enableMyLocation()
  }

  override protected def onStop() {
    mMyLocationOverlay.disableMyLocation()
    super.onStop()
  }

  /**
   * Add zoom controls to our frame layout
   */ /*
  private def addZoomControls(frame: FrameLayout) {
    // Get the zoom controls and add them to the bottom of the map
    val zoomControls = mMapView.getZoomControls

    val p = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.BOTTOM + Gravity.CENTER_HORIZONTAL)
    frame.addView(zoomControls, p)
  }*/

  override protected def isRouteDisplayed: Boolean = false

  /**
   * Starts a new search when the user clicks the search button.
   */
  def onClick(view: View) {
    // Get the search area
    val latHalfSpan = mMapView.getLatitudeSpan >> 1
    val longHalfSpan = mMapView.getLongitudeSpan >> 1

    // Remember how the map was displayed so we can show it the same way later
    val center: GeoPoint = mMapView.getMapCenter
    val zoom = mMapView.getZoomLevel
    val latitudeE6 = center.getLatitudeE6
    val longitudeE6 = center.getLongitudeE6

    val i = new Intent(this, classOf[ImageList])
    i.putExtra(ImageManager.ZOOM_EXTRA, zoom)
    i.putExtra(ImageManager.LATITUDE_E6_EXTRA, latitudeE6)
    i.putExtra(ImageManager.LONGITUDE_E6_EXTRA, longitudeE6)

    val minLong = (longitudeE6 - longHalfSpan).toFloat / MILLION
    val maxLong = (longitudeE6 + longHalfSpan).toFloat / MILLION

    val minLat = (latitudeE6 - latHalfSpan).toFloat / MILLION
    val maxLat = (latitudeE6 + latHalfSpan).toFloat / MILLION

    mImageManager.clear()

    // Start downloading
    mImageManager.load(minLong, maxLong, minLat, maxLat)

    // Show results
    startActivity(i)
  }
}

object Panoramio {
  final val MILLION = 1000000
}
