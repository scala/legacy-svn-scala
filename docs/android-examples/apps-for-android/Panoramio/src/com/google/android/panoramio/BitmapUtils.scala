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

package com.google.android.panoramio

import android.graphics.{Bitmap, BitmapFactory}
import android.util.Log

import java.io.{BufferedInputStream, BufferedOutputStream, ByteArrayOutputStream,
                Closeable, IOException, InputStream, OutputStream}
import java.net.URL

/**
 *  Utilities for loading a bitmap from a URL
 *
 */
object BitmapUtils {

  private final val TAG = "Panoramio"

  private final val IO_BUFFER_SIZE = 4 * 1024

  /**
   * Loads a bitmap from the specified url. This can take a while, so it should
   * not be called from the UI thread.
   * 
   * @param url The location of the bitmap asset
   * 
   * @return The bitmap, or null if it could not be loaded
   */
  def loadBitmap(url: String): Bitmap = {
    var bitmap: Bitmap = null
    var in: InputStream = null
    var out: BufferedOutputStream = null

    try {
      in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE)

      val dataStream = new ByteArrayOutputStream()
      out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE)
      copy(in, out)
      out.flush()

      val data = dataStream.toByteArray
      bitmap = BitmapFactory.decodeByteArray(data, 0, data.length)
    } catch {
      case e: IOException =>
        Log.e(TAG, "Could not load Bitmap from: " + url)
    } finally {
      closeStream(in)
      closeStream(out)
    }

    bitmap
  }

  /**
   * Closes the specified stream.
   * 
   * @param stream The stream to close.
   */
  private def closeStream(stream: Closeable) {
    if (stream != null) {
      try {
        stream.close();
      } catch {
        case e: IOException =>
          Log.e(TAG, "Could not close stream", e)
      }
    }
  }

  /**
   * Copy the content of the input stream into the output stream, using a
   * temporary byte array buffer whose size is defined by
   * {@link #IO_BUFFER_SIZE}.
   * 
   * @param in The input stream to copy from.
   * @param out The output stream to copy to.
   * @throws IOException If any error occurs during the copy.
   */
  @throws(classOf[IOException])
  private def copy(in: InputStream, out: OutputStream) {
    val b = new Array[Byte](IO_BUFFER_SIZE)
    var data = in read b
    while (data != -1) {
      out.write(b, 0, data)
      data = in read b
    }
  }

}
