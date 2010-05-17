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
import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.view.View.OnClickListener
import _root_.android.widget.Button

object MediaPlayerDemo {
  private final val MEDIA = "media"

  private final val LOCAL_AUDIO     = 1
  private final val STREAM_AUDIO    = 2
  private final val RESOURCES_AUDIO = 3
  private final val LOCAL_VIDEO     = 4
  private final val STREAM_VIDEO    = 5
  private final val RESOURCES_VIDEO = 6
}

class MediaPlayerDemo extends Activity {
  import MediaPlayerDemo._  // companion object

  private var mlocalvideo: Button = _
  private var mresourcesvideo: Button = _
  private var mstreamvideo: Button = _
  private var mlocalaudio: Button = _
  private var mresourcesaudio: Button = _
  private var mstreamaudio: Button = _

  override protected def onCreate(icicle: Bundle) {
    // TODO Auto-generated method stub
    super.onCreate(icicle)
    setContentView(R.layout.mediaplayer_1)
    mlocalaudio = findViewById(R.id.localaudio).asInstanceOf[Button]
    mlocalaudio setOnClickListener mLocalAudioListener
    mresourcesaudio = findViewById(R.id.resourcesaudio).asInstanceOf[Button]
    mresourcesaudio setOnClickListener mResourcesAudioListener

    mlocalvideo = findViewById(R.id.localvideo).asInstanceOf[Button]
    mlocalvideo setOnClickListener mLocalVideoListener
    mstreamvideo = findViewById(R.id.streamvideo).asInstanceOf[Button]
    mstreamvideo setOnClickListener mStreamVideoListener
  }

  private val mLocalAudioListener = new OnClickListener {
    def onClick(v: View) {
      val intent = new Intent(MediaPlayerDemo.this.getApplication,
                              classOf[MediaPlayerDemo_Audio])
      intent.putExtra(MEDIA, LOCAL_AUDIO)
      startActivity(intent)
    }
  }

  private val mResourcesAudioListener = new OnClickListener {
    def onClick(v: View) {
      val intent = new Intent(MediaPlayerDemo.this.getApplication,
                              classOf[MediaPlayerDemo_Audio])
      intent.putExtra(MEDIA, RESOURCES_AUDIO)
      startActivity(intent)
    }
  }

  private val mLocalVideoListener = new OnClickListener {
    def onClick(v: View) {
      val intent = new Intent(MediaPlayerDemo.this,
                              classOf[MediaPlayerDemo_Video])
      intent.putExtra(MEDIA, LOCAL_VIDEO)
      startActivity(intent)
    }
  }

  private val mStreamVideoListener = new OnClickListener {
    def onClick(v: View) {
      val intent = new Intent(MediaPlayerDemo.this,
                              classOf[MediaPlayerDemo_Video])
      intent.putExtra(MEDIA, STREAM_VIDEO)
      startActivity(intent)
    }
  }

}
