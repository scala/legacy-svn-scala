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

package com.example.android.home

import android.app.{Activity, ActivityManager, SearchManager}
import android.content.{BroadcastReceiver, ComponentName, Context,
                        Intent, IntentFilter}
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.{ActivityInfo, PackageManager, ResolveInfo}
import android.graphics.{Bitmap, Canvas, Paint, PaintFlagsDrawFilter,
                         PixelFormat, Rect, ColorFilter}
import android.graphics.drawable.{BitmapDrawable, Drawable, PaintDrawable}
import android.os.{Bundle, Environment}
import android.util.{Log, Xml}
import android.view.{KeyEvent, LayoutInflater, Menu, MenuItem, View, ViewGroup}
import android.view.animation.{Animation, AnimationUtils, LayoutAnimationController}
import android.widget.{AdapterView, ArrayAdapter, CheckBox, GridView, TextView}

import java.io.{IOException, FileReader, File, FileNotFoundException}
import java.util.Collections

import org.xmlpull.v1.{XmlPullParser, XmlPullParserException}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object Home {
  /**
   * Tag used for logging errors.
   */
  private final val LOG_TAG = "Home"

  /**
   * Keys during freeze/thaw.
   */
  private final val KEY_SAVE_GRID_OPENED = "grid.opened"

  private final val DEFAULT_FAVORITES_PATH = "etc/favorites.xml"

  private final val TAG_FAVORITES = "favorites"
  private final val TAG_FAVORITE = "favorite"
  private final val TAG_PACKAGE = "package"
  private final val TAG_CLASS = "class"

  // Identifiers for option menu items
  private final val MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1
  private final val MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1
  private final val MENU_SETTINGS = MENU_SEARCH + 1

  /**
   * Maximum number of recent tasks to query.
   */
  private final val MAX_RECENT_TASKS = 20

  private var mWallpaperChecked: Boolean = _
  private var mApplications: ArrayBuffer[ApplicationInfo] = _
  private var mFavorites: ListBuffer[ApplicationInfo] = _

  @throws(classOf[XmlPullParserException])
  @throws(classOf[IOException])
  private def beginDocument(parser: XmlPullParser, firstElementName: String) {
    var typ = parser.next()
    while (typ != XmlPullParser.START_TAG &&
           typ != XmlPullParser.END_DOCUMENT) {
      parser.next()
    }

    if (typ != XmlPullParser.START_TAG) {
      throw new XmlPullParserException("No start tag found")
    }

    if (!parser.getName.equals(firstElementName)) {
      throw new XmlPullParserException(
        "Unexpected start tag: found " + parser.getName() +
        ", expected " + firstElementName)
    }
  }

  @throws(classOf[XmlPullParserException])
  @throws(classOf[IOException])
  private def nextElement(parser: XmlPullParser) {
    var typ = parser.next()
    while (typ != XmlPullParser.START_TAG &&
           typ != XmlPullParser.END_DOCUMENT) {
      typ = parser.next()
    }
  }

  private def getApplicationInfo(manager: PackageManager,
                                 intent: Intent): ApplicationInfo = {
    val resolveInfo = manager.resolveActivity(intent, 0)

    if (resolveInfo == null) {
      return null
    }

    val info = new ApplicationInfo
    val activityInfo = resolveInfo.activityInfo
    info.icon = activityInfo loadIcon manager
    if (info.title == null || info.title.length() == 0) {
      info.title = activityInfo loadLabel manager
    }
    if (info.title == null) {
      info.title = ""
    }
    info
  }
}

class Home extends Activity {
  import Activity._, Home._  // companion object

  private final val mWallpaperReceiver: BroadcastReceiver =
    new WallpaperIntentReceiver
  private final val mApplicationsReceiver: BroadcastReceiver =
    new ApplicationsIntentReceiver

  private var mGrid: GridView = _

  private var mShowLayoutAnimation: LayoutAnimationController = _
  private var mHideLayoutAnimation: LayoutAnimationController = _

  private var mBlockAnimation: Boolean = _

  private var mHomeDown: Boolean = _
  private var mBackDown: Boolean = _
    
  private var mShowApplications: View = _
  private var mShowApplicationsCheck: CheckBox = _

  private var mApplicationsStack: ApplicationsStackLayout = _

  private var mGridEntry: Animation = _
  private var mGridExit: Animation = _
    
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)

    setContentView(R.layout.home)

    registerIntentReceivers()

    setDefaultWallpaper()

    loadApplications(true)

    bindApplications()
    bindFavorites(true)
    bindRecents()
    bindButtons()

    mGridEntry = AnimationUtils.loadAnimation(this, R.anim.grid_entry)
    mGridExit = AnimationUtils.loadAnimation(this, R.anim.grid_exit)
  }

  override protected def onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    // Close the menu
    if (Intent.ACTION_MAIN equals intent.getAction) {
      getWindow.closeAllPanels
    }
  }

  override def onDestroy() {
    super.onDestroy()

    // Remove the callback for the cached drawables or we leak
    // the previous Home screen on orientation change
    for (info <- mApplications) {
      info.icon setCallback null
    }

    unregisterReceiver(mWallpaperReceiver)
    unregisterReceiver(mApplicationsReceiver)
  }

  override protected def onResume() {
    super.onResume()
    bindRecents()
  }
    
  override protected def onRestoreInstanceState(state: Bundle) {
    super.onRestoreInstanceState(state)
    val opened = state.getBoolean(KEY_SAVE_GRID_OPENED, false)
    if (opened)
      showApplications(false)
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(KEY_SAVE_GRID_OPENED, mGrid.getVisibility == View.VISIBLE)
  }

  /**
   * Registers various intent receivers. The current implementation registers
   * only a wallpaper intent receiver to let other applications change the
   * wallpaper.
   */
  private def registerIntentReceivers() {
    var filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
    registerReceiver(mWallpaperReceiver, filter)

    filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED)
    filter addAction Intent.ACTION_PACKAGE_REMOVED
    filter addAction Intent.ACTION_PACKAGE_CHANGED
    filter addDataScheme "package"
    registerReceiver(mApplicationsReceiver, filter)
  }

  /**
   * Creates a new appplications adapter for the grid view and registers it.
   */
  private def bindApplications() {
    if (mGrid == null) {
      mGrid = findViewById(R.id.all_apps).asInstanceOf[GridView]
    }
    mGrid setAdapter new ApplicationsAdapter(this, mApplications.toArray)
    mGrid setSelection 0

    if (mApplicationsStack == null) {
      mApplicationsStack =
        findViewById(R.id.faves_and_recents).asInstanceOf[ApplicationsStackLayout]
    }
  }

  /**
   * Binds actions to the various buttons.
   */
  private def bindButtons() {
    mShowApplications = findViewById(R.id.show_all_apps)
    mShowApplications setOnClickListener new ShowApplications
    mShowApplicationsCheck =
      findViewById(R.id.show_all_apps_check).asInstanceOf[CheckBox]

    mGrid setOnItemClickListener new ApplicationLauncher
  }

  /**
   * When no wallpaper was manually set, a default wallpaper is used instead.
   */
  private def setDefaultWallpaper() {
    if (!mWallpaperChecked) {
      val wallpaper: Drawable = peekWallpaper()
      if (wallpaper == null) {
        try {
          clearWallpaper()
        } catch {
          case e: IOException =>
            Log.e(LOG_TAG, "Failed to clear wallpaper " + e)
        }
      } else {
        getWindow setBackgroundDrawable new ClippedDrawable(wallpaper)
      }
      mWallpaperChecked = true
    }
  }

  /**
   * Refreshes the favorite applications stacked over the all apps button.
   * The number of favorites depends on the user.
   */
  private def bindFavorites(isLaunching: Boolean) {
    if (!isLaunching || mFavorites == null) {

      if (mFavorites == null) {
         mFavorites = new ListBuffer[ApplicationInfo]
      } else {
         mFavorites.clear()
      }
      mApplicationsStack setFavorites mFavorites.toList
            
      val intent = new Intent(Intent.ACTION_MAIN, null)
      intent addCategory Intent.CATEGORY_LAUNCHER

      val packageManager = getPackageManager

      // Environment.getRootDirectory() is a fancy way of saying ANDROID_ROOT or "/system".
      val favFile = new File(Environment.getRootDirectory, DEFAULT_FAVORITES_PATH)
      try {
        val favReader = new FileReader(favFile)

        val parser = Xml.newPullParser
        parser setInput favReader

        beginDocument(parser, TAG_FAVORITES)

        nextElement(parser)
        while (TAG_FAVORITE equals parser.getName) {
          val favoritePackage = parser.getAttributeValue(null, TAG_PACKAGE)
          val favoriteClass = parser.getAttributeValue(null, TAG_CLASS)

          val cn = new ComponentName(favoritePackage, favoriteClass)
          intent setComponent cn
          intent setFlags Intent.FLAG_ACTIVITY_NEW_TASK

          val info = Home.getApplicationInfo(packageManager, intent)
          if (info != null) {
            info.intent = intent
            mFavorites prepend info
          }
          nextElement(parser)
        } //while
      } catch {
        case e: FileNotFoundException =>
          Log.e(LOG_TAG, "Couldn't find or open favorites file " + favFile)
        case e: XmlPullParserException =>
          Log.w(LOG_TAG, "Got exception parsing favorites.", e)
        case e: IOException =>
          Log.w(LOG_TAG, "Got exception parsing favorites.", e)
      }
    }

    mApplicationsStack setFavorites mFavorites.toList
  }

  /**
   * Refreshes the recently launched applications stacked over the favorites.
   * The number of recents depends on how many favorites are present.
   */
  private def bindRecents() {
    val manager = getPackageManager
    val tasksManager =
      getSystemService(ACTIVITY_SERVICE).asInstanceOf[ActivityManager]
    val recentTasks = tasksManager.getRecentTasks(MAX_RECENT_TASKS, 0)

    val count = recentTasks.size()
    val recents = new ArrayBuffer[ApplicationInfo]

    for (i <- count - 1 to 0 by -1) {
      val intent = recentTasks.get(i).baseIntent

      if (Intent.ACTION_MAIN.equals(intent.getAction) &&
          !intent.hasCategory(Intent.CATEGORY_HOME)) {

        val info = Home.getApplicationInfo(manager, intent)
        if (info != null) {
          info.intent = intent
          if (!mFavorites.contains(info)) {
            recents += info
          }
        }
      }
    }

    mApplicationsStack setRecents recents.toList
  }

  override def onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (!hasFocus) {
      mBackDown = mHomeDown == false
    }
  }

  override def dispatchKeyEvent(event: KeyEvent): Boolean =
    event.getAction match {
      case KeyEvent.ACTION_DOWN =>
        event.getKeyCode match {
          case KeyEvent.KEYCODE_BACK =>
            mBackDown = true
            true
          case KeyEvent.KEYCODE_HOME =>
            mHomeDown = true
            true
        }
      case KeyEvent.ACTION_UP =>
        event.getKeyCode match {
          case KeyEvent.KEYCODE_BACK =>
            if (!event.isCanceled()) {
               // Do BACK behavior.
            }
            mBackDown = true
            true
          case KeyEvent.KEYCODE_HOME =>
            if (!event.isCanceled()) {
              // Do HOME behavior.
            }
            mHomeDown = true
            true
        }
      case _ =>
        super.dispatchKeyEvent(event)
    }
    
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    menu.add(0, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
        .setIcon(android.R.drawable.ic_menu_gallery)
        .setAlphabeticShortcut('W')
    menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
        .setIcon(android.R.drawable.ic_search_category_default)
        .setAlphabeticShortcut(SearchManager.MENU_KEY)
    menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
        .setIcon(android.R.drawable.ic_menu_preferences)
        .setIntent(new Intent(android.provider.Settings.ACTION_SETTINGS))

    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case MENU_WALLPAPER_SETTINGS =>
        startWallpaper()
        true
      case MENU_SEARCH =>
        onSearchRequested()
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }

  private def startWallpaper() {
    val pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER)
    startActivity(Intent.createChooser(pickWallpaper, getString(R.string.menu_wallpaper)))
  }

  /**
   * Loads the list of installed applications in mApplications.
   */
  private def loadApplications(isLaunching: Boolean) {
    if (isLaunching && mApplications != null) {
      return
    }

    val manager = getPackageManager

    val mainIntent = new Intent(Intent.ACTION_MAIN, null)
    mainIntent addCategory Intent.CATEGORY_LAUNCHER

    val apps = manager.queryIntentActivities(mainIntent, 0)
    Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager))

    if (apps != null) {
      val count = apps.size()

      if (mApplications == null) {
        mApplications = new ArrayBuffer[ApplicationInfo](count)
      }
      mApplications.clear()

      for (i <- 0 until count) {
        val application = new ApplicationInfo
        val info = apps.get(i)

        application.title = info.loadLabel(manager)
        application.setActivity(new ComponentName(
            info.activityInfo.applicationInfo.packageName,
            info.activityInfo.name),
            Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        application.icon = info.activityInfo loadIcon manager

        mApplications += application
      }
    }
  }

  /**
   * Shows all of the applications by playing an animation on the grid.
   */
  private def showApplications(animate: Boolean) {
    if (mBlockAnimation) {
      return
    }
    mBlockAnimation = true

    mShowApplicationsCheck.toggle()

    if (mShowLayoutAnimation == null) {
      mShowLayoutAnimation =
        AnimationUtils.loadLayoutAnimation(this, R.anim.show_applications)
    }

    // This enables a layout animation; if you uncomment this code, you need
    // to comment the line mGrid.startAnimation() below
//  mGrid.setLayoutAnimationListener(new ShowGrid())
//  mGrid.setLayoutAnimation(mShowLayoutAnimation)
//  mGrid.startLayoutAnimation()

    if (animate) {
      mGridEntry setAnimationListener new ShowGrid
      mGrid startAnimation mGridEntry
    }

    mGrid setVisibility View.VISIBLE

    if (!animate) {
      mBlockAnimation = false
    }

    // ViewDebug.startHierarchyTracing("Home", mGrid);
  }

  /**
   * Hides all of the applications by playing an animation on the grid.
   */
  private def hideApplications() {
    if (mBlockAnimation) {
      return
    }
    mBlockAnimation = true

    mShowApplicationsCheck.toggle()

    if (mHideLayoutAnimation == null) {
      mHideLayoutAnimation =
        AnimationUtils.loadLayoutAnimation(this, R.anim.hide_applications)
    }

    mGridExit setAnimationListener new HideGrid
    mGrid startAnimation mGridExit
    mGrid setVisibility View.INVISIBLE
    mShowApplications.requestFocus()

    // This enables a layout animation; if you uncomment this code, you need to
    // comment the line mGrid.startAnimation() above
//  mGrid.setLayoutAnimationListener(new HideGrid())
//  mGrid.setLayoutAnimation(mHideLayoutAnimation)
//  mGrid.startLayoutAnimation()
  }

  /**
   * Receives intents from other applications to change the wallpaper.
   */
  private class WallpaperIntentReceiver extends BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      getWindow setBackgroundDrawable new ClippedDrawable(getWallpaper)
    }
  }

  /**
   * Receives notifications when applications are added/removed.
   */
  private class ApplicationsIntentReceiver extends BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      loadApplications(false)
      bindApplications()
      bindRecents()
      bindFavorites(false)
    }
  }

  /**
   * GridView adapter to show the list of all installed applications.
   */
  private class ApplicationsAdapter(context: Context, apps: Array[ApplicationInfo])
  extends ArrayAdapter(context, 0, apps) {
    private val mOldBounds = new Rect

    override def getView(position: Int, convertView: View,
                         parent: ViewGroup): View = {
      val info = mApplications(position)

      val convertView1 = if (convertView == null) {
        val inflater = getLayoutInflater
        inflater.inflate(R.layout.application, parent, false)
      } else
        convertView

      var icon = info.icon

      if (!info.filtered) {
        //val resources = getContext.getResources
        var width = 42 //resources.getDimension(android.R.dimen.app_icon_size).toInt
        var height = 42 //resources.getDimension(android.R.dimen.app_icon_size).toInt

        val iconWidth = icon.getIntrinsicWidth
        val iconHeight = icon.getIntrinsicHeight

        if (icon.isInstanceOf[PaintDrawable]) {
          val painter = icon.asInstanceOf[PaintDrawable]
          painter setIntrinsicWidth width
          painter setIntrinsicHeight height
        }

        if (width > 0 && height > 0 && 
            (width < iconWidth || height < iconHeight)) {
          val ratio = (iconWidth / iconHeight).toInt

          if (iconWidth > iconHeight) {
            height = (width / ratio).toInt
          } else if (iconHeight > iconWidth) {
            width = (height * ratio).toInt
          }

          val c = if (icon.getOpacity != PixelFormat.OPAQUE)
            Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
          val thumb = Bitmap.createBitmap(width, height, c)
          val canvas = new Canvas(thumb)
          canvas setDrawFilter new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0)
          // Copy the old bounds to restore them later
          // If we were to do oldBounds = icon.getBounds(),
          // the call to setBounds() that follows would
          // change the same instance and we would lose the
          // old bounds
          mOldBounds set icon.getBounds
          icon.setBounds(0, 0, width, height)
          icon.draw(canvas)
          icon setBounds mOldBounds
          icon = new BitmapDrawable(thumb)
          info.icon = icon
          info.filtered = true
        }
      }

      val textView = convertView1.findViewById(R.id.label).asInstanceOf[TextView]
      textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
      textView setText info.title

      convertView1
    }
  }

  /**
   * Shows and hides the applications grid view.
   */
  private class ShowApplications extends View.OnClickListener {
    def onClick(v: View) {
      if (mGrid.getVisibility() != View.VISIBLE) {
        showApplications(true)
      } else {
        hideApplications()
      }
    }
  }

  /**
   * Hides the applications grid when the layout animation is over.
   */
  private class HideGrid extends Animation.AnimationListener {
    def onAnimationStart(animation: Animation) {
    }

    def onAnimationEnd(animation: Animation) {
      mBlockAnimation = false
    }

    def onAnimationRepeat(animation: Animation) {
    }
  }

  /**
   * Shows the applications grid when the layout animation is over.
   */
  private class ShowGrid extends Animation.AnimationListener {
    def onAnimationStart(animation: Animation) {
    }

    def onAnimationEnd(animation: Animation) {
      mBlockAnimation = false
      // ViewDebug.stopHierarchyTracing()
    }

    def onAnimationRepeat(animation: Animation) {
    }
  }

  /**
   * Starts the selected activity/application in the grid view.
   */
  private class ApplicationLauncher extends AdapterView.OnItemClickListener {
    def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
      val app = parent.getItemAtPosition(position).asInstanceOf[ApplicationInfo]
      startActivity(app.intent)
    }
  }

  /**
   * When a drawable is attached to a View, the View gives the Drawable its
   * dimensions by calling Drawable.setBounds(). In this application, the
   * View that draws the wallpaper has the same size as the screen. However,
   * the wallpaper might be larger that the screen which means it will be
   * automatically stretched. Because stretching a bitmap while drawing it is
   * very expensive, we use a ClippedDrawable instead.
   * This drawable simply draws another wallpaper but makes sure it is not
   *  stretched by always giving it its intrinsic dimensions. If the wallpaper
   * is larger than the screen, it will simply get clipped but it won't impact
   * performance.
   */
  private class ClippedDrawable(wallpaper: Drawable) extends Drawable {
    private val mWallpaper = wallpaper

    override def setBounds(left: Int, top: Int, right: Int, bottom: Int) {
      super.setBounds(left, top, right, bottom)
      // Ensure the wallpaper is as large as it really is, to avoid
      // stretching it at drawing time
      mWallpaper.setBounds(left, top, left + mWallpaper.getIntrinsicWidth,
                           top + mWallpaper.getIntrinsicHeight)
    }

    def draw(canvas: Canvas) {
      mWallpaper.draw(canvas)
    }

    def setAlpha(alpha: Int) {
      mWallpaper setAlpha alpha
    }

    def setColorFilter(cf: ColorFilter) {
      mWallpaper setColorFilter cf
    }

    def getOpacity: Int = {
      mWallpaper.getOpacity
    }
  }
}
