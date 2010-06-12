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
import android.media.{AudioManager, MediaPlayer}
import android.media.MediaPlayer.{OnBufferingUpdateListener, OnCompletionListener,
                                  OnPreparedListener, OnVideoSizeChangedListener}
import android.os.Bundle
import android.util.Log
import android.view.{SurfaceHolder, SurfaceView}
import android.widget.Toast

class MediaPlayerDemo_Video extends Activity
                               with OnBufferingUpdateListener
                               with OnCompletionListener
                               with OnPreparedListener
                               with OnVideoSizeChangedListener
                               with SurfaceHolder.Callback {
  import MediaPlayerDemo_Video._  // companion object

  private var mVideoWidth: Int = _
  private var mVideoHeight: Int = _
  private var mMediaPlayer: MediaPlayer = _
  private var mPreview: SurfaceView = _
  private var holder: SurfaceHolder = _
  private var path: String = _
  private var extras: Bundle = _
  private var mIsVideoSizeKnown = false
  private var mIsVideoReadyToBePlayed = false

  /**
   * 
   * Called when the activity is first created.
   */
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.mediaplayer_2)
    mPreview = findViewById(R.id.surface).asInstanceOf[SurfaceView]
    holder = mPreview.getHolder
    holder addCallback this
    holder setType SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS
    extras = getIntent.getExtras
  }

  private def playVideo(media: Int) {
    doCleanUp()
    try {

      media match {
        case LOCAL_VIDEO =>
          /*
           * TODO: Set the path variable to a local media file path.
           */
          path = ""
          if (path == "") {
            // Tell the user to provide a media file URL.
            Toast.makeText(MediaPlayerDemo_Video.this,
                           "Please edit MediaPlayerDemo_Video Activity, "
                           + "and set the path variable to your media file path."
                           + " Your media file must be stored on sdcard.",
                           Toast.LENGTH_LONG).show()
          }

        case STREAM_VIDEO =>
          /*
           * TODO: Set path variable to progressive streamable mp4 or
           * 3gpp format URL. Http protocol should be used.
           * Mediaplayer can only play "progressive streamable
           * contents" which basically means: 1. the movie atom has to
           * precede all the media data atoms. 2. The clip has to be
           * reasonably interleaved.
           * 
           */
          path = ""
          if (path == "") {
            // Tell the user to provide a media file URL.
            Toast.makeText(MediaPlayerDemo_Video.this,
                           "Please edit MediaPlayerDemo_Video Activity,"
                           + " and set the path variable to your media file URL.",
                           Toast.LENGTH_LONG).show()
          }

      }

      // Create a new media player and set the listeners
      mMediaPlayer = new MediaPlayer
      mMediaPlayer setDataSource path
      mMediaPlayer setDisplay holder
      mMediaPlayer.prepare()
      mMediaPlayer setOnBufferingUpdateListener this
      mMediaPlayer setOnCompletionListener this
      mMediaPlayer setOnPreparedListener this
      mMediaPlayer setOnVideoSizeChangedListener this
      mMediaPlayer setAudioStreamType AudioManager.STREAM_MUSIC

    } catch {
      case e: Exception =>
        Log.e(TAG, "error: " + e.getMessage(), e)
    }
  }

  def onBufferingUpdate(arg0: MediaPlayer, percent: Int) {
    Log.d(TAG, "onBufferingUpdate percent:" + percent)
  }

  def onCompletion(arg0: MediaPlayer) {
    Log.d(TAG, "onCompletion called")
  }

  def onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
    Log.v(TAG, "onVideoSizeChanged called")
    if (width == 0 || height == 0) {
      Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")")
      return
    }
    mIsVideoSizeKnown = true
    mVideoWidth = width
    mVideoHeight = height
    if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
      startVideoPlayback()
    }
  }

  def onPrepared(mediaplayer: MediaPlayer) {
    Log.d(TAG, "onPrepared called")
    mIsVideoReadyToBePlayed = true
    if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
      startVideoPlayback()
    }
  }

  def surfaceChanged(surfaceholder: SurfaceHolder, i: Int, j: Int, k: Int) {
    Log.d(TAG, "surfaceChanged called")
  }

  def surfaceDestroyed(surfaceholder: SurfaceHolder) {
    Log.d(TAG, "surfaceDestroyed called")
  }

  def surfaceCreated(holder: SurfaceHolder) {
    Log.d(TAG, "surfaceCreated called")
    playVideo(extras getInt MEDIA)
  }

  override protected def onPause() {
    super.onPause()
    releaseMediaPlayer()
    doCleanUp()
  }

  override protected def onDestroy() {
    super.onDestroy()
    releaseMediaPlayer()
    doCleanUp()
  }

  private def releaseMediaPlayer() {
    if (mMediaPlayer != null) {
      mMediaPlayer.release()
      mMediaPlayer = null
    }
  }

  private def doCleanUp() {
    mVideoWidth = 0
    mVideoHeight = 0
    mIsVideoReadyToBePlayed = false
    mIsVideoSizeKnown = false
  }

  private def startVideoPlayback() {
    Log.v(TAG, "startVideoPlayback")
    holder.setFixedSize(mVideoWidth, mVideoHeight)
    mMediaPlayer.start()
  }
}

object MediaPlayerDemo_Video {
  private final val TAG = "MediaPlayerDemo"

  private final val MEDIA = "media"
  private final val LOCAL_AUDIO = 1
  private final val STREAM_AUDIO = 2
  private final val RESOURCES_AUDIO = 3
  private final val LOCAL_VIDEO = 4
  private final val STREAM_VIDEO = 5
}
