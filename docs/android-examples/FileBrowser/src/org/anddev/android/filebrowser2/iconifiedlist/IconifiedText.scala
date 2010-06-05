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

import android.graphics.drawable.Drawable

/**
 *  Based on Steven Osborn's tutorial on anddev.org
 *  (http://www.anddev.org/iconified_textlist_-_the_making_of-t97.html)
 */
class IconifiedText(text: String, bullet: Drawable) extends Comparable[Any] {

  private var mText = text
  private var mIcon: Drawable = bullet
  private var mSelectable = true

  def isSelectable: Boolean = mSelectable

  def setSelectable(selectable: Boolean) { mSelectable = selectable }

  def getText: String = mText

  def setText(text: String) { mText = text }

  def setIcon(icon: Drawable) { mIcon = icon }

  def getIcon: Drawable = mIcon

  /** Make IconifiedText comparable by its name */
  override def compareTo(other: Any): Int =
    if (mText != null && other.isInstanceOf[IconifiedText])
      mText compareTo other.asInstanceOf[IconifiedText].getText
    else
      throw new IllegalArgumentException()
}
