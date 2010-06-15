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

package com.msi.manning.chapter10.soundrecordingdemo

import android.app.Activity
import android.content.{ContentResolver, ContentValues, Intent}
import android.media.MediaRecorder
import android.net.Uri
import android.os.{Bundle, Environment}
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button

import java.io.{File, IOException}

class SoundRecordingDemo extends Activity  {
  import SoundRecordingDemo._  // companion object

  private var mRecorder: MediaRecorder = _
  private var mSampleFile: File = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    mRecorder = new MediaRecorder()

    val startButton = findViewById(R.id.startrecording).asInstanceOf[Button]
    val stopButton = findViewById(R.id.stoprecording).asInstanceOf[Button]

    startButton setOnClickListener new View.OnClickListener(){
      def onClick(v: View) {
        startRecording()
      }
    }

    stopButton setOnClickListener new View.OnClickListener(){
      def onClick(v: View) {
        stopRecording()
        addToDB()
      }
    }
  }

  // Since method put in Java class ContentValues has overloaded definitions:
  //   (java.lang.String,java.lang.Double)Unit
  //   (java.lang.String,java.lang.Float)Unit
  // we have to explicitly resolve the ambiguity.
  protected def addToDB() {
    val values = new ContentValues(3)
    val current = System.currentTimeMillis

    values.put(MediaStore.MediaColumns.TITLE, "test_audio")
    values.put(MediaStore.MediaColumns.DATE_ADDED, Predef.float2Float(current / 1000))
    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
    values.put(MediaStore.MediaColumns.DATA, mSampleFile.getAbsolutePath)
    val contentResolver = getContentResolver()
      
    val base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val newUri = contentResolver.insert(base, values)

    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri))
  }

  // We need a MediaRecorder instance in order to access Java constants
  // declared inside its scope (e.g. in final class AudioSource).
  object MediaRecorder extends MediaRecorder

  protected def startRecording() {
    mRecorder = new MediaRecorder()
    mRecorder setAudioSource MediaRecorder.AudioSource.MIC
    mRecorder setOutputFormat MediaRecorder.OutputFormat.THREE_GPP
    mRecorder setAudioEncoder MediaRecorder.AudioEncoder.AMR_NB
    mRecorder setOutputFile mSampleFile.getAbsolutePath
    mRecorder.prepare()
    mRecorder.start()

    if (mSampleFile == null) {
      val sampleDir = Environment.getExternalStorageDirectory
	        
      try {
        mSampleFile = File.createTempFile(SAMPLE_PREFIX, SAMPLE_EXTENSION, sampleDir)
      } catch {
        case e: IOException =>
	  Log.e(TAG,"sdcard access error")
      }
    }
  }

  protected def stopRecording() {
    mRecorder.stop()
    mRecorder.release()
  }
}

object SoundRecordingDemo {
  private final val SAMPLE_PREFIX = "recording"
  private final val SAMPLE_EXTENSION = ".mp3"
  private final val TAG = "SoundRecordingDemo"
}
