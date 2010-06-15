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
import android.os.Bundle
import android.util.Log
import android.widget.TextView

import java.io.{File, FileInputStream, FileNotFoundException,
                FileOutputStream, IOException}

class ReadWriteSDCardFile extends Activity {
  import ReadWriteSDCardFile._  // companion object

  private var readOutput: TextView = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.read_write_sdcard_file)

    readOutput = findViewById(R.id.readwritesd_output).asInstanceOf[TextView]

    val fileName = "testfile-" + System.currentTimeMillis + ".txt"

    // create structure /sdcard/unlocking_android and then WRITE
    val sdDir = new File("/sdcard/")
    if (sdDir.exists && sdDir.canWrite) {
      val uadDir = new File(sdDir.getAbsolutePath + "/unlocking_android")
      uadDir.mkdir()
      if (uadDir.exists && uadDir.canWrite) {
        val file = new File(uadDir.getAbsolutePath + "/" + fileName)
        try {
          file.createNewFile()
        } catch {
          case e: IOException =>
            Log.e(ReadWriteSDCardFile.LOGTAG, "error creating file", e)
        }

        // now that we have the structure we want, write to the file
        if (file.exists && file.canWrite) {
          var fos: FileOutputStream = null
          try {
            fos = new FileOutputStream(file)
            fos.write("I fear you speak upon the rack, where men enforced do speak anything.".getBytes)
          } catch {
            case e: FileNotFoundException =>
              Log.e(ReadWriteSDCardFile.LOGTAG, "ERROR", e)
            case e: IOException =>
              Log.e(ReadWriteSDCardFile.LOGTAG, "ERROR", e)
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
        } else {
          Log.e(ReadWriteSDCardFile.LOGTAG, "error writing to file")
        }

      } else {
        Log.e(ReadWriteSDCardFile.LOGTAG, "ERROR, unable to write to /sdcard/unlocking_android")
      }
    } else {
       Log.e(ReadWriteSDCardFile.LOGTAG,
             "ERROR, /sdcard path not available " +
             "(did you create an SD image with the mksdcard tool, " +
             "and start emulator with -sdcard <path_to_file> option?")
    }

    // READ
    val rFile = new File("/sdcard/unlocking_android/" + fileName)
    if (rFile.exists && rFile.canRead) {
      var fis: FileInputStream = null
      try {
        fis = new FileInputStream(rFile)
        val reader = new Array[Byte](fis.available)
        while (fis.read(reader) != -1) {}
        readOutput setText new String(reader)
      } catch {
        case e: IOException =>
          Log.e(ReadWriteSDCardFile.LOGTAG, e.getMessage, e)
      } finally {
        if (fis != null) {
          try {
            fis.close()
          } catch {
            case e: IOException => // swallow
          }
        }
      }
    } else {
      readOutput.setText("Unable to read/write sdcard file, see logcat output")
    }
  }
}

object ReadWriteSDCardFile {
  private final val LOGTAG = "FileStorage"
}
