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

import android.util.Log

import com.msi.manning.windwaves.Constants

import org.xml.sax.{InputSource, XMLReader}

import java.net.URL
import java.util.ArrayList

import javax.xml.parsers.{SAXParser, SAXParserFactory}

/**
 * Invoke NDBC API and parse.
 * 
 * @see NDBCHandler
 * 
 * @author charliecollins
 * 
 * Network data fetcher for NDBC RSS feeds, requires lat in such as 37W,
 * and long in such as 122E (not integers or doubles, Strings).
 * 
 * @param lat
 * @param lon
 * @param rad
 */
class NDBCFetcher(lat: String, lon: String, rad: String) {
  import NDBCFetcher._  // companion object

  private val query = QBASE + QLAT + lat + QLON + lon + QRAD + rad
  Log.i(Constants.LOGTAG, CLASSTAG + " query - " + query)

  def getData: List[BuoyData] =
    try {
      val url = new URL(query)
      val spf = SAXParserFactory.newInstance()
      val sp = spf.newSAXParser
      val xr = sp.getXMLReader
      val handler = new NDBCHandler()
      xr setContentHandler handler
      xr parse new InputSource(url.openStream())
      // after parsed, get data
      handler.getBuoyDataList
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }

}

object NDBCFetcher {
  private final val CLASSTAG = classOf[NDBCFetcher].getSimpleName

  private final val QBASE = "http://www.ndbc.noaa.gov/rss/ndbc_obs_search.php?"
  private final val QLAT = "lat="
  private final val QLON = "&lon="
  private final val QRAD = "&radius="
}
