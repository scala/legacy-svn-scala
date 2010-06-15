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
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.{View, ViewGroup}
import android.widget.{BaseAdapter, GridView, ImageView}

import java.util.List

class LayoutAnimation5 extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    loadApps()

    setContentView(R.layout.layout_animation_5)
    val grid = findViewById(R.id.grid).asInstanceOf[GridView]
    grid setAdapter new AppsAdapter()
  }

  private var mApps: List[ResolveInfo] = _

  private def loadApps() {
    val mainIntent = new Intent(Intent.ACTION_MAIN, null)
    mainIntent addCategory Intent.CATEGORY_LAUNCHER

    mApps = getPackageManager.queryIntentActivities(mainIntent, 0)
  }

  class AppsAdapter extends BaseAdapter {
    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val i = new ImageView(LayoutAnimation5.this)

      val info = mApps.get(position % mApps.size)

      i setImageDrawable info.activityInfo.loadIcon(getPackageManager)
      i setScaleType ImageView.ScaleType.FIT_CENTER
      i setLayoutParams new ViewGroup.LayoutParams(36, 36)
      i
    }

    final def getCount: Int = math.min(32, mApps.size)

    final def getItem(position: Int): AnyRef =
      mApps.get(position % mApps.size).asInstanceOf[AnyRef]

    final def getItemId(position: Int): Long = position
  }

}
