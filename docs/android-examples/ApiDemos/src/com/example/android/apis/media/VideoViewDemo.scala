/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.apis.media

import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle
import android.widget.{MediaController, Toast, VideoView}

class VideoViewDemo extends Activity {

  /**
   * TODO: Set the path variable to a streaming video URL or a local media
   * file path.
   */
  private var path = ""
  private var mVideoView: VideoView = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.videoview)
    mVideoView = findViewById(R.id.surface_view).asInstanceOf[VideoView]

    if (path == "") {
      // Tell the user to provide a media file URL/path.
      Toast.makeText(VideoViewDemo.this,
                     "Please edit VideoViewDemo Activity, and set path"
                            + " variable to your media file URL/path",
                     Toast.LENGTH_LONG).show()

    } else {
      /*
       * Alternatively,for streaming media you can use
       * mVideoView.setVideoURI(Uri.parse(URLstring));
       */
      mVideoView setVideoPath path
      mVideoView setMediaController new MediaController(this)
      mVideoView.requestFocus()
    }
  }
}
