/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.appwidget

import android.appwidget.AppWidgetManager
import android.content.{BroadcastReceiver, Context, Intent}
import android.util.Log

import java.util.ArrayList

/**
 * A BroadcastReceiver that listens for updates for the ExampleAppWidgetProvider.
 * This BroadcastReceiver starts off disabled, and we only enable it when there
 * is a widget instance created, in order to only receive notifications when we
 * need them.
 */
class ExampleBroadcastReceiver extends BroadcastReceiver {

  override def onReceive(context: Context, intent: Intent) {
    Log.d("ExmampleBroadcastReceiver", "intent=" + intent)

    // For our example, we'll also update all of the widgets when the timezone
    // changes, or the user or network sets the time.
    val action = intent.getAction
    if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
        action.equals(Intent.ACTION_TIME_CHANGED)) {
      val gm = AppWidgetManager.getInstance(context)
      val appWidgetIds = new ArrayList[java.lang.Integer]
      val texts = new ArrayList[String]

      ExampleAppWidgetConfigure.loadAllTitlePrefs(context, appWidgetIds, texts)

      val N = appWidgetIds.size()
      for (i <- 0 until N) {
        ExampleAppWidgetProvider.updateAppWidget(context, gm, appWidgetIds.get(i).intValue, texts.get(i))
      }
    }
  }

}
