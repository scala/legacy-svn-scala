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

package com.msi.manning.restaurant.data

import android.util.Log

import com.msi.manning.restaurant.Constants._

import org.xml.sax.{InputSource, XMLReader}

import java.io.UnsupportedEncodingException
import java.net.{URL, URLEncoder}

import javax.xml.parsers.{SAXParser, SAXParserFactory}

import scala.collection.JavaConversions._

/**
 * Use Google Base with specified criteria to obtain Review data.
 * 
 * @author charliecollins
 *
 * Construct ReviewFetcher with location, description, rating, and paging params.
 * 
 * @param location
 * @param description
 * @param rating
 * @param start
 * @param numResults
 */
class ReviewFetcher(loc: String, description: String, rat: String,
                    start: Int, numResults: Int) {
  import ReviewFetcher._  // companion object

  private var query: String = _

  Log.v(LOGTAG, " " + CLASSTAG +
        " location = " + loc + " rating = " + rat +
        " start = " + start + " numResults = " + numResults)

  { // initialization
    var location = loc
    var rating = rat

    // urlencode params
    try {
      if (location != null) {
        location = URLEncoder.encode(location, "UTF-8")
      }
      if (rating != null) {
        rating = URLEncoder.encode(rating, "UTF-8")
      }
    } catch {
      case e: UnsupportedEncodingException =>
        e.printStackTrace()
    }

    // build query
    query = QBASE
    if ((rating != null) && !rating.equals("ALL")) {
      query += (QR_PREFIX + rating + QR_SUFFIX)
    }
    if ((location != null) && !location.equals("")) {
      query += (QL_PREFIX + location + QL_SUFFIX)
    }
    if ((description != null) && !description.equals("ANY")) {
      query += (QD_PREFIX + description + QD_SUFFIX)
    }
    query += (QSTART_INDEX + start + QMAX_RESULTS + numResults)

    Log.v(LOGTAG, " " + CLASSTAG + " query - " + query)
  }

  /**
   * Call Google Base and parse via SAX.
   * 
   * @return
   */
  def getReviews: List[Review] = {
    val startTime = System.currentTimeMillis
    var results: List[Review] = null

    try {
      val url = new URL(this.query)
      val spf = SAXParserFactory.newInstance()
      val sp = spf.newSAXParser()
      val xr = sp.getXMLReader

      val handler = new ReviewHandler()
      xr setContentHandler handler

      xr parse new InputSource(url.openStream)
      // after parsed, get record
      results = new JListWrapper(handler.getReviews).toList
    } catch {
      case e: Exception =>
        Log.e(LOGTAG, " " + CLASSTAG, e)
    }
    val duration = System.currentTimeMillis - startTime
    Log.v(LOGTAG, " " + CLASSTAG + " call and parse duration - " + duration)
    results
  }
}

object ReviewFetcher {
  private final val CLASSTAG = classOf[ReviewFetcher].getSimpleName
  private final val QBASE = "http://www.google.com/base/feeds/snippets/-/reviews?bq=[review%20type:restaurant]"
  private final val QD_PREFIX = "[description:"
  private final val QD_SUFFIX = "]"
  private final val QL_PREFIX = "[location:"
  private final val QL_SUFFIX = "]"
  private final val QMAX_RESULTS = "&max-results="
  private final val QR_PREFIX = "[rating:"
  private final val QR_SUFFIX = "]"
  private final val QSTART_INDEX = "&start-index="
}
