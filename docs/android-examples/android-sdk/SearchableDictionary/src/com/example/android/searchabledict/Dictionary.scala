/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.searchabledict

import android.content.res.Resources
import android.text.TextUtils
import android.util.Log

import java.io.{BufferedReader, IOException, InputStream, InputStreamReader}
import scala.collection.mutable.{HashMap, ListBuffer, Map, SynchronizedMap}

/**
 * Contains logic to load the word of words and definitions and find a list of
 * matching words given a query.  Everything is held in memory; this is not a
 * robust way to serve lots of words and is only for demo purposes.
 *
 * You may want to consider using an SQLite database. In practice, you'll want
 * to make sure your suggestion provider is as efficient as possible, as the
 * system will be taxed while performing searches across many sources for each
 * keystroke the user enters into Quick Search Box.
 */
class Dictionary {
  import Dictionary._  // companion object

  private final val mDict: Map[String, ListBuffer[Word]] =
    new HashMap[String, ListBuffer[Word]]()
      with SynchronizedMap[String, ListBuffer[Word]]

  private var mLoaded = false

  /**
   * Loads the words and definitions if they haven't been loaded already.
   *
   * @param resources Used to load the file containing the words and definitions.
   */
  def ensureLoaded(resources: Resources): Unit = synchronized {
    if (mLoaded) return

    val th = new Thread(new Runnable() {
      def run() {
        try {
          loadWords(resources)
        } catch {
          case e: IOException =>
            throw new RuntimeException(e)
        }
      }
    })
    th.start()
  }

  @throws(classOf[IOException])
  private def loadWords(resources: Resources): Unit = synchronized {
    if (mLoaded) return

    Log.d("dict", "loading words")
    val inputStream = resources openRawResource R.raw.definitions
    val reader = new BufferedReader(new InputStreamReader(inputStream))

    try {
      var line = reader.readLine()
      while (line != null) {
        val strings = TextUtils.split(line, "-")
        if (strings.length >= 2)
          addWord(strings(0).trim(), strings(1).trim())
        line = reader.readLine()
      }
    } finally {
      reader.close()
    }
    mLoaded = true
  }


  def getMatches(query: String): List[Word] =
    mDict get query match {
      case Some(list) => list.toList
      case None => Nil
    }

  private def addWord(word: String, definition: String) {
    val theWord = Word(word, definition)

    val len = word.length
    for (i <- 0 until len) {
      val prefix = word.substring(0, len - i)
      addMatch(prefix, theWord)
    }
  }

  private def addMatch(query: String, word: Word) {
    mDict get query match {
      case Some(list) => list += word
      case None => mDict(query) = new ListBuffer() += word
    }
  }

}

object Dictionary {

  case class Word(word: String, definition: String)

  private final val sInstance = new Dictionary()

  def getInstance: Dictionary = sInstance

}
