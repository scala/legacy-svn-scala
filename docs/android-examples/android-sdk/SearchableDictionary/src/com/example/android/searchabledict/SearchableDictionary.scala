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

import android.app.{Activity, SearchManager}
import android.content.{Context, Intent}
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.{LayoutInflater, View, ViewGroup, Menu, MenuItem}
import android.widget.{AdapterView, BaseAdapter, ListView, TextView,
                       TwoLineListItem}

/**
 * The main activity for the dictionary.  Also displays search results
 * triggered by the search dialog.
 */
class SearchableDictionary extends Activity {
  import SearchableDictionary._  // companion object

  private var mTextView: TextView = _
  private var mList: ListView = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val intent = getIntent

    setContentView(R.layout.main)
    mTextView = findViewById(R.id.textField).asInstanceOf[TextView]
    mList = findViewById(R.id.list).asInstanceOf[ListView]

    if (Intent.ACTION_VIEW equals intent.getAction) {
      // from click on search results
      Dictionary.getInstance ensureLoaded getResources
      val word = intent.getDataString
      val theWord = Dictionary.getInstance.getMatches(word).head
      launchWord(theWord)
      finish()
    } else if (Intent.ACTION_SEARCH equals intent.getAction) {
      val query = intent getStringExtra SearchManager.QUERY
      mTextView setText getString(R.string.search_results, query)
      val wordAdapter = new WordAdapter(Dictionary.getInstance getMatches query)
      mList setAdapter wordAdapter
      mList setOnItemClickListener wordAdapter
    }

    Log.d("dict", intent.toString)
    if (intent.getExtras != null) {
      Log.d("dict", intent.getExtras.keySet.toString)
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
        .setIcon(android.R.drawable.ic_search_category_default)
        .setAlphabeticShortcut(SearchManager.MENU_KEY)

    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    if (item.getItemId == MENU_SEARCH) {
      onSearchRequested()
      true
    } else {
      super.onOptionsItemSelected(item)
    }

  private def launchWord(theWord: Dictionary.Word) {
    val next = new Intent()
    next.setClass(this, classOf[WordActivity])
    next.putExtra("word", theWord.word)
    next.putExtra("definition", theWord.definition)
    startActivity(next)
  }

  class WordAdapter(words: List[Dictionary.Word])
  extends BaseAdapter with AdapterView.OnItemClickListener {

    private final val mInflater =
      SearchableDictionary.this.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    def getCount: Int = words.size

    def getItem(position: Int): AnyRef = position.asInstanceOf[AnyRef]

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val view: TwoLineListItem =
        if (convertView != null) convertView.asInstanceOf[TwoLineListItem]
        else createView(parent)
      bindView(view, words(position))
      view
    }

    private def createView(parent: ViewGroup): TwoLineListItem = {
      val item = mInflater.inflate(android.R.layout.simple_list_item_2,
                                   parent, false).asInstanceOf[TwoLineListItem]
      item.getText2.setSingleLine()
      item.getText2 setEllipsize TextUtils.TruncateAt.END
      item
    }

    private def bindView(view: TwoLineListItem, word: Dictionary.Word) {
      view.getText1 setText word.word
      view.getText2 setText word.definition
    }

    def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
      launchWord(words(position))
    }
  }
}

object SearchableDictionary {
  private final val MENU_SEARCH = 1
}
