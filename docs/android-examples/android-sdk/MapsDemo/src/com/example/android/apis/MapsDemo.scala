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

package com.example.android.apis

import android.app.ListActivity
import android.content.Intent
import android.content.pm.{PackageManager, ResolveInfo}
import android.os.Bundle
import android.view.View
import android.widget.{ListView, SimpleAdapter}

import java.text.Collator
import java.util.{ArrayList, Collections, Comparator, HashMap, List, Map}

class MapsDemo extends ListActivity {
  import MapsDemo._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setListAdapter(new SimpleAdapter(this, getData,
                android.R.layout.simple_list_item_1, Array("title"),
                Array(android.R.id.text1)))
    getListView setTextFilterEnabled true
  }

  protected def getData: List[Map[String, AnyRef]] = {
    val myData = new ArrayList[Map[String, AnyRef]]

    val mainIntent = new Intent(Intent.ACTION_MAIN, null)
    mainIntent addCategory Intent.CATEGORY_SAMPLE_CODE

    val pm = getPackageManager
    val list = pm.queryIntentActivities(mainIntent, 0)

    if (null == list)
     return myData

    var prefixPath: Array[String] = null

    var len = list.size

    //Map<String, Boolean> entries = new HashMap<String, Boolean>();

    for (i <- 0 until len) {
      val info: ResolveInfo = list.get(i)

      val labelSeq = info loadLabel pm

      if ("com.example.android.google.apis" equals info.activityInfo.applicationInfo.packageName) {
        addItem(myData, labelSeq.toString, activityIntent(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name))
      }
    }

    Collections.sort(myData, sDisplayNameComparator)
        
    myData
  }

  protected def activityIntent(pkg: String, componentName: String): Intent = {
    val result = new Intent()
    result.setClassName(pkg, componentName)
    result
  }

  protected def addItem(data: List[Map[String, AnyRef]], name: String, intent: Intent) {
    val temp = new HashMap[String, AnyRef]()
    temp.put("title", name)
    temp.put("intent", intent)
    data add temp
  }

  override protected def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val map = l.getItemAtPosition(position).asInstanceOf[Map[String, AnyRef]]

    val intent = map.get("intent").asInstanceOf[Intent]
    startActivity(intent)
  }
}

object MapsDemo {

  private final val sDisplayNameComparator = new Comparator[Map[String, AnyRef]]() {
    private final val collator = Collator.getInstance

    def compare(map1: Map[String, AnyRef], map2: Map[String, AnyRef]): Int =
      collator.compare(map1 get "title", map2 get "title")

  }
}
