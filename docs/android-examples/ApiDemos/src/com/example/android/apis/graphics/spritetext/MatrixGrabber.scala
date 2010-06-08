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

package com.example.android.apis.graphics.spritetext

import javax.microedition.khronos.opengles.GL10

class MatrixGrabber {
  private[spritetext] val mModelView = new Array[Float](16)
  private[spritetext] val mProjection = new Array[Float](16)

  /**
   * Record the current modelView and projection matrix state.
   * Has the side effect of setting the current matrix state to GL_MODELVIEW
   * @param gl
   */
  def getCurrentState(gl: GL10) {
    getCurrentProjection(gl)
    getCurrentModelView(gl)
  }

  /**
   * Record the current modelView matrix state. Has the side effect of
   * setting the current matrix state to GL_MODELVIEW
   * @param gl
   */
  def getCurrentModelView(gl: GL10) {
    getMatrix(gl, GL10.GL_MODELVIEW, mModelView)
  }

  /**
   * Record the current projection matrix state. Has the side effect of
   * setting the current matrix state to GL_PROJECTION
   * @param gl
   */
  def getCurrentProjection(gl: GL10) {
    getMatrix(gl, GL10.GL_PROJECTION, mProjection)
  }

  private def getMatrix(gl: GL10, mode: Int, mat: Array[Float]) {
    val gl2 = gl.asInstanceOf[MatrixTrackingGL]
    gl2.glMatrixMode(mode)
    gl2.getMatrix(mat, 0)
  }

}
