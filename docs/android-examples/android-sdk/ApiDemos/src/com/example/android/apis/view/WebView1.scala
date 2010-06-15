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
import android.os.Bundle
import android.webkit.WebView

import com.example.android.apis.R


/**
 * Sample creating 10 webviews.
 */
class WebView1 extends Activity {

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    setContentView(R.layout.webview_1)

    val mimeType = "text/html"
    val encoding = "utf-8"

    var wv = findViewById(R.id.wv1).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 1</a>", mimeType, encoding)

    wv = findViewById(R.id.wv2).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 2</a>", mimeType, encoding)

    wv = findViewById(R.id.wv3).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 3</a>", mimeType, encoding)

    wv = findViewById(R.id.wv4).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 4</a>", mimeType, encoding)

    wv = findViewById(R.id.wv5).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 5</a>", mimeType, encoding)

    wv = findViewById(R.id.wv6).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 6</a>", mimeType, encoding)

    wv = findViewById(R.id.wv7).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 7</a>", mimeType, encoding)
        
    wv = findViewById(R.id.wv8).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 8</a>", mimeType, encoding)

    wv = findViewById(R.id.wv9).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 9</a>", mimeType, encoding)

    wv = findViewById(R.id.wv10).asInstanceOf[WebView]
    wv.loadData("<a href='x'>Hello World! - 10</a>", mimeType, encoding)
  }
}
