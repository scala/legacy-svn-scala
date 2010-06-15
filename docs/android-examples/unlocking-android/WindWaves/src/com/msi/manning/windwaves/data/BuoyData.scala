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

package com.msi.manning.windwaves.data

import java.util.Date

class BuoyData {

  var date: Date = _
  var dateString: String = _
  var title: String = _
  var description: String = _
  var location: String = _
  var windDirection: String = _
  var windSpeed: String = _
  var windGust: String = _
  var waveHeight: String = _
  var wavePeriod: String = _
  var atmoPressure: String = _
  var atmoPressureTendency: String = _
  var airTemp: String = _
  var waterTemp: String = _
  var link: String = _
  var geoRssPoint: String = _

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("BuoyData - " + title)
    sb.append("\ndate - " + date)
    sb.append("\ndateString - " + dateString)
    sb.append("\nlink - " + link)
    sb.append("\ngeoRssPoint - " + this.geoRssPoint)
    sb.append("\ndescription - " + this.description)
    sb.append("\nlocation - " + this.location)
    sb.append("\nwindDirection - " + this.windDirection)
    sb.append("\nwindSpeed - " + this.windSpeed)
    sb.append("\nwindGust - " + this.windGust)
    sb.append("\nwaveHeight - " + this.waveHeight)
    sb.append("\nwavePeriod - " + wavePeriod)
    sb.append("\natmoPressure - " + atmoPressure)
    sb.append("\natmoPressureTendency - " + atmoPressureTendency)
    sb.append("\nairTemp - " + airTemp)
    sb.append("\nwaterTemp - " + waterTemp)
    sb.toString()
  }
}
