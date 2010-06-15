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

import java.io.OutputStream

import android.hardware.Camera
import android.hardware.Camera.PictureCallback

class ImageCaptureCallback(ostream: OutputStream) extends PictureCallback {

  def onPictureTaken(data: Array[Byte], camera: Camera) {
    try {
      ostream write data
      ostream.flush()
      ostream.close()
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }
}
