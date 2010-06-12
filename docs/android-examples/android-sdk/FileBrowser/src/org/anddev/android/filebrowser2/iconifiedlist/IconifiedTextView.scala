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

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup.LayoutParams
import android.widget.{ImageView, LinearLayout, TextView}

/**
 *  Based on Steven Osborn's tutorial on anddev.org
 *  (http://www.anddev.org/iconified_textlist_-_the_making_of-t97.html)
 */
class IconifiedTextView(context: Context, aIconifiedText: IconifiedText)
extends LinearLayout(context) {

  /* First Icon and the Text to the right (horizontal),
   * not above and below (vertical) */
  setOrientation(LinearLayout.HORIZONTAL)

  private val mIcon = new ImageView(context)
  mIcon setImageDrawable aIconifiedText.getIcon
  // left, top, right, bottom
  mIcon.setPadding(0, 2, 5, 0) // 5px to the right

  /* At first, add the Icon to ourself
   * (! we are extending LinearLayout) */
  addView(mIcon, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT))
          
  private val mText = new TextView(context)
  mText setText aIconifiedText.getText
  /* Now the text (after the icon) */
  addView(mText, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT))

  def setText(words: String) {
    mText setText words
  }

  def setIcon(bullet: Drawable) {
    mIcon setImageDrawable bullet
  }
}
