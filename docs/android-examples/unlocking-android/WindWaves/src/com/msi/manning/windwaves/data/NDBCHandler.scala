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

import org.xml.sax.{Attributes, SAXException}
import org.xml.sax.helpers.DefaultHandler

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import scala.collection.mutable.ListBuffer

/**
 * SAX Handler impl for National Data Buoy Center RSS feeds.
 * 
 * This class has some ugly because of the way the feed is variable, and the
 * way the feed embeds data in HTML formatted CDATA (rather than using defined
 * fields).
 * 
 * @author charliecollins
 * 
 */
class NDBCHandler extends DefaultHandler {
  import NDBCHandler._  // companion object

  /*
   * <item> <pubDate>Sun, 16 Nov 2008 16:30:01 UT</pubDate> <title>Station 46091</title>
   * <description><![CDATA[ <strong>November 16, 2008 7:50 am PST</strong><br />
   * <strong>Location:</strong> 36.753N 122.423W or 25 nautical miles SW of search location of 37N
   * 122W.<br /> <strong>Wind Direction:</strong> NE (40&#176;)<br /> <strong>Wind Speed:</strong>
   * 8 knots<br /> <strong>Wind Gust:</strong> 10 knots<br /> <strong>Significant Wave
   * Height:</strong> 5 ft<br /> <strong>Dominant Wave Period:</strong> 13 sec<br />
   * <strong>Atmospheric Pressure:</strong> 30.07 in (1018.3 mb)<br /> <strong>Pressure
   * Tendency:</strong> +0.03 in (+1.0 mb)<br /> <strong>Air Temperature:</strong> 60&#176;F
   * (15.5&#176;C)<br /> <strong>Water Temperature:</strong> 57&#176;F (13.7&#176;C)<br />
   * ]]></description>
   * 
   * <link>http://www.ndbc.noaa.gov/station_page.php?station=46091</link>
   * <guid>http://www.ndbc.noaa.gov/station_page.php?station=46091</guid> <georss:point>36.835
   * -121.899</georss:point> </item>
   */

  private val buoys = new ListBuffer[BuoyData]
  private var buoy: BuoyData = _

  private var inItem: Boolean = _
  private var inTitle: Boolean = _
  private var inPubDate: Boolean = _
  private var inDesc: Boolean = _
  private var inLink: Boolean = _
  private var inGeoRss: Boolean = _

  Log.d(Constants.LOGTAG, CLASSTAG + " NDBCHandler instantiated")

  override def characters(ch: Array[Char], start: Int, length: Int) {
    var chString = if (ch != null) new String(ch, start, length) else ""

    if (inTitle) {
      buoy.title = chString
    }
    if (inPubDate) {
      // hack to replace "UT" with GMT (UT won't parse)
      if (chString contains "UT") {
        chString = chString.replace("UT", "GMT")
      }
      buoy.dateString = chString

      // try to handle the various date formats
      var pubDate: Date = null
      try {
        pubDate = NDBCHandler.DATE_FORMAT_A.parse(chString)
      } catch {
        case e: ParseException => // swallow
      }
      if (pubDate == null) {
        try {
          pubDate = NDBCHandler.DATE_FORMAT_B.parse(chString)
        } catch {
          case e: ParseException => // swallow
        }
      }
      buoy.date = pubDate
    }

    if (inDesc) {
      buoy.description = chString
      // parse the description into separate elements (annoying, but the feed
      // puts all this data in one CDATA block)
      val descArray = chString split "<br />"
      for (s <- descArray) {
        if (s.indexOf("Location:</strong>") != -1) {
          buoy.location = s.substring(s.indexOf("Location:</strong>") + 18, s.length).trim()
        } else if (s.indexOf("Wind Direction:</strong>") != -1) {
          buoy.windDirection = s.substring(s.indexOf("Wind Direction:</strong>") + 24, s.length)
                        .trim().replaceAll("&#176;", "");
        } else if (s.indexOf("Wind Speed:</strong>") != -1) {
          buoy.windSpeed = s.substring(s.indexOf("Wind Speed:</strong>") + 20, s.length).trim()
        } else if (s.indexOf("Wind Gust:</strong>") != -1) {
          buoy.windGust = s.substring(s.indexOf("Wind Gust:</strong>") + 19, s.length).trim()
        } else if (s.indexOf("Significant Wave Height:</strong>") != -1) {
          buoy.waveHeight = s.substring(s.indexOf("Significant Wave Height:</strong>") + 33, s.length)
                        .trim()
        } else if (s.indexOf("Dominant Wave Period:</strong>") != -1) {
          buoy.wavePeriod = s.substring(s.indexOf("Dominant Wave Period:</strong>") + 30, s.length)
                        .trim()
        } else if (s.indexOf("Atmospheric Pressure:</strong>") != -1) {
          buoy.atmoPressure = s.substring(s.indexOf("Atmospheric Pressure:</strong>") + 30, s.length)
                        .trim()
        } else if (s.indexOf("Pressure Tendency:</strong>") != -1) {
          buoy.atmoPressureTendency = s.substring(s.indexOf("Pressure Tendency:</strong>") + 27,
                        s.length).trim()
        } else if (s.indexOf("Air Temperature:</strong>") != -1) {
          buoy.airTemp = s.substring(s.indexOf("Air Temperature:</strong>") + 25, s.length).trim()
                        .replaceAll("&#176;", "")
        } else if (s.indexOf("Water Temperature:</strong>") != -1) {
          buoy.waterTemp = s.substring(s.indexOf("Water Temperature:</strong>") + 27, s.length).trim()
                        .replaceAll("&#176;", "")
        }
      }
    }

    if (inLink) {
      buoy.link = chString
    }

    if (inGeoRss) {
      buoy.geoRssPoint = chString
    }
  }

  @throws(classOf[SAXException])
  override def endDocument() {
  }

  @throws(classOf[SAXException])
  override def endElement(namespaceURI: String, localName: String, qName: String) {
    if (localName equals NDBCHandler.ITEM) {
      inItem = false
      buoys append buoy
    }

    if (inItem) localName match {
      case NDBCHandler.TITLE   => inTitle = false
      case NDBCHandler.PUBDATE => inPubDate = false
      case NDBCHandler.DESC    => inDesc = false
      case NDBCHandler.LINK    => inLink = false
      case NDBCHandler.GEORSS  => inGeoRss = false
      case _ =>
    }
  }

  def getBuoyDataList: List[BuoyData] = buoys.toList

  @throws(classOf[SAXException])
  override def startDocument() {
  }

  @throws(classOf[SAXException])
  override def startElement(namespaceURI: String, localName: String,
                   fiqName: String, atts: Attributes) {
    if (localName equals NDBCHandler.ITEM) {
      inItem = true
      buoy = new BuoyData()
    }

    if (inItem) localName match {
      case NDBCHandler.TITLE   => inTitle = true
      case NDBCHandler.PUBDATE => inPubDate = true
      case NDBCHandler.DESC    => inDesc = true
      case NDBCHandler.LINK    => inLink = true
      case NDBCHandler.GEORSS  => inGeoRss = true
      case _ =>
    }
  }
}

object NDBCHandler {
  private final val CLASSTAG = classOf[NDBCHandler].getSimpleName

  private final val ITEM = "item"
  private final val PUBDATE = "pubDate"
  private final val TITLE = "title"
  private final val DESC = "description"
  private final val LINK = "link"
  private final val GEORSS = "point"

  private final val DATE_FORMAT_A = new SimpleDateFormat("MMMM dd',' yyyy hh:mm a z")
  private final val DATE_FORMAT_B = new SimpleDateFormat("EEE',' dd MMM yyyy hh:mm:ss z")
}
