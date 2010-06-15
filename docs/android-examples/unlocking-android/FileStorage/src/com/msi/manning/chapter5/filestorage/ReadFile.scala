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

package com.msi.manning.chapter5.filestorage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

import java.io.{FileInputStream, IOException}

class ReadFile extends Activity {
  import ReadFile._  // companion object

  private var readOutput: TextView = _
  private var gotoReadResource: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.read_file)

    readOutput = findViewById(R.id.read_output).asInstanceOf[TextView]

    var fis: FileInputStream = null
    try {
      fis = openFileInput("filename.txt")
      val reader = new Array[Byte](fis.available)
      while (fis.read(reader) != -1) {}
      readOutput setText new String(reader)
    } catch {
      case e: IOException =>
        Log.e(ReadFile.LOGTAG, e.getMessage, e)
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch {
          case e: IOException => // swallow
        }
      }
    }

    gotoReadResource = findViewById(R.id.read_button).asInstanceOf[Button]
    gotoReadResource setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(ReadFile.this, classOf[ReadRawResourceFile]))
      }
    }

  }
}

object ReadFile {
  private final val LOGTAG = "FileStorage"
}
