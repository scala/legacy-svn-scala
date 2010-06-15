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

import org.xml.sax.{Attributes, SAXException}
import org.xml.sax.helpers.DefaultHandler

import java.text.{ParseException, SimpleDateFormat}
import java.util.ArrayList

/**
 * SAX Handler impl for Google Base API and restaurant Review bean.
 * 
 * @author charliecollins
 */
class ReviewHandler extends DefaultHandler {
  import ReviewHandler._  // companion object

  private var authorChars: Boolean = _
  private var contentChars: Boolean = _
  private var dateChars: Boolean = _
  private var imageLinkChars: Boolean = _
  private var locationChars: Boolean = _
  private var nameChars: Boolean = _
  private var numEntries: Int = _
  private var phoneChars: Boolean = _
  private var ratingChars: Boolean = _
  private var review: Review = _
  private val reviews = new ArrayList[Review]
  private var startEntry: Boolean = _

  @throws(classOf[SAXException])
  override def startDocument() {
  }

  @throws(classOf[SAXException])
  override def startElement(namespaceURI: String, localName: String,
                            qName: String, atts: Attributes) {
    if (localName equals ReviewHandler.ENTRY) {
      startEntry = true
      review = new Review()
    }

    if (startEntry) localName match {
      case ReviewHandler.R_NAME =>
        nameChars = true
      case ReviewHandler.R_AUTHOR =>
        authorChars = true
      case ReviewHandler.R_LINK =>
        val rel = getAttributeValue("rel", atts)
        if (rel != null && rel.equals("alternate")) {
          review.link = getAttributeValue("href", atts)
        }
      case ReviewHandler.R_LOCATION =>
        locationChars = true
      case ReviewHandler.R_RATING =>
        ratingChars = true
      case ReviewHandler.R_PHONE =>
        phoneChars = true
      case ReviewHandler.R_DATE =>
        dateChars = true
      case ReviewHandler.R_CONTENT =>
        contentChars = true
      case ReviewHandler.R_IMAGE_LINK =>
        imageLinkChars = true
      case _ =>
    }
  }

  @throws(classOf[SAXException])
  override def endDocument() {
  }

  @throws(classOf[SAXException])
  override def endElement(namespaceURI: String, localName: String,
                          qName: String) {
    if (localName equals ReviewHandler.ENTRY) {
      startEntry = false
      numEntries += 1
      reviews add review
    }

    if (startEntry) localName match {
      case ReviewHandler.R_NAME =>
        nameChars = false
      case ReviewHandler.R_AUTHOR =>
        authorChars = false
      case ReviewHandler.R_LOCATION =>
        locationChars = false
      case ReviewHandler.R_RATING =>
        ratingChars = false
      case ReviewHandler.R_PHONE =>
        phoneChars = false
      case ReviewHandler.R_DATE =>
        dateChars = false
      case ReviewHandler.R_CONTENT =>
        contentChars = false
      case ReviewHandler.R_IMAGE_LINK =>
        imageLinkChars = false
      case _ =>
    }
  } 
    
  override def characters(ch: Array[Char], start: Int, length: Int) {
    var chString = ""
    if (ch != null) {
      chString = new String(ch, start, length)
    }

    if (startEntry) {
      if (nameChars) {
        review.name = chString
      } else if (authorChars) {
        review.author = chString
      } else if (locationChars) {
        review.location = chString
      } else if (ratingChars) {
        review.rating = chString
      } else if (phoneChars) {
        review.phone = chString
      } else if (this.dateChars) {
        if (ch != null) {
          try {
            review.date = ReviewHandler.DATE_FORMAT.parse(chString)
           } catch {
             case e: ParseException => e.printStackTrace()
           }
        }
      } else if (contentChars) {
        review.content = new String(chString)
      } else if (imageLinkChars) {
        review.imageLink = new String(chString)
      }
    }
  }

  def getReviews: ArrayList[Review] = reviews

}

object ReviewHandler {
  private final val DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")
  private final val ENTRY = "entry"
  private final val R_AUTHOR = "review_author"
  private final val R_CONTENT = "content"
  private final val R_DATE = "review_date"
  private final val R_IMAGE_LINK = "image_link"
  private final val R_LINK = "link"
  private final val R_LOCATION = "location"
  private final val R_NAME = "name_of_item_reviewed"
  private final val R_PHONE = "phone_of_item_reviewed"
  private final val R_RATING = "rating"

  private def getAttributeValue(attrName: String, attrs: Attributes): String = {
    var result: String = null
    for (i <- 0 until attrs.getLength) {
      val thisAttr = attrs.getLocalName(i)
      if (attrName equals thisAttr) {
        return attrs.getValue(i)
      }
    }
    result
  }
}
