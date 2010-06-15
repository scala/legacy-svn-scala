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

package com.msi.manning.chapter10.simplecamera

import java.text.SimpleDateFormat
import java.util.Date

import android.app.Activity
import android.content.{ContentValues, Intent}
import android.graphics.PixelFormat
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.{ImageColumns, Media}
import android.provider.MediaStore.MediaColumns
import android.util.Log
import android.view.{KeyEvent, Menu, MenuItem, SurfaceHolder, SurfaceView}

class SimpleCamera extends Activity with SurfaceHolder.Callback {

  private var camera: Camera = _
  private var isPreviewRunning = false
  private val timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS")
    
  private var surfaceView: SurfaceView = _
  private var surfaceHolder: SurfaceHolder = _
  private val targetResource = Media.EXTERNAL_CONTENT_URI

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Log.e(getClass.getSimpleName, "onCreate")
    getWindow setFormat PixelFormat.TRANSLUCENT
    setContentView(R.layout.main)
    surfaceView = findViewById(R.id.surface).asInstanceOf[SurfaceView]
    surfaceHolder = surfaceView.getHolder
    surfaceHolder addCallback this
    surfaceHolder setType SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val item = menu.add(0, 0, 0, "View Pictures")
    item setOnMenuItemClickListener new MenuItem.OnMenuItemClickListener() {
      def onMenuItemClick(item: MenuItem): Boolean = {
        val intent = new Intent(Intent.ACTION_VIEW, targetResource)
        startActivity(intent)
        true
      }
    }
    true
  }

  override protected def onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
  }

  private val mPictureCallbackRaw = new Camera.PictureCallback() {
    def onPictureTaken(data: Array[Byte], c: Camera) {
      camera.startPreview()
    }
  }

  private val mShutterCallback = new Camera.ShutterCallback() {
    def onShutter() {
    }
  } 

  override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
    var camDemo: ImageCaptureCallback = null
    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
      try {
        val filename = timeStampFormat format new Date()
        val values = new ContentValues()
        values.put(MediaColumns.TITLE, filename)
        values.put(ImageColumns.DESCRIPTION, "Image from Android Emulator")
        val uri = getContentResolver.insert(Media.EXTERNAL_CONTENT_URI, values)
        camDemo =
          new ImageCaptureCallback(getContentResolver openOutputStream uri)
      } catch {
        case e: Exception =>
      }
    }
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      return super.onKeyDown(keyCode, event)
    }
 
    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
      camera.takePicture(mShutterCallback, mPictureCallbackRaw, camDemo)
      return true
    }

    false
  }

  override protected def onResume() {
    Log.e(getClass.getSimpleName, "onResume")
    super.onResume()
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
  }

  override protected def onStop() {
    super.onStop()
  }

  def surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
    if (isPreviewRunning) {
      camera.stopPreview()
    }
    val p = camera.getParameters()
    p.setPreviewSize(w, h)
    camera setParameters p
    camera setPreviewDisplay holder
    camera.startPreview()
    isPreviewRunning = true
  }

  def surfaceCreated(holder: SurfaceHolder) {
    camera = Camera.open()
  }

  def surfaceDestroyed(holder: SurfaceHolder) {
    camera.stopPreview()
    isPreviewRunning = false
    camera.release()
  }
}
