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

package com.example.android.apis

import android.app.ListActivity
import android.content.Intent
import android.content.pm.{PackageManager, ResolveInfo}
import android.os.Bundle
import android.view.View
import android.widget.{ListView, SimpleAdapter}

import java.text.Collator
import java.util.{ArrayList, Collections, Comparator, HashMap => JHashMap,
                  List => JList, Map => JMap}

class ApiDemos extends ListActivity {
  import ApiDemos._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
        
    val intent = getIntent
    var path = intent getStringExtra "com.example.android.apis.Path"
        
    if (path == null) {
      path = ""
    }

    setListAdapter(
      new SimpleAdapter(this, getData(path),
        android.R.layout.simple_list_item_1,
        Array("title"), Array(android.R.id.text1)
      )
    )
    getListView setTextFilterEnabled true
  }

  protected def getData(prefix: String): JList[JMap[String, AnyRef]] = {
    val myData = new ArrayList[JMap[String, AnyRef]]

    val mainIntent = new Intent(Intent.ACTION_MAIN, null)
    mainIntent addCategory Intent.CATEGORY_SAMPLE_CODE

    val pm = getPackageManager
    val list = pm.queryIntentActivities(mainIntent, 0)

    if (null == list)
      return myData

    var prefixPath: Array[String] =
      if (prefix equals "") null
      else prefix split "/"
        
    val len = list.size()
        
    val entries = new JHashMap[String, Boolean]

    for (i <- 0 until len) {
      val info: ResolveInfo = list.get(i)
      val labelSeq = info.loadLabel(pm)
      val label =
        if (labelSeq != null) labelSeq.toString
        else info.activityInfo.name
            
      if (prefix.length() == 0 || label.startsWith(prefix)) {
        val labelPath = label split "/"

        val nextLabel =
          if (prefixPath == null) labelPath(0)
          else labelPath(prefixPath.length)

        val pathLength =
          if (prefixPath != null) prefixPath.length
          else 0
        if (pathLength == labelPath.length - 1) {
          addItem(myData, nextLabel, activityIntent(
               info.activityInfo.applicationInfo.packageName,
               info.activityInfo.name))
        } else if (! entries.containsKey(nextLabel)) {
          val text = if (prefix equals "") nextLabel else prefix + "/" + nextLabel
          addItem(myData, nextLabel, browseIntent(text))
          entries.put(nextLabel, true)
        }
      }
    }

    Collections.sort(myData, sDisplayNameComparator)
        
    myData
  }

  protected def activityIntent(pkg: String, componentName: String): Intent = {
    val result = new Intent
    result.setClassName(pkg, componentName)
    result
  }
    
  protected def browseIntent(path: String): Intent = {
    val result = new Intent
    result.setClass(this, classOf[ApiDemos])
    result.putExtra("com.example.android.apis.Path", path)
    result
  }

  protected def addItem(data: JList[JMap[String, AnyRef]],
                        name: String, intent: Intent) {
    val temp = new JHashMap[String, Object]
    temp.put("title", name)
    temp.put("intent", intent)
    data add temp
  }

  override protected def onListItemClick(l: ListView, v: View,
                                         position: Int, id: Long) {
    val map = l.getItemAtPosition(position).asInstanceOf[JMap[String, AnyRef]]

    val intent = map.get("intent").asInstanceOf[Intent]
    startActivity(intent)
  }

}

object ApiDemos {

  private final val sDisplayNameComparator = new Comparator[JMap[String, AnyRef]] {
    private final val collator = Collator.getInstance

    def compare(map1: JMap[String, AnyRef], map2: JMap[String, AnyRef]): Int = {
      collator.compare(map1 get "title", map2 get "title")
    }
  }

}
