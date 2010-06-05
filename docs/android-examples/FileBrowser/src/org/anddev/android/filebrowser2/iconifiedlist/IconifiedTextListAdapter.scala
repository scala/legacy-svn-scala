/*
 * Copyright 2007 Steven Osborn
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
package org.anddev.android.filebrowser2.iconifiedlist

import scala.collection.mutable.ArrayBuffer

import android.content.Context
import android.widget.BaseAdapter
import android.view.{View, ViewGroup}

/**
 *  Based on Steven Osborn's tutorial on anddev.org
 *  (http://www.anddev.org/iconified_textlist_-_the_making_of-t97.html)
 */
class IconifiedTextListAdapter(context: Context) extends BaseAdapter {

  /** Remember our context so we can use it when constructing views. */
  private var mContext: Context = context

  private var mItems = new ArrayBuffer[IconifiedText]

  def addItem(it: IconifiedText) { mItems += it }

  def setListItems(lit: ArrayBuffer[IconifiedText]) { mItems = lit }

  /** @return The number of items in the */
  def getCount: Int = mItems.size

  def getItem(position: Int): AnyRef = mItems(position)

  //override def areAllItemsSelectable(): Boolean = false

  override def /*isSelectable*/isEnabled(position: Int): Boolean =
    try {
      mItems(position).isSelectable
    } catch {
      case _: IndexOutOfBoundsException =>
        super.isEnabled(position) // .isSelectable(position)
    }

  /** Use the array index as a unique id. */
  def getItemId(position: Int): Long = position

  /** @param convertView The old view to overwrite, if one is passed
   * @returns a IconifiedTextView that holds wraps around an IconifiedText */
  def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val item = mItems(position)
    if (convertView == null)
      new IconifiedTextView(mContext, item)
    else { // Reuse/Overwrite the View passed
           // We are assuming(!) that it is castable!
      val btv = convertView.asInstanceOf[IconifiedTextView]
      btv setText item.getText
      btv setIcon item.getIcon
      btv
    }
  }
}
