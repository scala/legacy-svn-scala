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

import android.location.Location
import android.util.Log

import com.google.android.maps.GeoPoint

import java.text.DecimalFormat

object LocationHelper {

  final val CLASSTAG = LocationHelper.getClass.getSimpleName

  final val MILLION = 1e6

  private final val DEC_FORMAT = new DecimalFormat("###.##")

  final val GOLDEN_GATE =
    new GeoPoint((37.49 * MILLION).toInt, (-122.49 * MILLION).toInt)

  // note GeoPoint stores lat/long as "integer numbers of microdegrees"
  // meaning int*1E6
  // parse Location into GeoPoint
  def getGeoPoint(loc: Location): GeoPoint = {
    val lat = (loc.getLatitude * MILLION).toInt
    val lon = (loc.getLongitude * MILLION).toInt
    new GeoPoint(lat, lon)
  }

  // parse geoRssPoint into GeoPoint(<georss:point>36.835 -121.899</georss:point>)
  def getGeoPoint(geoRssPoint: String): GeoPoint = {
    Log.d(Constants.LOGTAG, CLASSTAG + " getGeoPoint - geoRssPoint - " + geoRssPoint)
    val gPoint = geoRssPoint.trim()
    if (gPoint.indexOf(" ") != -1) {
      val latString = gPoint.substring(0, gPoint indexOf " ")
      val lonString = gPoint.substring(gPoint indexOf " ", gPoint.length)
      val latd = java.lang.Double.parseDouble(latString)
      val lond = java.lang.Double.parseDouble(lonString)
      val lat = (latd * MILLION).toInt
      val lon = (lond * MILLION).toInt
      new GeoPoint(lat, lon)
    } else
      null
  }

  // parse double point(-127.50) into String (127.50W)
  def parsePoint(point: Double, isLat: Boolean): String = {
    Log.d(Constants.LOGTAG, CLASSTAG + " parsePoint - point - " +
          point + " isLat - " + isLat)
    var result = LocationHelper.DEC_FORMAT.format(point)
    if (result.indexOf("-") != -1) {
      result = result.substring(1, result.length)
    }
    // latitude is decimal expressed as +- 0-90
    // (South negative, North positive, from Equator)
    if (isLat) {
      result += (if (point < 0) "S" else "N")
    }
    // longitude is decimal expressed as +- 0-180
    // (West negative, East positive, from Prime Meridian)
    else {
      result += (if (point < 0) "W" else "E")
    }
    Log.d(Constants.LOGTAG, CLASSTAG + " parsePoint result - " + result)
    result
  }
}
