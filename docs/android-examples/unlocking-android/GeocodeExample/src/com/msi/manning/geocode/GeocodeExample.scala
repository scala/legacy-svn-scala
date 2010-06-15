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

package com.msi.manning.geocode

import android.app.Activity
import android.location.{Address, Geocoder}
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, CheckBox, EditText, TextView}

import java.io.IOException

class GeocodeExample extends Activity {

  private var input: EditText = _
  private var output: TextView = _
  private var isAddress: CheckBox = _
  private var button: Button = _

  private def find[V <: View](id: Int) = findViewById(id).asInstanceOf[V]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    input = find(R.id.input)
    output = find(R.id.output)
    button = find(R.id.geocode_button)
    isAddress = find(R.id.checkbox_address)

    button setOnClickListener new OnClickListener() {
      def onClick(v: View) {
         output setText performGeocode(input.getText.toString, isAddress.isChecked)
      }
    }
  }

  // Note that this is not at all robust, rather it's a quick and dirty example
  // (splitting a string and then parsing as a double is a brittle approach,
  // use more checks in production code)
  private def performGeocode(in: String, isAddr: Boolean): String = {
    var result = "Unable to Geocode - " + in
    if (input != null) {
      val geocoder = new Geocoder(this)
      if (isAddr) {
        try {
          val addresses = geocoder.getFromLocationName(in, 1)
          if (addresses != null) {
            result = addresses.get(0).toString
          }
        } catch {
          case e: IOException => Log.e("GeocodExample", "Error", e)
        }
      } else {
        try {
          val coords = in split ","
          if ((coords != null) && (coords.length == 2)) {
            val x = java.lang.Double.parseDouble(coords(0))
            val y = java.lang.Double.parseDouble(coords(1))
            val addresses = geocoder.getFromLocation(x, y, 1)
            result = addresses.get(0).toString
          }
        } catch {
          case e: IOException => Log.e("GeocodExample", "Error", e)
        }
      }
    }
    result
  }
}
