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

package com.example.android.apis.graphics

import java.nio.{ ByteBuffer, ByteOrder, IntBuffer}

import javax.microedition.khronos.opengles.GL10

/**
 * A vertex shaded cube.
 */
class Cube {
  val one = 0x10000
  val vertices = Array(
    -one, -one, -one,
     one, -one, -one,
     one,  one, -one,
    -one,  one, -one,
    -one, -one,  one,
     one, -one,  one,
     one,  one,  one,
    -one,  one,  one
  )

  val colors = Array(
    0,    0,    0,    one,
    one,  0,    0,    one,
    one,  one,  0,    one,
    0,    one,  0,    one,
    0,    0,    one,  one,
    one,  0,    one,  one,
    one,  one,  one,  one,
    0,    one,  one,  one
  )

  val indices = Array[Byte](
    0, 4, 5,    0, 5, 1,
    1, 5, 6,    1, 6, 2,
    2, 6, 7,    2, 7, 3,
    3, 7, 4,    3, 4, 0,
    4, 7, 6,    4, 6, 5,
    3, 0, 1,    3, 1, 2
  )

  // Buffers to be passed to gl*Pointer() functions
  // must be direct, i.e., they must be placed on the
  // native heap where the garbage collector cannot
  // move them.
  //
  // Buffers with multi-byte datatypes (e.g., short, int, float)
  // must have their byte order set to native order

  private val mVertexBuffer: IntBuffer = {
    val vbb = ByteBuffer.allocateDirect(vertices.length*4)
    vbb order ByteOrder.nativeOrder()
    val buf = vbb.asIntBuffer()
    buf put vertices
    buf position 0
    buf
  }

  private val mColorBuffer: IntBuffer = {
    val cbb = ByteBuffer.allocateDirect(colors.length*4)
    cbb order ByteOrder.nativeOrder()
    val buf = cbb.asIntBuffer()
    buf put colors
    buf position 0
    buf
  }

  private val mIndexBuffer: ByteBuffer = {
    val buf = ByteBuffer.allocateDirect(indices.length)
    buf put indices
    buf position 0
    buf
  }

  def draw(gl: GL10) {
    gl glFrontFace GL10.GL_CW
    gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer)
    gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer)
    gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer)
  }

}
