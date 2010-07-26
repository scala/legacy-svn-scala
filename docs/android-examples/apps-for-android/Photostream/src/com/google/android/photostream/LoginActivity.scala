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
import scala.collection.mutable.HashMap

import android.content.{Context, ContentValues, Intent}
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.{ContextMenu, View, ViewGroup, KeyEvent, LayoutInflater,
                     Menu, MenuItem}
import android.view.animation.{Animation, AnimationUtils}
import android.widget.{AdapterView, CursorAdapter, ProgressBar, ListView, TextView}

/**
 * Activity used to login the user. The activity asks for the user name and
 * then add the user to the users list upong successful login. If the login is
 * unsuccessful, an error message is displayed. Clicking any stored user
 * launches PhotostreamActivity.
 *
 * This activity is also used to create Home shortcuts. When the intent
 * {@link Intent#ACTION_CREATE_SHORTCUT} is used to start this activity,
 * sucessful login returns a shortcut Intent to Home instead of proceeding
 * to PhotostreamActivity.
 *
 * The shortcut Intent contains the real name of the user, his buddy icon, the action
 * {@link android.content.Intent#ACTION_VIEW} and the URI flickr://photos/nsid.
 */
class LoginActivity extends Activity with View.OnKeyListener
                                     with AdapterView.OnItemClickListener {
  import LoginActivity._  // companion object

  private var mCreateShortcut: Boolean = _

  private var mUsername: TextView = _
  private var mProgress: ProgressBar = _

  private var mDatabase: SQLiteDatabase = _
  private var mAdapter: UsersAdapter = _

  private var mTask: UserTask[String, Nothing, Flickr.User] = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    schedule()

    // If the activity was started with the "create shortcut" action, we
    // remember this to change the behavior upon successful login
    if (Intent.ACTION_CREATE_SHORTCUT equals getIntent.getAction) {
      mCreateShortcut = true
    }

    mDatabase = new UserDatabase(this).getWritableDatabase

    setContentView(R.layout.screen_login)
    setupViews()
  }

  private def schedule() {
    //val preferences = getSharedPreferences(Preferences.NAME, MODE_PRIVATE)
    //if (!preferences.getBoolean(Preferences.KEY_ALARM_SCHEDULED, false)) {
    CheckUpdateService.schedule(this)
    //    preferences.edit().putBoolean(Preferences.KEY_ALARM_SCHEDULED, true).commit()
    //}
  }

  private def setupViews() {
    mUsername = findViewById(R.id.input_username).asInstanceOf[TextView]
    mUsername setOnKeyListener this
    mUsername.requestFocus()

    mAdapter = new UsersAdapter(this, initializeCursor())

    val userList = findViewById(R.id.list_users).asInstanceOf[ListView]
    userList setAdapter mAdapter
    userList setOnItemClickListener this

    registerForContextMenu(userList)

    mProgress = findViewById(R.id.progress).asInstanceOf[ProgressBar]
  }

  private def initializeCursor(): Cursor = {
    val cursor = mDatabase.query(
      UserDatabase.TABLE_USERS,
      Array(UserDatabase._ID, UserDatabase.COLUMN_REALNAME,
            UserDatabase.COLUMN_NSID, UserDatabase.COLUMN_BUDDY_ICON),
      null, null, null, null, UserDatabase.SORT_DEFAULT)

    startManagingCursor(cursor)

    cursor
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.login, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean =
    item.getItemId match {
      case R.id.menu_item_settings =>
        SettingsActivity.show(this)
        true
      case R.id.menu_item_info =>
        Eula.showDisclaimer(this)
        true
      case _ =>
        super.onMenuItemSelected(featureId, item)
    }

  def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
    if (event.getAction == KeyEvent.ACTION_UP) {
      v.getId match {
        case R.id.input_username =>
          if (keyCode == KeyEvent.KEYCODE_ENTER ||
              keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            onAddUser(mUsername.getText.toString)
            return true
          }
        case _ =>
      }
    }
    false
  }

  def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
    val user = Flickr.User.fromId((view.getTag.asInstanceOf[UserDescription]).nsid)

    if (!mCreateShortcut) {
      onShowPhotostream(user)
    } else {
      onCreateShortcut(user)
    }
  }

  override def onCreateContextMenu(menu: ContextMenu, view: View,
                                   menuInfo: ContextMenu.ContextMenuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo)

    val info = menuInfo.asInstanceOf[AdapterView.AdapterContextMenuInfo]
    menu setHeaderTitle info.targetView.asInstanceOf[TextView].getText

    menu.add(0, MENU_ID_SHOW, 0, R.string.context_menu_show_photostream)
    menu.add(0, MENU_ID_DELETE, 0, R.string.context_menu_delete_user)
  }

  override def onContextItemSelected(item: MenuItem): Boolean = {
    val info = item.getMenuInfo.asInstanceOf[AdapterView.AdapterContextMenuInfo]
    val description = info.targetView.getTag.asInstanceOf[UserDescription]

    item.getItemId match {
      case MENU_ID_SHOW =>
        val user = Flickr.User.fromId(description.nsid)
        onShowPhotostream(user)
        true
      case MENU_ID_DELETE =>
        onRemoveUser(description.id)
        true
      case _ =>
        super.onContextItemSelected(item)
    }
  }

  override protected def onResume() {
    super.onResume()

    if (mProgress.getVisibility == View.VISIBLE) {
      mProgress setVisibility View.GONE
    }
  }

  override protected def onDestroy() {
    super.onDestroy()

    if (mTask != null && mTask.getStatus == UserTask.Status.RUNNING) {
      mTask cancel true
    }

    mAdapter.cleanup()
    mDatabase.close()
  }

  private def onAddUser(username: String) {
    // When the user enters his user name, we need to find his NSID before
    // adding it to the list.
    mTask = new FindUserTask().execute(username)
  }

  private def onRemoveUser(id: String) {
    val rows = mDatabase.delete(UserDatabase.TABLE_USERS,
                                UserDatabase._ID + "=?", Array(id))
    if (rows > 0) {
      mAdapter.refresh()
    }
  }

  private def onError() {
    hideProgress()
    mUsername setError getString(R.string.screen_login_error)
  }

  private def hideProgress() {
    if (mProgress.getVisibility != View.GONE) {
      val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
      mProgress setVisibility View.GONE
      mProgress startAnimation fadeOut
    }
  }

  private def showProgress() {
    if (mProgress.getVisibility != View.VISIBLE) {
     val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
     mProgress setVisibility View.VISIBLE
     mProgress startAnimation fadeIn
    }
  }

  private def onShowPhotostream(user: Flickr.User) {
    PhotostreamActivity.show(this, user)
  }

  /**
   * Creates the shortcut Intent to send back to Home. The intent is a view
   * action to a flickr://photos/nsid URI, with a title (real name or user
   * name) and a custom icon (the user's buddy icon.)
   *
   * @param user The user to create a shortcut for.
   */
  private def onCreateShortcut(user: Flickr.User) {
    val cursor: Cursor = mDatabase.query(UserDatabase.TABLE_USERS,
                Array(UserDatabase.COLUMN_REALNAME, UserDatabase.COLUMN_USERNAME,
                UserDatabase.COLUMN_BUDDY_ICON), UserDatabase.COLUMN_NSID + "=?",
                Array(user.id), null, null, UserDatabase.SORT_DEFAULT)
    cursor.moveToFirst()

    val shortcutIntent = new Intent(PhotostreamActivity.ACTION)
    shortcutIntent setFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    shortcutIntent.putExtra(PhotostreamActivity.EXTRA_NSID, user.id)

    // Sets the custom shortcut's title to the real name of the user. If no
    // real name was found, use the user name instead.
    val intent = new Intent()
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
    var name = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_REALNAME))
    if (name == null || name.length == 0) {
      name = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_USERNAME))
    }
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name)

    // Sets the custom shortcut icon to the user's buddy icon. If no buddy
    // icon was found, use a default local buddy icon instead.
    val data =
      cursor getBlob cursor.getColumnIndexOrThrow(UserDatabase.COLUMN_BUDDY_ICON)
    val icon = BitmapFactory.decodeByteArray(data, 0, data.length)
    if (icon != null) {
      intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
    } else {
      val buddyIcon = Intent.ShortcutIconResource
        .fromContext(this, R.drawable.default_buddyicon)
      intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, buddyIcon)
    }

    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  /**
   * Background task used to load the user's NSID. The task begins by showing the
   * progress bar, then loads the user NSID from the network and finally open
   * PhotostreamActivity.
   */
  private class FindUserTask extends UserTask[String, Nothing, Flickr.User] {

    override def onPreExecute() {
      showProgress()
    }

    def  doInBackground(params: String*): Flickr.User = {
      val name = params(0).trim()
      if (name.length == 0) return null

      val user = Flickr.get findByUserName name
      if (isCancelled || user == null) return null

      val info = Flickr.get getUserInfo user
      if (isCancelled || info == null) return null

      var realname = info.realName
      if (realname == null) realname = name

      val values = new ContentValues()
      values.put(UserDatabase.COLUMN_USERNAME, name)
      values.put(UserDatabase.COLUMN_REALNAME, realname)
      values.put(UserDatabase.COLUMN_NSID, user.id)
      values.put(UserDatabase.COLUMN_LAST_UPDATE, System.currentTimeMillis.toDouble)
      UserDatabase.writeBitmap(values, UserDatabase.COLUMN_BUDDY_ICON,
                    info.loadBuddyIcon())

      var result = -1L
      if (!isCancelled) {
         result = mDatabase.insert(UserDatabase.TABLE_USERS,
                        UserDatabase.COLUMN_REALNAME, values)
      }

      if (result != -1) user else null
    }

    override def onPostExecute(user: Flickr.User) {
      if (user == null) {
        onError()
      } else {
        mAdapter.refresh()
        hideProgress()
      }
    }

  } //class FindUserTask

  private class UsersAdapter(context: Context, cursor: Cursor)
  extends CursorAdapter(context, cursor, true) {
    private final val mInflater = LayoutInflater.from(context)
    private final val mRealname =
      cursor getColumnIndexOrThrow UserDatabase.COLUMN_REALNAME
    private final val mId =
      cursor getColumnIndexOrThrow UserDatabase._ID
    private final val mNsid =
      cursor getColumnIndexOrThrow UserDatabase.COLUMN_NSID
    private final val mBuddyIcon =
      cursor getColumnIndexOrThrow UserDatabase.COLUMN_BUDDY_ICON
    private final val mDefaultIcon =
      context.getResources.getDrawable(R.drawable.default_buddyicon)
    private final val mIcons = new HashMap[String, Drawable]()

    def newView(context: Context, cursor: Cursor, parent: ViewGroup): View = {
      val view = mInflater.inflate(R.layout.list_item_user, parent, false)
      val description = new UserDescription()
      view setTag description
      view
    }

    def bindView(view: View, context: Context, cursor: Cursor) {
      val description = view.getTag.asInstanceOf[UserDescription]
      description.id = cursor getString mId
      description.nsid = cursor getString mNsid

      val textView = view.asInstanceOf[TextView]
      textView setText cursor.getString(mRealname)

      val icon = mIcons get description.nsid match {
        case Some(icon) => icon
        case None =>
          val data = cursor getBlob mBuddyIcon

          var bitmap: Bitmap = null
          if (data != null)
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length)

          val icon =
            if (bitmap != null) new FastBitmapDrawable(bitmap)
            else mDefaultIcon

          mIcons += description.nsid -> icon
          icon
      }

      textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
    }

    def cleanup() {
      for (icon <- mIcons.values) {
        icon setCallback null
      }
    }

    def refresh() {
      getCursor.requery()
    }
  }

}

object LoginActivity {

  private final val MENU_ID_SHOW = 1
  private final val MENU_ID_DELETE = 2

  private class UserDescription {
    var id: String = _
    var nsid: String = _
    }

}
