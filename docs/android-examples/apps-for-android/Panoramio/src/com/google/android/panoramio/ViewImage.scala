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

import com.google.android.maps.GeoPoint

import android.app.{Activity, AlertDialog, Dialog}
import android.content.{ActivityNotFoundException, Intent}
import android.graphics.Bitmap
import android.net.Uri
import android.os.{Bundle, Handler}
import android.util.Log
import android.view.{Menu, MenuItem, View, Window}
import android.widget.{ImageView, TextView}

/**
 * Activity which displays a single image.
 */
class ViewImage extends Activity {
  import ViewImage._  // companion object

  var mItem: PanoramioItem = _

  private var mHandler: Handler = _

  private var mImage: ImageView = _

  private var mTitle: TextView = _

  private var mOwner: TextView = _

  private var mContent: View = _

  private var mMapZoom: Int = _

  private var mMapLatitudeE6: Int = _

  private var mMapLongitudeE6: Int = _

  override protected def onCreate(savedInstanceState: Bundle) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.view_image)

    // Remember the user's original search area and zoom level
    val i = getIntent
    mItem = i getParcelableExtra ImageManager.PANORAMIO_ITEM_EXTRA
    mMapZoom = i.getIntExtra(ImageManager.ZOOM_EXTRA, Integer.MIN_VALUE)
    mMapLatitudeE6 = i.getIntExtra(ImageManager.LATITUDE_E6_EXTRA, Int.MinValue)
    mMapLongitudeE6 = i.getIntExtra(ImageManager.LONGITUDE_E6_EXTRA, Int.MinValue)

    mHandler = new Handler()

    mContent = findViewById(R.id.content)
    mImage = findViewById(R.id.image).asInstanceOf[ImageView]
    mTitle = findViewById(R.id.title).asInstanceOf[TextView]
    mOwner = findViewById(R.id.owner).asInstanceOf[TextView]

    mContent setVisibility View.GONE
    getWindow.setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                            Window.PROGRESS_VISIBILITY_ON)
    new LoadThread().start()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu.add(0, MENU_RADAR, 0, R.string.menu_radar)
        .setIcon(R.drawable.ic_menu_radar)
        .setAlphabeticShortcut('R')
    menu.add(0, MENU_MAP, 0, R.string.menu_map)
        .setIcon(R.drawable.ic_menu_map)
        .setAlphabeticShortcut('M')
    menu.add(0, MENU_AUTHOR, 0, R.string.menu_author)
        .setIcon(R.drawable.ic_menu_author)
        .setAlphabeticShortcut('A')
    menu.add(0, MENU_VIEW, 0, R.string.menu_view)
        .setIcon(android.R.drawable.ic_menu_view)
        .setAlphabeticShortcut('V')
    true
  }
    
  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case MENU_RADAR =>
        // Launch the radar activity (if it is installed)
        val i = new Intent("com.google.android.radar.SHOW_RADAR")
        val location: GeoPoint = mItem.getLocation
        i.putExtra("latitude", (location.getLatitudeE6() / 1000000f))
        i.putExtra("longitude", (location.getLongitudeE6() / 1000000f))
        try {
          startActivity(i)
        } catch {
          case ex: ActivityNotFoundException =>
            showDialog(DIALOG_NO_RADAR)
        }
        true
      case MENU_MAP =>
        // Display our custom map 
        val i = new Intent(this, classOf[ViewMap])
        i.putExtra(ImageManager.PANORAMIO_ITEM_EXTRA, mItem)
        i.putExtra(ImageManager.ZOOM_EXTRA, mMapZoom)
        i.putExtra(ImageManager.LATITUDE_E6_EXTRA, mMapLatitudeE6)
        i.putExtra(ImageManager.LONGITUDE_E6_EXTRA, mMapLongitudeE6)
        startActivity(i)
        true
      case MENU_AUTHOR =>
        // Display the author info page in the browser
        val i = new Intent(Intent.ACTION_VIEW)
        i setData Uri.parse(mItem.getOwnerUrl)
        startActivity(i)
        true
      case MENU_VIEW =>
        // Display the photo info page in the browser
        val i = new Intent(Intent.ACTION_VIEW)
        i setData Uri.parse(mItem.getPhotoUrl)
        startActivity(i)
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }
    
  override protected def onCreateDialog(id: Int): Dialog = {
    id match {
      case DIALOG_NO_RADAR =>
        val builder = new AlertDialog.Builder(this)
        builder.setTitle(R.string.no_radar_title)
               .setMessage(R.string.no_radar)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .setPositiveButton(android.R.string.ok, null).create()
      case _ =>
        null
    }
  }


  /**
   * Utility to load a larger version of the image in a separate thread.
   *
   */
  private class LoadThread extends Thread {

    override def run() {
      try {
        var uri = mItem.getThumbUrl
        uri = uri.replace("thumbnail", "medium")
        val b = BitmapUtils.loadBitmap(uri)
        mHandler post new Runnable() {
          def run() {
            mImage setImageBitmap b
            mTitle setText mItem.getTitle
            mOwner setText mItem.getOwner
            mContent setVisibility View.VISIBLE
            getWindow.setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                                    Window.PROGRESS_VISIBILITY_OFF)
          }
        }
      } catch {
        case e: Exception =>
          Log.e(TAG, e.toString)
      }
    }
  }

}

object ViewImage {

  private final val TAG = "Panoramio"

  private final val MENU_RADAR  = Menu.FIRST + 1
  private final val MENU_MAP    = Menu.FIRST + 2
  private final val MENU_AUTHOR = Menu.FIRST + 3
  private final val MENU_VIEW   = Menu.FIRST + 4

  private final val DIALOG_NO_RADAR = 1

}
