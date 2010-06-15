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
import android.content.{Context, Intent}
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText}

import java.io.{FileNotFoundException, FileOutputStream, IOException}

class CreateFile extends Activity {
  import CreateFile._  // companion object

  private var createInput: EditText = _
  private var createButton: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.create_file)

    createInput = findViewById(R.id.create_input).asInstanceOf[EditText]
    createButton = findViewById(R.id.create_button).asInstanceOf[Button]

    createButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        var fos: FileOutputStream = null
        try {
          fos = openFileOutput("filename.txt", Context.MODE_PRIVATE)
          fos.write(createInput.getText.toString.getBytes)
        } catch {
          case e: FileNotFoundException =>
            Log.e(CreateFile.LOGTAG, e.getLocalizedMessage)
          case e: IOException =>
            Log.e(CreateFile.LOGTAG, e.getLocalizedMessage)
        } finally {
          if (fos != null) {
            try {
              fos.flush()
              fos.close()
            } catch {
              case e: IOException => // swallow
            }
          }
        }
        startActivity(new Intent(CreateFile.this, classOf[ReadFile]))
      }
    }
  }
}

object CreateFile {
  private final val LOGTAG = "FileStorage"
}
