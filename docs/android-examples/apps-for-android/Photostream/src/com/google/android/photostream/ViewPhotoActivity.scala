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

package com.google.android.photostream

import scala.android.app.Activity

import android.content.{Context, Intent, ActivityNotFoundException}
import android.os.Bundle
import android.os.ParcelFileDescriptor.{MODE_WORLD_READABLE, MODE_WORLD_WRITEABLE}
import android.widget.{ImageView, LinearLayout, TextView, ViewAnimator, Toast}
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.view.{View, ViewGroup, ViewTreeObserver, Menu, MenuItem}
import android.view.animation.AnimationUtils
import android.net.Uri

import java.io.{File, IOException, OutputStream, InputStream, FileNotFoundException}

/**
 * Activity that displays a photo along with its title and the date at which it
 * was taken. This activity also lets the user set the photo as the wallpaper.
 */
class ViewPhotoActivity extends Activity
                           with View.OnClickListener
                           with ViewTreeObserver.OnGlobalLayoutListener {
  import ViewPhotoActivity._  // companion object

  private var mPhoto: Flickr.Photo = _

  private var mSwitcher: ViewAnimator = _
  private var mPhotoView: ImageView = _
  private var mContainer: ViewGroup = _

  // UserTask[Params, Progress, Result]
  //private var mTask: UserTask[Any, Any, Any] = _
  private var mCropTask: CropWallpaperTask = _
  private var mLoadTask: LoadPhotoTask =_
  private var mSetTask: SetWallpaperTask = _
  private var mPhotoTitle: TextView = _
  private var mPhotoDate: TextView = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    mPhoto = getPhoto()

    setContentView(R.layout.screen_photo)
    setupViews()
  }

  override protected def onDestroy() {
    super.onDestroy()
    def cancel(task: UserTask[_, _, _]) {
      if (task != null && task.getStatus != UserTask.Status.RUNNING) {
        task cancel true
      }
    }
    cancel(mCropTask)
    cancel(mLoadTask)
    cancel(mSetTask)
  }

  private def setupViews() {
    mContainer = findViewById(R.id.container_photo).asInstanceOf[ViewGroup]
    mSwitcher = findViewById(R.id.switcher_menu).asInstanceOf[ViewAnimator]
    mPhotoView = findViewById(R.id.image_photo).asInstanceOf[ImageView]

    mPhotoTitle = findViewById(R.id.caption_title).asInstanceOf[TextView]
    mPhotoDate = findViewById(R.id.caption_date).asInstanceOf[TextView]

    findViewById(R.id.menu_back) setOnClickListener this
    findViewById(R.id.menu_set) setOnClickListener this

    mPhotoTitle setText mPhoto.title
    mPhotoDate setText mPhoto.date

    mContainer setVisibility View.INVISIBLE

    // Sets up a view tree observer. The photo will be scaled using the size
    // of one of our views so we must wait for the first layout pass to be
    // done to make sure we have the correct size.
    mContainer.getViewTreeObserver addOnGlobalLayoutListener this
  }

  /**
   * Loads the photo after the first layout. The photo is scaled using the
   * dimension of the ImageView that will ultimately contain the photo's
   * bitmap. We make sure that the ImageView is laid out at least once to
   * get its correct size.
   */
  def onGlobalLayout() {
    mContainer.getViewTreeObserver removeGlobalOnLayoutListener this
    loadPhoto(mPhotoView.getMeasuredWidth, mPhotoView.getMeasuredHeight)
  }

  /**
   * Loads the photo either from the last known instance or from the network.
   * Loading it from the last known instance allows for fast display rotation
   * without having to download the photo from the network again.
   *
   * @param width The desired maximum width of the photo.
   * @param height The desired maximum height of the photo.
   */
  private def loadPhoto(width: Int, height: Int) {
    val data = getLastNonConfigurationInstance
    if (data == null) {
      mLoadTask = new LoadPhotoTask().execute(mPhoto, width.asInstanceOf[AnyRef], height.asInstanceOf[AnyRef])
    } else {
      mPhotoView setImageBitmap data.asInstanceOf[Bitmap]
      mSwitcher.showNext()
    }
  }

  /**
   * Loads the {@link com.google.android.photostream.Flickr.Photo} to display
   * from the intent used to start the activity.
   *
   * @return The photo to display, or null if the photo cannot be found.
   */
  def getPhoto(): Flickr.Photo = {
    val intent = getIntent
    val extras = intent.getExtras

    if (extras != null) extras getParcelable EXTRA_PHOTO
    else null
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.view_photo, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean = {
    if (item.getItemId == R.id.menu_item_radar) {
      onShowRadar()
    }
    super.onMenuItemSelected(featureId, item)
  }

  private def onShowRadar() {
    new ShowRadarTask().execute(mPhoto)
  }

  def onClick(v: View) {
    v.getId match {
      case R.id.menu_back =>
        onBack()
      case R.id.menu_set =>
        onSet()
      case _ =>
    }
  }

  private def onSet() {
    mCropTask = new CropWallpaperTask().execute(mPhoto)
  }

  private def onBack() {
    finish()
  }

  /**
   * If we successfully loaded a photo, send it to our future self to allow
   * for fast display rotation. By doing so, we avoid reloading the photo
   * from the network when the activity is taken down and recreated upon
   * display rotation.
   *
   * @return The Bitmap displayed in the ImageView, or null if the photo
   *         wasn't loaded.
   */
  override def onRetainNonConfigurationInstance(): AnyRef = {
    val d = mPhotoView.getDrawable
    if (d != null) d.asInstanceOf[BitmapDrawable].getBitmap else null
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int,
                                          data: Intent) {
    // Spawns a new task to set the wallpaper in a background thread when/if
    // we receive a successful result from the image cropper.
    if (requestCode == REQUEST_CROP_IMAGE) {
      if (resultCode == Activity.RESULT_OK) {
        mSetTask = new SetWallpaperTask().execute()
      } else {
        cleanupWallpaper()
        showWallpaperError()
      }
    }
  }

  private def showWallpaperError() {
    Toast.makeText(ViewPhotoActivity.this, R.string.error_cannot_save_file,
                   Toast.LENGTH_SHORT).show()
  }

  private def showWallpaperSuccess() {
    Toast.makeText(ViewPhotoActivity.this, R.string.success_wallpaper_set,
                   Toast.LENGTH_SHORT).show()
  }

  private def cleanupWallpaper() {
    deleteFile(WALLPAPER_FILE_NAME)
    mSwitcher.showNext()
  }

  /**
   * Background task to load the photo from Flickr. The task loads the bitmap,
   * then scale it to the appropriate dimension. The task ends by readjusting
   * the activity's layout so that everything aligns correctly.
   */
  private class LoadPhotoTask extends UserTask[AnyRef, Any, Bitmap] {

    def doInBackground(params: AnyRef*): Bitmap = {
      var bitmap = params(0)
        .asInstanceOf[Flickr.Photo]
        .loadPhotoBitmap(Flickr.PhotoSize.MEDIUM)
      if (bitmap == null) {
        bitmap = BitmapFactory.decodeResource(getResources, R.drawable.not_found)
      }

      val width = params(1).toString.toInt
      val height = params(2).toString.toInt

      val framed = ImageUtilities.scaleAndFrame(bitmap, width, height)
      bitmap.recycle()

      framed
    }

    override def onPostExecute(result: Bitmap) {
      mPhotoView setImageBitmap result

      // Find by how many pixels the title and date must be shifted on the
      // horizontal axis to be left aligned with the photo
      val offsetX = (mPhotoView.getMeasuredWidth - result.getWidth) / 2

      // Forces the ImageView to have the same size as its embedded bitmap
      // This will remove the empty space between the title/date pair and
      // the photo itself
      var params = mPhotoView.getLayoutParams.asInstanceOf[LinearLayout.LayoutParams]
      params.height = result.getHeight
      params.weight = 0.0f
      mPhotoView setLayoutParams params

      params = mPhotoTitle.getLayoutParams.asInstanceOf[LinearLayout.LayoutParams]
      params.leftMargin = offsetX
      mPhotoTitle setLayoutParams params

      params = mPhotoDate.getLayoutParams.asInstanceOf[LinearLayout.LayoutParams]
      params.leftMargin = offsetX
      mPhotoDate setLayoutParams params

      mSwitcher.showNext()
      mContainer.startAnimation(AnimationUtils.loadAnimation(ViewPhotoActivity.this,
                    R.anim.fade_in))
      mContainer setVisibility View.VISIBLE

      mLoadTask = null
    }
  }

  /**
   * Background task to crop a large version of the image. The cropped result
   * will be set as a wallpaper. The tasks sarts by showing the progress bar,
   * then downloads the large version of hthe photo into a temporary file and
   * ends by sending an intent to the Camera application to crop the image.
   */
  private class CropWallpaperTask extends UserTask[Flickr.Photo, Nothing, Boolean] {
    private var mFile: File = _

    override def onPreExecute() {
      mFile = getFileStreamPath(WALLPAPER_FILE_NAME)
      mSwitcher.showNext()
    }

    def doInBackground(params: Flickr.Photo*): Boolean = {
      var success = false

      var out: OutputStream = null
      try {
        out = openFileOutput(mFile.getName, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE)
        Flickr.get.downloadPhoto(params(0), Flickr.PhotoSize.LARGE, out)
        success = true
      } catch {
        case e: FileNotFoundException =>
          android.util.Log.e(Flickr.LOG_TAG, "Could not download photo", e)
          success = false
        case e: IOException =>
          android.util.Log.e(Flickr.LOG_TAG, "Could not download photo", e)
          success = false
      } finally {
        if (out != null) {
          try { out.close() }
          catch { case e: IOException => success = false }
        }
      }

      success
    }

    override def onPostExecute(result: Boolean) {
      if (!result) {
        cleanupWallpaper()
        showWallpaperError()
      } else {
        val width = getWallpaperDesiredMinimumWidth
        val height = getWallpaperDesiredMinimumHeight

        val intent = new Intent("com.android.camera.action.CROP")
        intent.setClassName("com.android.camera", "com.android.camera.CropImage")
        intent setData Uri.fromFile(mFile)
        intent.putExtra("outputX", width)
        intent.putExtra("outputY", height)
        intent.putExtra("aspectX", width)
        intent.putExtra("aspectY", height)
        intent.putExtra("scale", true)
        intent.putExtra("noFaceDetection", true)
        intent.putExtra("output", Uri.parse("file:/" + mFile.getAbsolutePath))

        startActivityForResult(intent, REQUEST_CROP_IMAGE)
      }

      mCropTask = null
    }

  } //class CropWallpaperTask

  /**
   * Background task to set the cropped image as the wallpaper. The task simply
   * open the temporary file and sets it as the new wallpaper. The task ends by
   * deleting the temporary file and display a message to the user.
   */
  private class SetWallpaperTask extends UserTask[Nothing, Nothing, Boolean] {

    def doInBackground(params: Nothing*): Boolean = {
      var success = false
      var in: InputStream = null
      try {
        in = openFileInput(WALLPAPER_FILE_NAME)
        setWallpaper(in)
        success = true
      } catch {
        case e: IOException => success = false
      } finally {
        if (in != null) {
          try { in.close() }
          catch { case e: IOException => success = false }
        }
      }
      success
    }

    override def onPostExecute(result: Boolean) {
      cleanupWallpaper()

      if (!result) {
        showWallpaperError()
      } else {
        showWallpaperSuccess()
      }

      mSetTask = null
    }

  } //class SetWallpaperTask

  private class ShowRadarTask extends UserTask[Flickr.Photo, Nothing, Flickr.Location] {

    def doInBackground(params: Flickr.Photo*): Flickr.Location =
       Flickr.get getLocation params(0)

    override def onPostExecute(location: Flickr.Location) {
      if (location != null) {
        val intent = new Intent(RADAR_ACTION)
        intent.putExtra(RADAR_EXTRA_LATITUDE, location.latitude)
        intent.putExtra(RADAR_EXTRA_LONGITUDE, location.longitude)

        try {
          startActivity(intent)
        } catch {
          case e: ActivityNotFoundException =>
            Toast.makeText(ViewPhotoActivity.this,
                           R.string.error_cannot_find_radar,
                           Toast.LENGTH_SHORT).show()
        }
      } else {
        Toast.makeText(ViewPhotoActivity.this,
                       R.string.error_cannot_find_location,
                       Toast.LENGTH_SHORT).show()
      }
    }

  }
}

object ViewPhotoActivity {

  final val ACTION = "com.google.android.photostream.FLICKR_PHOTO"

  private final val RADAR_ACTION = "com.google.android.radar.SHOW_RADAR"
  private final val RADAR_EXTRA_LATITUDE = "latitude"
  private final val RADAR_EXTRA_LONGITUDE = "longitude"

  private final val EXTRA_PHOTO = "com.google.android.photostream.photo"

  private final val WALLPAPER_FILE_NAME = "wallpaper"

  private final val REQUEST_CROP_IMAGE = 42

  /**
   * Starts the ViewPhotoActivity for the specified photo.
   *
   * @param context The application's environment.
   * @param photo The photo to display and optionally set as a wallpaper.
   */
  def show(context: Context, photo: Flickr.Photo) {
    val intent = new Intent(ACTION)
    intent.putExtra(EXTRA_PHOTO, photo)
    context startActivity intent
  }

}
