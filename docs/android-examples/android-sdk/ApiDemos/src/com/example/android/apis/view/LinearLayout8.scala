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

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.view.{Gravity, Menu, MenuItem}
import android.widget.LinearLayout

/**
 * Demonstrates horizontal and vertical gravity
 */
class LinearLayout8 extends Activity {
  import LinearLayout8._  // companion object

  private var mLinearLayout: LinearLayout = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.linear_layout_8)
    mLinearLayout = findViewById(R.id.layout).asInstanceOf[LinearLayout]
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu.add(0, VERTICAL_ID, 0, R.string.linear_layout_8_vertical)
    menu.add(0, HORIZONTAL_ID, 0, R.string.linear_layout_8_horizontal)
    menu.add(0, TOP_ID, 0, R.string.linear_layout_8_top)
    menu.add(0, MIDDLE_ID, 0, R.string.linear_layout_8_middle)
    menu.add(0, BOTTOM_ID, 0, R.string.linear_layout_8_bottom)
    menu.add(0, LEFT_ID, 0, R.string.linear_layout_8_left)
    menu.add(0, CENTER_ID, 0, R.string.linear_layout_8_center)
    menu.add(0, RIGHT_ID, 0, R.string.linear_layout_8_right)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case VERTICAL_ID =>
        mLinearLayout setOrientation LinearLayout.VERTICAL
        true
      case HORIZONTAL_ID =>
        mLinearLayout setOrientation LinearLayout.HORIZONTAL
        true
      case TOP_ID =>
        mLinearLayout setVerticalGravity Gravity.TOP
        true
      case MIDDLE_ID =>
        mLinearLayout setVerticalGravity Gravity.CENTER_VERTICAL
        true
      case BOTTOM_ID =>
        mLinearLayout setVerticalGravity Gravity.BOTTOM
        true
      case LEFT_ID =>
        mLinearLayout setHorizontalGravity Gravity.LEFT
        true
      case CENTER_ID =>
        mLinearLayout setHorizontalGravity Gravity.CENTER_HORIZONTAL
        true
      case RIGHT_ID =>
        mLinearLayout setHorizontalGravity Gravity.RIGHT
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
}

object LinearLayout8 {

  // Menu item Ids
  final val VERTICAL_ID = Menu.FIRST
  final val HORIZONTAL_ID = Menu.FIRST + 1

  final val TOP_ID = Menu.FIRST + 2
  final val MIDDLE_ID = Menu.FIRST + 3
  final val BOTTOM_ID = Menu.FIRST + 4

  final val LEFT_ID = Menu.FIRST + 5
  final val CENTER_ID = Menu.FIRST + 6
  final val RIGHT_ID = Menu.FIRST + 7

}
