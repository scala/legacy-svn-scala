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

import android.content.{Context, SharedPreferences}
import android.content.SharedPreferences.Editor
import android.util.Log

import scala.collection.mutable.ListBuffer

/**
 * This class handles the history of past translations.
 */
object History {
  private final val HISTORY = "history"

  /**
   * Sort the translations by timestamp.
   */ /*
  private final val MOST_RECENT_COMPARATOR = new Comparator[HistoryRecord]() {
    def compare(object1: HistoryRecord, object2: HistoryRecord): Int =
      (object2.when - object1.when).toInt
    }
  }*/

  /**
   * Sort the translations by destination language and then by input.
   */ /*
  private final LANGUAGE_COMPARATOR = new Comparator[HistoryRecord]() {
    def compare(object1: HistoryRecord, object2: HistoryRecord): Int = {
      val result = object1.to.getLongName compareTo object2.to.getLongName
      if (result == 0) object1.input compareTo object2.input
      else result
    }
  }*/

  def restoreHistory(prefs: SharedPreferences): List[HistoryRecord] = {
    import scala.collection.JavaConversions._
    val result = new ListBuffer[HistoryRecord]()
    var done = false
    var i = 0
    val allKeys = asMap(prefs.getAll)
    for (key <- allKeys.keySet) {
      if (key startsWith HISTORY) {
        val value = allKeys(key).toString
        result += HistoryRecord.decode(value)
      }
    }
//  while (! done) {
//    val history = prefs.getString(HISTORY + "-" + i++, null)
//    if (history != null) {
//      result.add(HistoryRecord.decode(history))
//    } else {
//      done = true
//    }
//  }

    result.toList
  }

//    public void saveHistory(Editor edit) {
//        log("Saving history");
//        for (int i = 0; i < mHistoryRecords.size(); i++) {
//            HistoryRecord hr = mHistoryRecords.get(i);
//            edit.putString(HISTORY + "-" + i, hr.encode());
//        }
//    }
    
  def addHistoryRecord(context: Context,
                       from: Language, to: Language,
                       input: String, output: String) {
    val historyRecord = new History(TranslateActivity.getPrefs(context))
    val hr = new HistoryRecord(from, to, input, output, System.currentTimeMillis)
        
    // Find an empty key to add this history record
    val prefs = TranslateActivity.getPrefs(context)
    var i = 0
    while (true) {
      val key = HISTORY + "-" + i
      if (!prefs.contains(key)) {
        val edit = prefs.edit()
        edit.putString(key, hr.encode)
        log("Committing " + key + " " + hr.encode)
        edit.commit()
        return
      } else {
        i += 1
      }
    }
  }
    
//    public static void addHistoryRecord(Context context, List<HistoryRecord> result, HistoryRecord hr) {
//        if (! result.contains(hr)) {
//            result.add(hr);
//        }
//        Editor edit = getPrefs(context).edit();
//    }

  private def log(s: String) {
    Log.d(TranslateActivity.TAG, "[History] " + s)
  }
    
}

class History(prefs: SharedPreferences) {
  import History._  // companion object

  private var mHistoryRecords = restoreHistory(prefs)

  def getHistoryRecordsMostRecentFirst: List[HistoryRecord] = {
    //Collections.sort(mHistoryRecords, MOST_RECENT_COMPARATOR)
    mHistoryRecords sortWith (_.when < _.when)
  }
    
  def getHistoryRecordsByLanguages: List[HistoryRecord] = {
    //Collections.sort(mHistoryRecords, LANGUAGE_COMPARATOR)
    def languageComparator(hr1: HistoryRecord, hr2: HistoryRecord) =
      (hr1.to.longName < hr2.to.longName) || (hr1.input < hr2.input)
    mHistoryRecords sortWith languageComparator
  }
/*
  def getHistoryRecords(comparator: Comparator[HistoryRecord]) {
    if (comparator != null) {
            Collections.sort(mHistoryRecords, comparator);
    }
    mHistoryRecords
  }
*/
  def clear(context: Context) {
    val size = mHistoryRecords.size
    mHistoryRecords = List()
    val edit = TranslateActivity.getPrefs(context).edit()
    for (i <- 0 until size) {
      val key = HISTORY + "-" + i
      log("Removing key " + key)
      edit remove key
    }
    edit.commit()
  }

}
