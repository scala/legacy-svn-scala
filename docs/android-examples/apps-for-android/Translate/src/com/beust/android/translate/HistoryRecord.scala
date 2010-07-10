/*
 * Copyright (C) 2008 Google Inc.
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

package com.beust.android.translate

import com.beust.android.translate.Languages.Language

/**
 * This class describes one entry in the history
 */
class HistoryRecord(val from: Language, val to: Language,
                    val input: String, val output: String, val when: Long) {
  import HistoryRecord._  // companion object

  override def equals(o: Any): Boolean = {
    if (o == null) return false
    if (o == this) return true
    o match {
      case other: HistoryRecord =>
        (other.from equals from) && (other.to equals to) &&
        (other.input equals input) && (other.output equals output)
      case _ =>
        false
    }
  }

  override def hashCode: Int =
    from.hashCode ^ to.hashCode ^ input.hashCode ^ output.hashCode

  def encode: String =
    from.name + SEPARATOR + to.name + SEPARATOR + input +
                SEPARATOR + output + SEPARATOR + when

  override def toString = encode
    
}

object HistoryRecord {

  private final val SEPARATOR = "@"

  def decode(s: String): HistoryRecord = {
    val o = s split SEPARATOR
    var i = 0
    val from = Language(o(i)); i += 1
    val to = Language(o(i)); i += 1
    val input = o(i); i += 1
    val output = o(i); i += 1
    val when = o(i).toLong
    new HistoryRecord(from, to, input, output, when)
  }

}

