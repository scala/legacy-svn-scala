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

import android.app.ProgressDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.location.{Location, LocationListener, LocationManager, LocationProvider}
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.{Menu, MenuItem, ViewGroup}
import android.widget.Toast

import com.google.android.maps.{GeoPoint, MapActivity, MapController, MapView, Overlay}

import com.msi.manning.windwaves.data.{BuoyData, NDBCFetcher}

import scala.collection.mutable.ListBuffer

// NOTE apiKey stuff in layout file - note that that seems dumb (in the layout,
// not the manifest?)
// NOTE lastKnownLocation via emulator may be NULL though, need to set at least
// one location (w/tools) for it to not be
// NOTE have to setBounds on defaultMarker
// NOTE can also create a receiver for LOCATION_CHANGED (alternative way to get updated)
// NOTE can also get lastLocation and locationChanged callback from MyLocationOverlay (more coarse
// grained though)
// NOTE can also get location using CellLocation via TelephonyManager
// NOTE can use Geocode to go back and forth with lat/long addresses/places

/**
 * MapView Activity for WindWaves.
 * 
 * @author charliecollins
 * 
 */
class MapViewActivity extends MapActivity {
  import MapViewActivity._  // companion object

  private final val handler = new Handler() {

    override def handleMessage(msg: Message) {
      Log.d(Constants.LOGTAG, " " + CLASSTAG +
            " handleMessage invoked - update overlays with new data")
      Log.d(Constants.LOGTAG, " " + CLASSTAG +
            "   buoys (List<BuoyOverlayItem>) size - " + buoys.size)

      progressDialog.dismiss()

      // clear the buoys itemized overlay - if it's already there
      if (mapView.getOverlays contains buoyOverlay) {
        mapView.getOverlays remove buoyOverlay
      }

      // add buoys itemized overlay
      buoyOverlay = new BuoyItemizedOverlay(buoys, defaultMarker, MapViewActivity.this)
      mapView.getOverlays add buoyOverlay

      // invalidate so redrawn with icons (without this, not drawn until touch)
      mapView.invalidate()
    }
  }

  private final val locationListenerGetBuoyData = new LocationListener() {

    def  onLocationChanged(loc: Location) {
      Log.v(Constants.LOGTAG, " " + CLASSTAG +
            "   locationProviderGetBuoyData LOCATION CHANGED - " + loc);
      val lat = (loc.getLatitude * LocationHelper.MILLION).toInt
      val lon = (loc.getLongitude * LocationHelper.MILLION).toInt
      // update buoy data
      getBuoyData(new GeoPoint(lat, lon))
    }

    def onProviderDisabled(s: String) {
    }

    def onProviderEnabled(s: String) {
    }

    def onStatusChanged(s: String, i: Int, b: Bundle) {
    }
  }

  private final val locationListenerRecenterMap = new LocationListener() {

    override def onLocationChanged(loc: Location) {
      Log.v(Constants.LOGTAG, " " + CLASSTAG +
            "   locationProvider LOCATION CHANGED - " + loc)
      val lat = (loc.getLatitude * LocationHelper.MILLION).toInt
      val lon = (loc.getLongitude * LocationHelper.MILLION).toInt
      // animate to new location 
      mapController animateTo new GeoPoint(lat, lon)
    }

    def onProviderDisabled(s: String) {
    }

    def onProviderEnabled(s: String) {
    }

    def onStatusChanged(s: String, i: Int, b: Bundle) {
    }
  }

  private var mapController: MapController = _
  private var locationManager: LocationManager = _
  private var locationProvider: LocationProvider = _
  private var mapView: MapView = _
  private var zoom: ViewGroup = _
  private var buoyOverlay: Overlay = _
  private var progressDialog: ProgressDialog = _
  private var defaultMarker: Drawable = _
  private var buoys: List[BuoyOverlayItem] = Nil

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onCreate")
    setContentView(R.layout.mapview_activity)

    mapView = findViewById(R.id.map_view).asInstanceOf[MapView]
    mapView setBuiltInZoomControls true
    zoom = findViewById(R.id.zoom).asInstanceOf[ViewGroup]
    zoom addView mapView

    defaultMarker = getResources.getDrawable(R.drawable.buoy)
    defaultMarker.setBounds(0, 0, defaultMarker.getIntrinsicWidth,
                                  defaultMarker.getIntrinsicHeight)
  }

  override def onStart() {
    super.onStart()
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onStart")
    locationManager =
      getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

    // this.locationProvider = this.locationManager.getBestProvider(myCriteria, true);
    // this.locationProvider = this.locationManager.getProviders(true).get(0);
    locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER)

    Log.v(Constants.LOGTAG, " " + CLASSTAG +
          "   locationProvider from criteria - " + locationProvider)

    // get location updates from locationProvider
    // we set minTime(milliseconds) and minDistance(meters) to low values here
    // to get updated often (for emulator/debug)
    // in real life you *DO NOT* want to do this, it may consume too many resources
    // see LocationMangaer in JavaDoc for guidelines (time less than 60000ms
    // for minTime is NOT recommended)
    //
    // we use separate locationListeners for different purposes
    // one to update buoy data only if we move a long distance (185000 meters,
    // just under the 100 nautical miles we are parsing the data for)
    // another to recenter the map, even when we move a short distance (1000 meters)
    if (locationProvider != null) {
      locationManager.requestLocationUpdates(
        locationProvider.getName, 3000, 185000, locationListenerGetBuoyData)
      locationManager.requestLocationUpdates(
        locationProvider.getName, 3000, 1000, locationListenerRecenterMap)
    } else {
      Log.e(Constants.LOGTAG, " " + CLASSTAG + "  NO LOCATION PROVIDER AVAILABLE")
      Toast.makeText(this,
        "Wind and Waves cannot continue, the GPS location provider is not available at this time.",
      Toast.LENGTH_SHORT).show()
      finish()
    }

    // animate to, and get buoy data for lastKnownPoint on startup
    // (or fake/prime point if no last known)
    val lastKnownPoint = getLastKnownPoint
    mapController = mapView.getController
    mapController setZoom 10
    mapController animateTo lastKnownPoint
    getBuoyData(lastKnownPoint)
  }

  override def onResume() {
    super.onResume()
    if (progressDialog.isShowing) {
      progressDialog.dismiss()
    }
  }

  override def onPause() {
    super.onPause();
    if (progressDialog.isShowing) {
      progressDialog.dismiss()
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu.add(0, MENU_SET_MAP, 0, "Map")
        .setIcon(android.R.drawable.ic_menu_mapmode)
    menu.add(0, MENU_SET_SATELLITE, 0, "Satellite")
        .setIcon(android.R.drawable.ic_menu_mapmode)
    menu.add(1, MENU_BUOYS_FROM_MAP_CENTER, 0, "Get Buoy Data")
        .setIcon(android.R.drawable.ic_menu_directions)
    menu.add(2, MENU_BACK_TO_LAST_LOCATION, 0, "My Location")
        .setIcon(android.R.drawable.ic_menu_mylocation)
    true
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean = {
    item.getItemId match {
      case MENU_SET_MAP =>
        mapView setSatellite false
      case MENU_SET_SATELLITE =>
        mapView setSatellite true
      case MENU_BUOYS_FROM_MAP_CENTER =>
        getBuoyData(mapView.getMapCenter)
      case MENU_BACK_TO_LAST_LOCATION =>
        mapController animateTo getLastKnownPoint
        getBuoyData(getLastKnownPoint)
      case _ =>
    }
    super.onMenuItemSelected(featureId, item)
  }

  // required by MapActivity for maps server to be notified if you are
  // displaying directions
  override def isRouteDisplayed: Boolean = false

  private def getLastKnownPoint: GeoPoint = {
    var lastKnownPoint: GeoPoint = null

    // last KNOWN may be null, if none set after power up, or in emulator
    // and not setup
    val lastKnownLocation =
      locationManager getLastKnownLocation LocationManager.GPS_PROVIDER
    Log.i(Constants.LOGTAG, " " + CLASSTAG +
          "  lastKnownLocation - " + lastKnownLocation)

    // get lastKnown GeoPoint (either from lastKnownLocation, or prime the
    // pump manually)
    if (lastKnownLocation != null) {
      lastKnownPoint = LocationHelper getGeoPoint lastKnownLocation
    } else {
      Log.w(Constants.LOGTAG, " " + CLASSTAG +
            "  lastKnownLocation NULL - override to Golden Gate (gotta start somewhere)")
      lastKnownPoint = LocationHelper.GOLDEN_GATE

      Toast.makeText(this,
         "Wind and Waves cannot determine your location at startup, last known location is not present - defaulting to Golden Gate (enable GPS and then use menu->My Location).",
         Toast.LENGTH_LONG).show()
    }
    lastKnownPoint
  }

  private def getBuoyData(point: GeoPoint) {
    Log.d(Constants.LOGTAG, " " + CLASSTAG + "  getBuoyData invoked")
    progressDialog = ProgressDialog.show(this, "Processing . . .", "Getting buoy data", true, false)

    val th = new Thread() {
      override def run() {
        // parse lat/lon from GeoPoint back into Strings with direction (37N, 112W - etc)
        val lats = LocationHelper.parsePoint(point.getLatitudeE6() / LocationHelper.MILLION, true)
        val lons = LocationHelper.parsePoint(point.getLongitudeE6() / LocationHelper.MILLION, false)

        // for now we hard code the radius to 100 nautical miles from current
        // point (enhancement, dynamically set radius based on map zoom bounds,
        // or add user prefs)
        val fetcher = new NDBCFetcher(lats, lons, "100")
        val start = System.currentTimeMillis
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " fetcher start")
        val buoyDataList = fetcher.getData
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " fetcher finish - duration = " +
              (System.currentTimeMillis - start))
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " buoyDataList size = " +
              buoyDataList.size)

        // parse the List<BuoyData> from network call into a List<BuoyOverlayItem>
        buoys = getBuoyOverlayItems(buoyDataList)

        // send message to Handler to update UI
        handler sendEmptyMessage 1
      }
    }
    th.start()
  }

  private def getBuoyOverlayItems(buoyDataList: List[BuoyData]): List[BuoyOverlayItem] = {
    var buoyOverylayItemList = new ListBuffer[BuoyOverlayItem]
    for (bd <- buoyDataList) {
      if (bd.geoRssPoint != null) {
        val gp = LocationHelper.getGeoPoint(bd.geoRssPoint)
        if (gp != null) {
          val boi = new BuoyOverlayItem(gp, bd)
          buoyOverylayItemList append boi
        } else {
          Log.w(Constants.LOGTAG, " " + CLASSTAG +
                "  buoy WITH incomplete geoRssPoint data skipped - " + bd.title)
        }
      } else {
        Log.w(Constants.LOGTAG, " " + CLASSTAG +
              "  buoy WITHOUT geoRssPoint data skipped - " + bd.title)
      }
    }
    buoyOverylayItemList.toList
  }
}

object MapViewActivity {

  private final val CLASSTAG = classOf[MapViewActivity].getSimpleName

  private final val MENU_SET_SATELLITE = 1
  private final val MENU_SET_MAP = 2
  private final val MENU_BUOYS_FROM_MAP_CENTER = 3
  private final val MENU_BACK_TO_LAST_LOCATION = 4

}
