/*
 * Copyright (C) 2008 Google Inc.
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

package com.beust.android.translate

import android.app.AlertDialog
import android.content.Context
import android.webkit.WebView

/**
 * Display a simple about dialog.
 */
class AboutDialog protected (context: Context) extends AlertDialog(context) {

  setContentView(R.layout.about_dialog)
  setTitle(R.string.about_title)
  setCancelable(true)

  val webView = findViewById(R.id.webview).asInstanceOf[WebView]
  webView.loadData("Written by C&eacute;dric Beust (<a href=\"mailto:cedric@beust.com\">cedric@beust.com)</a>", "text/html", "utf-8")

}
