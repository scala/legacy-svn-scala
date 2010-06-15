/*
 * Copyright (C) 2009 Manning Publications Co.
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

package com.msi.manning.chapter5.widget

import android.app.Activity
import android.content.{ContentValues, Context, Intent}
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, LinearLayout, TextView}

import scala.collection.mutable.ListBuffer

class WidgetExplorer extends Activity {

  private var addName: EditText = _
  private var addType: EditText = _
  private var addCategory: EditText = _
  private var editName: EditText = _
  private var editType: EditText = _
  private var editCategory: EditText = _
  private var addButton: Button = _
  private var editButton: Button = _

  private var itemId: Long = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.provider_explorer)

    addName = findView(R.id.add_name)
    addType = findView(R.id.add_type)
    addCategory = findView(R.id.add_category)
    editName = findView(R.id.edit_name)
    editType = findView(R.id.edit_type)
    editCategory = findView(R.id.edit_category)
    addButton = findView(R.id.add_button)
    addButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        add();
      }
    }
    editButton = findView(R.id.edit_button)
    editButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        edit()
      }
    }
  }

  override def onStart() {
    super.onStart()

    val widgets: List[WidgetBean] = getWidgets
    val params = new LinearLayout.LayoutParams(200,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    if (widgets != null) {
      val editLayout: LinearLayout = findView(R.id.edit_buttons_layout)
      val deleteLayout: LinearLayout = findView(R.id.delete_buttons_layout)
      params.setMargins(10, 0, 0, 0)
      for (w <- widgets) {
        val widgetEditButton = new WidgetButton(this, w)
        widgetEditButton setText w.toString
        editLayout.addView(widgetEditButton, params)
        widgetEditButton setOnClickListener new OnClickListener() {
          def onClick(v: View) {
            val view = v.asInstanceOf[WidgetButton]
            editName setText view.widget.name
            editType setText view.widget.typ
            editCategory setText view.widget.category
            itemId = view.widget.id
          }
        }

        val widgetDeleteButton = new WidgetButton(this, w)
        widgetDeleteButton.setText("Delete " + w.name)
        deleteLayout.addView(widgetDeleteButton, params)
        widgetDeleteButton setOnClickListener new OnClickListener() {
          def onClick(v: View) {
            val view = v.asInstanceOf[WidgetButton]
            itemId = view.widget.id
            delete()
          }
        }
      }
    } else {
      val layout: LinearLayout = findView(R.id.edit_buttons_layout)
      val empty = new TextView(this)
      empty.setText("No current widgets");
      layout.addView(empty, params);
    }
  }

  override def onPause() {
    super.onPause()
  }

  //
  // begin resolver methods
  //

  private def add() {
    val values = new ContentValues()
    values.put(Widget.NAME, addName.getText.toString)
    values.put(Widget.TYPE, addType.getText.toString)
    values.put(Widget.CATEGORY, addCategory.getText.toString)
    values.put(Widget.CREATED, getCurrentTimeMillis)
    getContentResolver().insert(Widget.CONTENT_URI, values)

    startActivity(new Intent(this, classOf[WidgetExplorer]))
  }

  private def delete() {
    var uri = Widget.CONTENT_URI
    uri = uri.buildUpon().appendPath(itemId.toString).build()
    getContentResolver.delete(uri, null, null)

    startActivity(new Intent(this, classOf[WidgetExplorer]))
  }

  private def edit() {
    var uri = Widget.CONTENT_URI
    uri = uri.buildUpon().appendPath(itemId.toString).build()
    val values = new ContentValues()
    values.put(Widget.NAME, editName.getText.toString)
    values.put(Widget.TYPE, editType.getText.toString)
    values.put(Widget.CATEGORY, editCategory.getText().toString)
    values.put(Widget.UPDATED, getCurrentTimeMillis)
    getContentResolver.update(uri, values, null, null)

    startActivity(new Intent(this, classOf[WidgetExplorer]))
  }

  private def getWidgets: List[WidgetBean] = {
    val results = ListBuffer[WidgetBean]()
    var id = 0L
    var name: String = null
    var typ: String = null
    var category: String = null
    var created = 0L
    var updated = 0L
    val uri = Widget.CONTENT_URI
    val cur: Cursor = managedQuery(uri, null, null, null, null)
    if (cur != null) {
      while (cur.moveToNext()) {
        id = cur.getLong(cur getColumnIndex BaseColumns._ID)
        name = cur.getString(cur getColumnIndex Widget.NAME)
        typ = cur.getString(cur getColumnIndex Widget.TYPE)
        category = cur.getString(cur getColumnIndex Widget.CATEGORY)
        created = cur.getLong(cur getColumnIndex Widget.CREATED)
        updated = cur.getLong(cur getColumnIndex Widget.UPDATED)
        results append new WidgetBean(id, name, typ, category, created, updated)
      }
    }
    results.toList
  }

  //
  // addtl classes
  //

  private class WidgetBean(val id: Long, val name: String,
                           val typ: String, val category: String,
                           val create: Long, val updated: Long) {
    override def toString: String =
      name + "\n" + typ + " " + category
  }

  private class WidgetButton(context: Context, val widget: WidgetBean)
  extends Button(context)

  //
  // helper methods
  //

  @inline
  private final def findView[V <: View](id: Int) =
    findViewById(id).asInstanceOf[V]

  @inline
  private final def getCurrentTimeMillis =
    java.lang.Double.valueOf(System.currentTimeMillis)

}
