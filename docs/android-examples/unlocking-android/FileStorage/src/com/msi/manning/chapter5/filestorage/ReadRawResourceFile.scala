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
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

import java.io.{IOException, InputStream}

class ReadRawResourceFile extends Activity {
  import ReadRawResourceFile._  // companion object

  private var readOutput: TextView = _
  private var gotoReadXMLResource: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.read_rawresource_file)

    readOutput = findViewById(R.id.readrawres_output).asInstanceOf[TextView]

    val resources = getResources
    var is: InputStream = null
    try {
      is = resources openRawResource R.raw.people
      val reader = new Array[Byte](is.available)
      while (is.read(reader) != -1) {}
      readOutput setText new String(reader)
    } catch {
      case e: IOException =>
        Log.e(ReadRawResourceFile.LOGTAG, e.getMessage, e)
    } finally {
      if (is != null) {
        try {
          is.close()
        } catch {
          case e: IOException => // swallow
        }
      }
    }

    gotoReadXMLResource = findViewById(R.id.readrawres_button).asInstanceOf[Button]
    gotoReadXMLResource setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(ReadRawResourceFile.this, classOf[ReadXMLResourceFile]))
      }
    }

  }
}

object ReadRawResourceFile {
  private final val LOGTAG = "FileStorage"
}
