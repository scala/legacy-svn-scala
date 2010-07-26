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

import scala.android.maps.Overlay
import scala.collection.mutable.ListBuffer

import com.google.android.maps.{GeoPoint, MapActivity, MapController, MapView,
         MyLocationOverlay, Projection}

import android.content.Intent
import android.graphics.{Canvas, Point}
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.{Gravity, View}
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout


/**
 * Displays a custom map which shows our current location and the location
 * where the photo was taken.
 */
class ViewMap extends MapActivity {
  private var mMapView: MapView = _

  private var mMyLocationOverlay: MyLocationOverlay = _

  var mItems: ListBuffer[PanoramioItem] = null

  private var mItem: PanoramioItem = _

  private var mMarker: Drawable = _

  private var mMarkerXOffset: Int = _

  private var mMarkerYOffset: Int = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val frame = new FrameLayout(this)
    mMapView = new MapView(this, "MapViewCompassDemo_DummyAPIKey")
    frame.addView(mMapView, 
      new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT))
    setContentView(frame)

    mMyLocationOverlay = new MyLocationOverlay(this, mMapView)

    mMarker = getResources getDrawable R.drawable.map_pin

    // Make sure to give mMarker bounds so it will draw in the overlay
    val intrinsicWidth = mMarker.getIntrinsicWidth
    val intrinsicHeight = mMarker.getIntrinsicHeight
    mMarker.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        
    mMarkerXOffset = -(intrinsicWidth / 2)
    mMarkerYOffset = -intrinsicHeight

    // Read the item we are displaying from the intent, along with the 
    // parameters used to set up the map
    val i = getIntent
    mItem = i.getParcelableExtra(ImageManager.PANORAMIO_ITEM_EXTRA)
    val mapZoom = i.getIntExtra(ImageManager.ZOOM_EXTRA, Int.MinValue)
    val mapLatitudeE6 = i.getIntExtra(ImageManager.LATITUDE_E6_EXTRA, Int.MinValue)
    val mapLongitudeE6 = i.getIntExtra(ImageManager.LONGITUDE_E6_EXTRA, Int.MinValue)

    val overlays = mMapView.getOverlays
    overlays add mMyLocationOverlay
    overlays add new PanoramioOverlay()

    val controller = mMapView.getController
    if (mapZoom != Int.MinValue && mapLatitudeE6 != Int.MinValue &&
        mapLongitudeE6 != Int.MinValue) {
      controller setZoom mapZoom
      controller setCenter new GeoPoint(mapLatitudeE6, mapLongitudeE6)
    } else {
      controller setZoom 15
      mMyLocationOverlay runOnFirstFix new Runnable() {
        def run() {
          controller animateTo mMyLocationOverlay.getMyLocation
        }
      }
    }

    mMapView setClickable true
    mMapView setEnabled true
    mMapView setSatellite true
    //addZoomControls(frame)
    mMapView setBuiltInZoomControls true
  }

  override def onResume() {
    super.onResume()
    mMyLocationOverlay.enableMyLocation()
  }

  override protected def onStop() {
    mMyLocationOverlay.disableMyLocation()
    super.onStop()
  }

  /**
   * Get the zoom controls and add them to the bottom of the map
   */ /*
  private def addZoomControls(frame: FrameLayout) {
    val zoomControls = mMapView.getZoomControls()

    val p = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM + Gravity.CENTER_HORIZONTAL)
    frame.addView(zoomControls, p)
  }*/
    
  override protected def isRouteDisplayed: Boolean =
    false
    
  /**
   * Custom overlay to display the Panoramio pushpin
   */
  class PanoramioOverlay extends Overlay {
    override def draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
      if (!shadow) {
        val point = new Point()
        val p = mapView.getProjection
        p.toPixels(mItem.getLocation, point)
        super.draw(canvas, mapView, shadow)
        Overlay.drawAt(canvas, mMarker,
                       point.x + mMarkerXOffset,
                       point.y + mMarkerYOffset, shadow)
      }
    }
  }

}
