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

import android.app.ListActivity
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.{Menu, MenuInflater, MenuItem, View}
import android.widget.{AdapterView, SimpleAdapter}
import android.widget.AdapterView.OnItemClickListener

import scala.collection.immutable.{HashMap, Map}
import scala.collection.mutable.ListBuffer

/**
 * This activity displays the history of past translations.
 *
 * @author Cedric Beust
 * @author Daniel Rall
 */
class HistoryActivity extends ListActivity with OnItemClickListener {
  import HistoryActivity._  // companion object

  private var mAdapter: SimpleAdapter = _
  private var mListData = new ListBuffer[Map[String, String]]()
  private var mHistory: History = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.history_activity)
        
    mHistory = new History(TranslateActivity.getPrefs(this))
    initializeAdapter(mHistory.getHistoryRecordsMostRecentFirst)
    getListView setEmptyView findViewById(R.id.empty)
  }
    
  private def initializeAdapter(historyRecords: List[HistoryRecord]) {
    for (hr <- historyRecords) {
      val data = HashMap.empty[String, String] ++ List(
        // Values that are bound to views
        INPUT -> hr.input,
        OUTPUT -> hr.output,
        FROM -> hr.from.name.toLowerCase,
        TO -> hr.to.name.toLowerCase,
        // Extra values we keep around for convenience
        FROM_SHORT_NAME -> hr.from.shortName,
        TO_SHORT_NAME -> hr.to.shortName)
      mListData += data
    }

    import scala.collection.JavaConversions._
    mAdapter = new SimpleAdapter(this, mListData map (data => asMap(data)),
                                 R.layout.history_record,
                                 COLUMN_NAMES, VIEW_IDS)
    getListView setAdapter mAdapter
    getListView setOnItemClickListener this
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.history_activity_menu, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.most_recent =>
        initializeAdapter(mHistory.getHistoryRecordsMostRecentFirst)

      case R.id.languages =>
        initializeAdapter(mHistory.getHistoryRecordsByLanguages)

      case R.id.clear_history =>
        mHistory clear this
        initializeAdapter(mHistory.getHistoryRecordsByLanguages)

      case _ =>
    }
    true
  }

  def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
    val data = parent.getItemAtPosition(position).asInstanceOf[Map[String, String]]
    val edit: Editor = TranslateActivity.getPrefs(this).edit()
    TranslateActivity.savePreferences(edit,
                data(FROM_SHORT_NAME), data(TO_SHORT_NAME), 
                data(INPUT), data(OUTPUT))
    finish()
  }

}

object HistoryActivity {

  private final val INPUT = "input"
  private final val OUTPUT = "output"
  private final val FROM = "from"
  private final val TO = "to"
  private final val FROM_SHORT_NAME = "from-short-name";
  private final val TO_SHORT_NAME = "to-short-name";

  // These constants are used to bind the adapter to the list view
  private final val COLUMN_NAMES = Array(INPUT, OUTPUT, FROM, TO)
  private final val VIEW_IDS = Array(R.id.input, R.id.output, R.id.from, R.id.to)

}
