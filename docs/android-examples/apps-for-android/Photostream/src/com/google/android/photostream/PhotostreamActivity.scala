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

import android.app.{Activity, NotificationManager}
import android.content.{Context, Intent}
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.{LayoutInflater, View}
import android.view.animation.{AccelerateDecelerateInterpolator, Animation,
         AnimationUtils, LayoutAnimationController, TranslateAnimation}
import android.widget.{ImageView, ViewAnimator}

import scala.util.Random

/**
 * Activity used to display a Flickr user's photostream. This activity shows a
 * fixed number of photos at a time. The activity is invoked either by
 * LoginActivity, when the application is launched normally, or by a Home
 * shortcut, or by an Intent with the view action and a flickr://photos/nsid URI.
 */
class PhotostreamActivity extends Activity
                             with View.OnClickListener
                             with Animation.AnimationListener {
  import PhotostreamActivity._  // companion object

  private var mUser: Flickr.User = _
  private var mCurrentPage = 1
  private var mPageCount = 0

  private var mInflater: LayoutInflater = _

  private var mSwitcher: ViewAnimator = _
  private var mMenuNext: View = _
  private var mMenuBack: View = _
  private var mMenuSeparator: View = _
  private var mGrid: GridLayout = _

  private var mNextAnimation: LayoutAnimationController = _
  private var mBackAnimation: LayoutAnimationController = _

  // UserTask[Params, Progress, Result]
  //private var mTask: UserTask[Any, Any, Any] = _
  private var mGetListTask: GetPhotoListTask = _
  private var mLoadTask: LoadPhotosTask = _
  private var mUsername: String = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    clearNotification()

    // Try to find a user name in the saved instance state or the intent
    // that launched the activity. If no valid user NSID can be found, we
    // just close the activity.
    if (!initialize(savedInstanceState)) {
      finish()
      return
    }

    setContentView(R.layout.screen_photostream)
    setupViews()

    loadPhotos()
  }

  private def clearNotification() {
    val notification = getIntent.getIntExtra(EXTRA_NOTIFICATION, -1)
    if (notification != -1) {
      val manager = getSystemService(Context.NOTIFICATION_SERVICE)
        .asInstanceOf[NotificationManager]
      manager cancel notification
    }
  }

  /**
   * Restores a previously saved state or, if missing, finds the user's NSID
   * from the intent used to start the activity.
   *
   * @param savedInstanceState The saved state, if any.
   *
   * @return true if a {@link com.google.android.photostream.Flickr.User} was
   *         found either in the saved state or the intent.
   */
  private def initialize(savedInstanceState: Bundle): Boolean = {
    mUser = if (savedInstanceState != null) {
      mCurrentPage = savedInstanceState getInt STATE_PAGE
      mPageCount = savedInstanceState getInt STATE_PAGE_COUNT
      savedInstanceState getParcelable STATE_USER
    } else {
      getUser
    }
    mUser != null || mUsername != null
  }

  /**
   * Creates a {@link com.google.android.photostream.Flickr.User} instance
   * from the intent used to start this activity.
   *
   * @return The user whose photos will be displayed, or null if no
   *         user was found.
   */
  private def getUser: Flickr.User = {
    val intent = getIntent
    val action = intent.getAction

    var user: Flickr.User = null

    if (ACTION equals action) {
      val extras: Bundle = intent.getExtras
      if (extras != null) {
        user = extras getParcelable EXTRA_USER

        if (user == null) {
          val nsid = extras getString EXTRA_NSID
          if (nsid != null) {
            user = Flickr.User.fromId(nsid)
          }
        }
      }
    } else if (Intent.ACTION_VIEW equals action) {
      val segments = intent.getData.getPathSegments
      if (segments.size > 1) {
        mUsername = segments get 1
      }
    }

    user
  }

  private def setupViews() {
    mInflater = LayoutInflater.from(PhotostreamActivity.this)
    mNextAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_slide_next)
    mBackAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_slide_back)

    mSwitcher = findViewById(R.id.switcher_menu).asInstanceOf[ViewAnimator]
    mMenuNext = findViewById(R.id.menu_next)
    mMenuBack = findViewById(R.id.menu_back)
    mMenuSeparator = findViewById(R.id.menu_separator)
    mGrid = findViewById(R.id.grid_photos).asInstanceOf[GridLayout]

    mMenuNext setOnClickListener this
    mMenuBack setOnClickListener this
    mMenuBack setVisibility View.GONE
    mMenuSeparator setVisibility View.GONE
    mGrid setClipToPadding false
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putParcelable(STATE_USER, mUser)
    outState.putInt(STATE_PAGE, mCurrentPage)
    outState.putInt(STATE_PAGE_COUNT, mPageCount)
  }

  override protected def onDestroy() {
    super.onDestroy()
    def cancel(task: UserTask[_, _, _]) {
      if (task != null && task.getStatus == UserTask.Status.RUNNING) {
        task cancel true
      }
    }
    cancel(mGetListTask)
    cancel(mLoadTask)
  }

  def onClick(v: View) {
    v.getId match {
      case R.id.menu_next =>
        onNext()
      case R.id.menu_back =>
        onBack()
      case _ =>
        onShowPhoto(v.getTag.asInstanceOf[Flickr.Photo])
    }
  }

  override def onRetainNonConfigurationInstance(): AnyRef = {
    val grid = mGrid
    val count = grid.getChildCount
    val list = new Array[LoadedPhoto](count)

    for (i <- 0 until count) {
      val v = grid.getChildAt(i).asInstanceOf[ImageView]
      list(i) = new LoadedPhoto(
        v.getDrawable.asInstanceOf[BitmapDrawable].getBitmap,
        v.getTag.asInstanceOf[Flickr.Photo])
    }

    list.asInstanceOf[AnyRef]
  }

  private def prepareMenu(pageCount: Int) {
    val backVisible = mCurrentPage > 1
    val nextVisible = mCurrentPage < pageCount

    mMenuBack.setVisibility(if (backVisible) View.VISIBLE else View.GONE)
    mMenuNext.setVisibility(if (nextVisible) View.VISIBLE else View.GONE)

    mMenuSeparator.setVisibility(
      if (backVisible && nextVisible) View.VISIBLE else View.GONE)
  }

  private def loadPhotos() {
    val data = getLastNonConfigurationInstance
    if (data == null) {
      mGetListTask = new GetPhotoListTask().execute(mCurrentPage)
    } else {
      val photos = data.asInstanceOf[Array[LoadedPhoto]]
      for (photo <- photos) {
        addPhoto(photo)
      }
      prepareMenu(mPageCount)
      mSwitcher.showNext()
    }
  }

  private def showPhotos(photos: Flickr.PhotoList) {
    mLoadTask = new LoadPhotosTask().execute(photos)
  }

  private def onShowPhoto(photo: Flickr.Photo) {
    ViewPhotoActivity.show(this, photo)
  }

  private def onNext() {
    mCurrentPage += 1
    animateAndLoadPhotos(mNextAnimation)
  }

  private def onBack() {
    mCurrentPage -= 1
    animateAndLoadPhotos(mBackAnimation)
  }

  private def animateAndLoadPhotos(animation: LayoutAnimationController) {
    mSwitcher.showNext()
    mGrid setLayoutAnimationListener this
    mGrid setLayoutAnimation animation
    mGrid.invalidate()
  }

  def onAnimationEnd(animation: Animation) {
    mGrid setLayoutAnimationListener null
    mGrid setLayoutAnimation null
    mGrid.removeAllViews()
    loadPhotos()
  }

  def onAnimationStart(animation: Animation) {
  }

  def onAnimationRepeat(animation: Animation) {
  }

  private def addPhoto(value: LoadedPhoto*) {
    val image = mInflater
      .inflate(R.layout.grid_item_photo, mGrid, false)
      .asInstanceOf[ImageView]
    image setImageBitmap value(0).bitmap
    image startAnimation createAnimationForChild(mGrid.getChildCount)
    image setTag value(0).photo
    image setOnClickListener PhotostreamActivity.this
    mGrid addView image
  }    

  /**
   * Background task used to load each individual photo. The task loads each
   * photo in order and publishes each loaded Bitmap as a progress unit. The
   * tasks ends by hiding the progress bar and showing the menu.
   */
  private class LoadPhotosTask extends UserTask[Flickr.PhotoList, LoadedPhoto, Flickr.PhotoList] {
    val mRandom = new Random()

    def doInBackground(params: Flickr.PhotoList*): Flickr.PhotoList = {
      val list: Flickr.PhotoList = params(0)
      val count = list.getCount

      for (i <- 0 until count if !isCancelled) {
        val photo: Flickr.Photo = list get i
        var bitmap = photo loadPhotoBitmap Flickr.PhotoSize.THUMBNAIL
        if (!isCancelled) {
          if (bitmap == null) {
            val portrait = mRandom.nextFloat >= 0.5f
            bitmap = BitmapFactory.decodeResource(getResources,
              if (portrait) R.drawable.not_found_small_1
              else R.drawable.not_found_small_2)
          }
          publishProgress(new LoadedPhoto(ImageUtilities.rotateAndFrame(bitmap), photo))
          bitmap.recycle()
        }
      }

      list
    }

    /**
     * Whenever a photo's Bitmap is loaded from the background thread, it is
     * displayed in this method by adding a new ImageView in the photos grid.
     * Each ImageView's tag contains the {@link com.google.android.photostream.Flickr.Photo}
     * it was loaded from.
     *
     * @param value The photo and its bitmap.
     */
    override def onProgressUpdate(value: LoadedPhoto*) {
      addPhoto(value: _*)
    }

    override def onPostExecute(result: Flickr.PhotoList) {
      mPageCount = result.getPageCount
      prepareMenu(mPageCount)
      mSwitcher.showNext()
      mLoadTask = null
    }

  } //class LoadPhotosTask

  /**
   * Background task used to load the list of photos. The tasks queries Flickr
   * for the list of photos to display and ends by starting the LoadPhotosTask.
   */
  private class GetPhotoListTask extends UserTask[Int, Nothing, Flickr.PhotoList] {

    def doInBackground(params: Int*): Flickr.PhotoList = {
      if (mUsername != null) {
        mUser = Flickr.get findByUserName mUsername
        mUsername = null
      }
      Flickr.get.getPublicPhotos(mUser, PHOTOS_COUNT_PER_PAGE, params(0))
    }

    override def onPostExecute(photoList: Flickr.PhotoList) {
      showPhotos(photoList)
      mGetListTask = null
    }

  }

}

object PhotostreamActivity {

  final val ACTION = "com.google.android.photostream.FLICKR_STREAM"

  final val EXTRA_NOTIFICATION =
    "com.google.android.photostream.extra_notify_id"
  final val EXTRA_NSID = "com.google.android.photostream.extra_nsid"
  final val EXTRA_USER = "com.google.android.photostream.extra_user"

  private final val STATE_USER = "com.google.android.photostream.state_user"
  private final val STATE_PAGE = "com.google.android.photostream.state_page"
  private final val STATE_PAGE_COUNT =
    "com.google.android.photostream.state_pagecount"

  private final val PHOTOS_COUNT_PER_PAGE = 6

  /**
   * Starts the PhotostreamActivity for the specified user.
   *
   * @param context The application's environment.
   * @param user The user whose photos to display with a PhotostreamActivity.
   */
  def show(context: Context, user: Flickr.User) {
    val intent = new Intent(ACTION)
    intent.putExtra(EXTRA_USER, user)
    context startActivity intent
  }

  private def createAnimationForChild(childIndex: Int): Animation = {
    val firstColumn = (childIndex & 0x1) == 0

    val translate = new TranslateAnimation(
      Animation.RELATIVE_TO_SELF, if (firstColumn) -1.1f else 1.1f,
      Animation.RELATIVE_TO_SELF, 0.0f,
      Animation.RELATIVE_TO_SELF, 0.0f,
      Animation.RELATIVE_TO_SELF, 0.0f)

    translate setInterpolator new AccelerateDecelerateInterpolator()
    translate setFillAfter false
    translate setDuration 300

    translate
  }

  /**
   * A LoadedPhoto contains the Flickr photo and the Bitmap loaded for that photo.
   */
  private case class LoadedPhoto(bitmap: Bitmap, photo: Flickr.Photo)

}
