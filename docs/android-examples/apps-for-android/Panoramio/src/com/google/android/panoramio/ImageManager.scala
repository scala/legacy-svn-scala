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

import org.apache.http.{HttpEntity, HttpResponse}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.json.{JSONArray, JSONException, JSONObject}

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log

import scala.collection.mutable.ListBuffer
import scala.ref.WeakReference

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI

/**
 * This class is responsible for downloading and parsing the search results for
 * a particular area. All of the work is done on a separate thread, and progress
 * is reported back through the DataSetObserver set in
 * {@link #addObserver(DataSetObserver). State is held in memory by in memory
 * maintained by a single instance of the ImageManager class.
 */
class ImageManager private (mContext: Context) {
  import ImageManager._  // companion object

  /**
   * Used to post results back to the UI thread
   */
  private val mHandler = new Handler()

  /**
   * Holds the images and related data that have been downloaded
   */
  private val mImages = new ListBuffer[PanoramioItem]()

  /**
   * Observers interested in changes to the current search results
   */
  private val mObservers = new ListBuffer[WeakReference[DataSetObserver]]()

  /**
   * True if we are in the process of loading
   */
  private var mLoading: Boolean = _
    
  /**
   * @return True if we are still loading content
   */
  def isLoading: Boolean = mLoading
    
  /**
   * Clear all downloaded content
   */
  def clear() {
    mImages.clear()
    notifyObservers()
  }
    
  /**
   * Add an item to and notify observers of the change.
   * @param item The item to add
   */
  private def add(item: PanoramioItem) {
    mImages += item
    notifyObservers()
  }
    
  /**
   * @return The number of items displayed so far
   */
  def size: Int = mImages.size

  /**
   * Gets the item at the specified position
   */
  def get(position: Int): PanoramioItem = mImages(position)
    
  /**
   * Adds an observer to be notified when the set of items held by this ImageManager changes.
   */
  def addObserver(observer: DataSetObserver) {
    val obs = new WeakReference[DataSetObserver](observer)
    mObservers += obs
  }
    
  /**
   * Load a new set of search results for the specified area.
   * 
   * @param minLong The minimum longitude for the search area
   * @param maxLong The maximum longitude for the search area
   * @param minLat The minimum latitude for the search area
   * @param maxLat The minimum latitude for the search area
   */
  def load(minLong: Float, maxLong: Float, minLat: Float, maxLat: Float) {
    mLoading = true
    new NetworkThread(minLong, maxLong, minLat, maxLat).start()
  }
    
  /**
   * Called when something changes in our data set. Cleans up any weak references that
   * are no longer valid along the way.
   */
  private def notifyObservers() {
    val observers = mObservers
    val count = observers.size
    for (i <- count - 1 to 0 by -1) {
      val weak: WeakReference[DataSetObserver] = observers(i)
      weak.get match {
        case Some(obs) => obs.onChanged()
        case None => observers remove i
      }
    }
        
  }
    
  /**
   * This thread does the actual work of downloading and parsing data.
   *
   */
  private class NetworkThread(mMinLong: Float, mMaxLong: Float,
                              mMinLat: Float, mMaxLat: Float) extends Thread {

    override def run() {
      val url = String.format(THUMBNAIL_URL,
        mMinLat.asInstanceOf[AnyRef], mMinLong.asInstanceOf[AnyRef],
        mMaxLat.asInstanceOf[AnyRef], mMaxLong.asInstanceOf[AnyRef])
      try {
        val uri = new URI("http", url, null)
        val get = new HttpGet(uri)
                
        val client = new DefaultHttpClient()
        val response = client.execute(get)
        val entity = response.getEntity();
        val str = convertStreamToString(entity.getContent)
        val json = new JSONObject(str)
        parse(json)
      } catch {
        case e: Exception =>
          Log.e(TAG, e.toString)
      }
    }

    private def parse(json: JSONObject) {
      try {
        val array = json.getJSONArray("photos")
        val count = array.length
        for (i <- 0 until count) {
          val obj = array getJSONObject i

          val id = obj getLong "photo_id"
          var title = obj getString "photo_title"
          val owner = obj getString "owner_name"
          val thumb = obj getString "photo_file_url"
          val ownerUrl = obj getString "owner_url"
          val photoUrl = obj getString "photo_url"
          val latitude = obj getDouble "latitude"
          val longitude = obj getDouble "longitude"
          val b = BitmapUtils.loadBitmap(thumb)
          if (title == null) {
            title = mContext getString R.string.untitled
          }

          val item = new PanoramioItem(id, thumb, b,
                       (latitude * Panoramio.MILLION).toInt,
                       (longitude * Panoramio.MILLION).toInt,
                       title, owner, ownerUrl, photoUrl)
          val done = i == count - 1
          mHandler post new Runnable() {
            def run() { 
              sInstance.mLoading = !done
              sInstance add item
            }
          }
        }
      } catch {
        case e: JSONException =>
          Log.e(TAG, e.toString)
      }
    }

    private def convertStreamToString(is: InputStream): String = {
      val reader = new BufferedReader(new InputStreamReader(is), 8*1024)
      val sb = new StringBuilder()
      try {
        var line = reader.readLine()
        while (line != null) {
          sb.append(line + "\n")
          line = reader.readLine()
        }
      } catch {
        case e: IOException => e.printStackTrace()
      } finally {
        try { is.close() }
        catch { case e: IOException => e.printStackTrace() }
      }
      sb.toString
    }

  }

}

object ImageManager {

  private final val TAG = "Panoramio"
    
  /**
   * Base URL for Panoramio's web API
   */
  private final val THUMBNAIL_URL = "//www.panoramio.com/map/get_panoramas.php?order=popularity&set=public&from=0&to=20&miny=%f&minx=%f&maxy=%f&maxx=%f&size=thumbnail"

  /**
   * Holds the single instance of a ImageManager that is shared by the process.
   */
  private var sInstance: ImageManager = _

  /**
   * Key for an Intent extra. The value is the zoom level selected by the user.
   */
  final val ZOOM_EXTRA = "zoom"

  /**
   * Key for an Intent extra. The value is the latitude of the center of the search
   * area chosen by the user.
   */
  final val LATITUDE_E6_EXTRA = "latitudeE6"

  /**
   * Key for an Intent extra. The value is the latitude of the center of the search
   * area chosen by the user.
   */
  final val LONGITUDE_E6_EXTRA = "longitudeE6"

  /**
   * Key for an Intent extra. The value is an item to display
   */
  final val PANORAMIO_ITEM_EXTRA = "item"
    
  final def getInstance(c: Context): ImageManager = {
    if (sInstance == null) {
      sInstance = new ImageManager(c.getApplicationContext)
    }
    sInstance
  }

}
