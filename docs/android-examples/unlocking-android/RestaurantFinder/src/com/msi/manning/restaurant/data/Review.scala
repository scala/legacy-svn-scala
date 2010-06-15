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

import java.util.Date

/**
 * Simple data bean to represent a Review - no getters and setters on purpose
 * (more efficient on Android).
 * 
 * @author ccollins
 */
class Review {

  var author: String = _
  var content: String = _
  var cuisine: String = _ // input only
  var date: Date = _
  var imageLink: String = _
  var link: String = _
  var location: String = _
  var name: String = _
  var phone: String = _
  var rating: String = _

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("*Review*\n")
    sb.append("name:" + name + "\n")
    sb.append("author:" + author + "\n")
    sb.append("link:" + link + "\n")
    sb.append("imageLink:" + imageLink + "\n")
    sb.append("location:" + location + "\n")
    sb.append("phone:" + phone + "\n")
    sb.append("rating:" + rating + "\n")
    sb.append("date:" + date + "\n")
    sb.append("content:" + content)
    sb.toString
  }
}
