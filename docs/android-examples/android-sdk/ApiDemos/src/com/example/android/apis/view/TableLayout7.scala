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

import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.view.{Gravity, View}
import android.widget.{Button, TableLayout, TableRow, TextView}

class TableLayout7 extends Activity {
  private var mShortcutsCollapsed: Boolean = _
  private var mCheckmarksCollapsed: Boolean = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.table_layout_7)

    val table = findViewById(R.id.menu).asInstanceOf[TableLayout]
    var button = findViewById(R.id.toggle1).asInstanceOf[Button]
    button setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        mShortcutsCollapsed = !mShortcutsCollapsed
        table.setColumnCollapsed(2, mShortcutsCollapsed)
      }
    }
    button = findViewById(R.id.toggle2).asInstanceOf[Button]
    button setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        mCheckmarksCollapsed = !mCheckmarksCollapsed
        table.setColumnCollapsed(0, mCheckmarksCollapsed)
      }
    }

    mCheckmarksCollapsed = table.isColumnCollapsed(0)
    mShortcutsCollapsed = table.isColumnCollapsed(2)

    appendRow(table)
  }

  private def appendRow(table: TableLayout) {
    val row = new TableRow(this)

    val label = new TextView(this)
    label setText R.string.table_layout_7_quit
    label.setPadding(3, 3, 3, 3)

    val shortcut = new TextView(this)
    shortcut setText R.string.table_layout_7_ctrlq
    shortcut.setPadding(3, 3, 3, 3)
    shortcut.setGravity(Gravity.RIGHT | Gravity.TOP)

    row.addView(label, new TableRow.LayoutParams(1))
    row.addView(shortcut, new TableRow.LayoutParams())

    table.addView(row, new TableLayout.LayoutParams())
  }
}
