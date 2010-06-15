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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

import scala.collection.mutable.ListBuffer

import com.msi.manning.windwaves.data.BuoyData

// ENHANCE build charts from the realtime data (http://www.ndbc.noaa.gov/data/realtime2/41002.txt)
// ENHANCE hook in with other NOAA data - http://www.weather.gov/rss/

/**
 * BuoyDetail Activity for WindWaves.
 * 
 * @author charliecollins
 */
class BuoyDetailActivity extends Activity {
  import BuoyDetailActivity._  // companion object

  private var buttonWeb: Button = _
  private var title: TextView = _
  private var location: TextView = _
  private var date: TextView = _
  private var airTemp: TextView = _
  private var waterTemp: TextView = _
  private var atmoPress: TextView = _
  private var atmoTend: TextView = _
  private var windDir: TextView = _
  private var windSpeed: TextView = _
  private var windGust: TextView = _
  private var waveHeight: TextView = _
  private var wavePeriod: TextView = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onCreate")

    setContentView(R.layout.buoydetail_activity)

    buttonWeb = findView(R.id.button_web)
    title = findView(R.id.bd_title)
    location = findView(R.id.bd_location)
    date = findView(R.id.bd_date)
    airTemp = findView(R.id.bd_air_temp)
    waterTemp = findView(R.id.bd_water_temp)
    atmoPress = findView(R.id.bd_atmo_press)
    atmoTend = findView(R.id.bd_atmo_tend)
    windDir = findView(R.id.bd_wind_dir)
    windSpeed = findView(R.id.bd_wind_speed)
    windGust = findView(R.id.bd_wind_gust)
    waveHeight = findView(R.id.bd_wave_height)
    wavePeriod = findView(R.id.bd_wave_period)

    val bd = BuoyDetailActivity.buoyData
    title.setText(bd.title)
    location.setText("Location:" + bd.location)
    def getValue(s: String) = if (s != null) s else "NA"
    date.setText("Data Poll Date: " + getValue(bd.dateString))
    airTemp.setText("Air Temp: " + getValue(bd.airTemp))
    waterTemp.setText("Water Temp: " + getValue(bd.waterTemp))
    atmoPress.setText("Barometric Press: " + getValue(bd.atmoPressure))
    atmoTend.setText("Barometric Trend: " + getValue(bd.atmoPressureTendency))
    windDir.setText("Wind Direction: " + getValue(bd.windDirection))
    windSpeed.setText("Wind Speed: " + getValue(bd.windSpeed))
    windGust.setText("Wind Gust: " + getValue(bd.windGust))
    waveHeight.setText("Wave Height: " + getValue(bd.waveHeight))
    wavePeriod.setText("Wave Period: " + getValue(bd.wavePeriod))

    buttonWeb setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                                 Uri.parse(BuoyDetailActivity.buoyData.link)))
      }
    }
  }

  override def onStart() {
    super.onStart()
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onStart")
  }

  override def onResume() {
    super.onResume()
  }

  override def onPause() {
    super.onPause()
  }

  @inline
  private final def findView[V <: View](id: Int) =
    findViewById(id).asInstanceOf[V]
}

object BuoyDetailActivity {
  private final val CLASSTAG = classOf[BuoyDetailActivity].getSimpleName

  final val BUOY_DETAIL_URL = "http://www.ndbc.noaa.gov/station_page.php?station="

  var buoyData: BuoyData = null
}
