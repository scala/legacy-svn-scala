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
import android.view.{ContextMenu, Gravity, MenuItem, View, ViewGroup}
import android.view.ContextMenu.ContextMenuInfo
import android.view.ViewGroup.MarginLayoutParams
import android.widget.{AbsListView, BaseExpandableListAdapter,
                       ExpandableListAdapter, ExpandableListView,
                       TextView, Toast}
import android.widget.ExpandableListView.ExpandableListContextMenuInfo

import com.example.android.apis.R

/**
 * Demonstrates expandable lists using a custom {@link ExpandableListAdapter}
 * from {@link BaseExpandableListAdapter}.
 */
class ExpandableList1 extends ExpandableListActivity {

  private var mAdapter: ExpandableListAdapter = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Set up our adapter
    mAdapter = new MyExpandableListAdapter()
    setListAdapter(mAdapter)
    registerForContextMenu(getExpandableListView)
  }

  override def onCreateContextMenu(menu: ContextMenu, view: View,
                                   menuInfo: ContextMenuInfo) {
    menu setHeaderTitle "Sample menu"
    menu.add(0, 0, 0, R.string.expandable_list_sample_action)
  }
    
  override def onContextItemSelected(item: MenuItem): Boolean = {
    val info = item.getMenuInfo.asInstanceOf[ExpandableListContextMenuInfo]

    val title = info.targetView.asInstanceOf[TextView].getText.toString
        
    val typePos = ExpandableListView.getPackedPositionType(info.packedPosition)
    if (typePos == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
      val groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition)
      val childPos = ExpandableListView.getPackedPositionChild(info.packedPosition)
      Toast.makeText(this, title + ": Child " + childPos +
                     " clicked in group " + groupPos, Toast.LENGTH_SHORT).show()
      true
    } else if (typePos == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
      val groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition)
      Toast.makeText(this, title + ": Group " + groupPos +
                     " clicked", Toast.LENGTH_SHORT).show()
      true
    } else
      false
  }

  /**
   * A simple adapter which maintains an ArrayList of photo resource Ids. 
   * Each photo is displayed as an image. This adapter supports clearing the
   * list of photos and adding a new photo.
   *
   */
  class MyExpandableListAdapter extends BaseExpandableListAdapter {
    // Sample data set.  children[i] contains the children (String[]) for groups[i].
    private val groups =
      Array("People Names", "Dog Names", "Cat Names", "Fish Names")
    private val children = Array(
      Array("Arnold", "Barry", "Chuck", "David"),
      Array("Ace", "Bandit", "Cha-Cha", "Deuce"),
      Array("Fluffy", "Snuggles"),
      Array("Goldy", "Bubbles")
    )
        
    def getChild(groupPosition: Int, childPosition: Int): AnyRef =
      children(groupPosition)(childPosition).asInstanceOf[AnyRef]

    def getChildId(groupPosition: Int, childPosition: Int): Long =
      childPosition

    def getChildrenCount(groupPosition: Int): Int =
      children(groupPosition).length

    def getGenericView: TextView = {
      // Layout parameters for the ExpandableListView
      val lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64)

      val textView = new TextView(ExpandableList1.this)
      textView setLayoutParams lp
      // Center the text vertically
      textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT)
      // Set the text starting position
      textView.setPadding(36, 0, 0, 0)
      textView
    }
        
    def getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                     convertView: View, parent: ViewGroup): View = {
      val textView = getGenericView
      textView setText getChild(groupPosition, childPosition).toString
      textView
    }

    def getGroup(groupPosition: Int): AnyRef =
      groups(groupPosition).asInstanceOf[AnyRef]

    def getGroupCount: Int = groups.length

    def getGroupId(groupPosition: Int): Long = groupPosition

    def getGroupView(groupPosition: Int, isExpanded: Boolean,
                      convertView: View, parent: ViewGroup): View = {
      val textView = getGenericView
      textView setText getGroup(groupPosition).toString
      textView
    }

    def isChildSelectable(groupPosition: Int, childPosition: Int): Boolean =
      true

    def hasStableIds: Boolean = true

  }
}
