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

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.{View, ViewGroup}
import android.widget.{AbsListView, BaseAdapter, GridView, ImageView}

import java.util.List

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R

class Grid1 extends Activity {

  private var mGrid: GridView = _
  private var mApps: List[ResolveInfo] = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    loadApps() // do this in onresume?

    setContentView(R.layout.grid_1)
    mGrid = findViewById(R.id.myGrid).asInstanceOf[GridView]
    mGrid setAdapter new AppsAdapter()
  }

  private def loadApps() {
    val mainIntent = new Intent(Intent.ACTION_MAIN, null)
    mainIntent addCategory Intent.CATEGORY_LAUNCHER

    mApps = getPackageManager.queryIntentActivities(mainIntent, 0)
  }

  class AppsAdapter extends BaseAdapter {

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val imageView = if (convertView == null) {
        val i = new ImageView(Grid1.this)
        i setScaleType ImageView.ScaleType.FIT_CENTER
        i setLayoutParams new AbsListView.LayoutParams(50, 50)
        i
      } else {
        convertView.asInstanceOf[ImageView]
      }

      val info = mApps get position
      imageView setImageDrawable info.activityInfo.loadIcon(getPackageManager)
      imageView
    }

    final def getCount: Int = mApps.size

    final def getItem(position: Int): AnyRef = 
      mApps.get(position).asInstanceOf[AnyRef]

    final def getItemId(position: Int): Long = position
  }

}
