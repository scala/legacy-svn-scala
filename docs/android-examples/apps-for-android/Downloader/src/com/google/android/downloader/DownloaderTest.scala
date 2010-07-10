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

package com.google.android.downloader

import android.app.Activity
import android.os.Bundle
import android.view.{Menu, MenuItem}

class DownloaderTest extends Activity {
  import DownloaderTest._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    if (! DownloaderActivity.ensureDownloaded(this,
            getString(R.string.app_name), FILE_CONFIG_URL,
            CONFIG_VERSION, DATA_PATH, USER_AGENT)) {
      return
    }
    setContentView(R.layout.main)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    if (item.getItemId == R.id.menu_main_download_again) {
      downloadAgain()
      true
    } else {
      super.onOptionsItemSelected(item)
    }

  private def downloadAgain() {
    DownloaderActivity.deleteData(DATA_PATH)
    startActivity(getIntent)
    finish()
  }

}

object DownloaderTest {

  /**
   * Fill this in with your own web server.
   */
  private final val FILE_CONFIG_URL =
        "http://lamp.epfl.ch/~michelou/android/download.config"
        //"http://example.com/download.config"
  private final val CONFIG_VERSION = "1.0"
  private final val DATA_PATH = "/sdcard/data/downloadTest"
  private final val USER_AGENT = "MyApp Downloader"
}
