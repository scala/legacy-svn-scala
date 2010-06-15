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

import android.app.ExpandableListActivity
import android.os.Bundle
import android.widget.{ExpandableListAdapter, SimpleExpandableListAdapter}

import java.util.{ArrayList, HashMap, List, Map}

/**
 * Demonstrates expandable lists backed by a Simple Map-based adapter
 */
class ExpandableList3 extends ExpandableListActivity {
  import ExpandableList3._  // companion object

  private var mAdapter: ExpandableListAdapter = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val groupData = new ArrayList[Map[String, String]]()
    val childData = new ArrayList[List[Map[String, String]]]()
    for (i <- 0 until 20) {
      val curGroupMap = new HashMap[String, String]()
      groupData add curGroupMap
      curGroupMap.put(NAME, "Group " + i)
      curGroupMap.put(IS_EVEN, "This group is "+(if (i % 2 == 0) "even" else "odd"))
            
      val children = new ArrayList[Map[String, String]]
      for (j <- 0 until 15) {
        val curChildMap = new HashMap[String, String]
        children add curChildMap
        curChildMap.put(NAME, "Child " + j);
        curChildMap.put(IS_EVEN, "This child is "+(if (j % 2 == 0) "even" else "odd"))
      }
      childData add children
    }
        
    // Set up our adapter
    mAdapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                Array(NAME, IS_EVEN),
                Array(android.R.id.text1, android.R.id.text2),
                childData,
                android.R.layout.simple_expandable_list_item_2,
                Array(NAME, IS_EVEN),
                Array(android.R.id.text1, android.R.id.text2)
               )
    setListAdapter(mAdapter)
  }

}

object ExpandableList3 {
  private final val NAME = "NAME"
  private final val IS_EVEN = "IS_EVEN"
}
