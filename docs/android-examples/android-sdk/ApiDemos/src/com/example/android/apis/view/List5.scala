/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.apis.view

import android.app.ListActivity
import android.content.Context
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, TextView}

/**
 * A list view example with separators.
 */
class List5 extends ListActivity {
  import List5._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setListAdapter(new MyListAdapter(this))
  }

  private class MyListAdapter(context: Context) extends BaseAdapter {

    def getCount: Int = mStrings.length

    override def areAllItemsEnabled: Boolean = false

    override def isEnabled(position: Int): Boolean =
      !mStrings(position).startsWith("-")

    def getItem(position: Int): AnyRef = position.asInstanceOf[AnyRef]

    def getItemId(position: Int): Long = position

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val tv = if (convertView == null) {
        LayoutInflater.from(context)
                      .inflate(android.R.layout.simple_expandable_list_item_1,
                               parent, false).asInstanceOf[TextView]
      } else {
        convertView.asInstanceOf[TextView]
      }
      tv setText mStrings(position)
      tv
    }

  }

}

object List5 {

  private val mStrings = Array(
    "----------",
    "----------",
    "Abbaye de Belloc",
    "Abbaye du Mont des Cats",
    "Abertam",
    "----------",
    "Abondance",
    "----------",
    "Ackawi",
    "Acorn",
    "Adelost",
    "Affidelice au Chablis",
    "Afuega'l Pitu",
    "Airag",
    "----------",
    "Airedale",
    "Aisy Cendre",
    "----------",
    "Allgauer Emmentaler",
    "Alverca",
    "Ambert",
    "American Cheese",
    "Ami du Chambertin",
    "----------",
    "----------",
    "Anejo Enchilado",
    "Anneau du Vic-Bilh",
    "Anthoriro",
    "----------",
    "----------")

}
