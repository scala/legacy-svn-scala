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

package com.example.android.gesture.builder

import android.app.{Dialog, AlertDialog, ListActivity}
import android.app.Activity._
import android.content.{Context, DialogInterface, Intent}
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.os.{AsyncTask, Bundle, Environment}
import android.view.{ContextMenu, View, MenuItem, LayoutInflater, ViewGroup}
import android.gesture.{Gesture, GestureLibrary, GestureLibraries}
import android.text.TextUtils
import android.widget.{AdapterView, ArrayAdapter, EditText, TextView, Toast}

import java.util.Comparator
import java.io.File

import scala.collection.JavaConversions.{JListWrapper, JSetWrapper}
import scala.collection.mutable.{HashMap, SynchronizedMap}

object GestureBuilderActivity {
  private final val STATUS_SUCCESS = 0
  private final val STATUS_CANCELLED = 1
  private final val STATUS_NO_STORAGE = 2
  private final val STATUS_NOT_LOADED = 3

  private final val MENU_ID_RENAME = 1
  private final val MENU_ID_REMOVE = 2

  private final val DIALOG_RENAME_GESTURE = 1

  private final val REQUEST_NEW_GESTURE = 1
    
  // Type: long (id)
  private final val GESTURES_INFO_ID = "gestures.info_id"

  private final val mStoreFile =
    new File(Environment.getExternalStorageDirectory, "gestures")
  private final val sStore = GestureLibraries.fromFile(mStoreFile)

  def getStore: GestureLibrary = sStore
  
  class NamedGesture(var name: String, var gesture: Gesture)
}

class GestureBuilderActivity extends ListActivity {
  import GestureBuilderActivity._  // companion object

  private final val mSorter = new Comparator[NamedGesture]() {
    def compare(object1: NamedGesture, object2: NamedGesture): Int = {
      object1.name compareTo object2.name
    }
  }

  private var mAdapter: GesturesAdapter = _
  private var mTask: GesturesLoadTask = _
  private var mEmpty: TextView = _

  private var mRenameDialog: Dialog = _
  private var mInput: EditText = _
  private var mCurrentRenameGesture: NamedGesture = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.gestures_list)

    mAdapter = new GesturesAdapter(this)
    setListAdapter(mAdapter)

    mEmpty = findViewById(android.R.id.empty).asInstanceOf[TextView]
    loadGestures()

    registerForContextMenu(getListView)
  }

  //@SuppressWarnings({"UnusedDeclaration"})
  def reloadGestures(v: View) {
    loadGestures()
  }

  //@SuppressWarnings({"UnusedDeclaration"})
  def addGesture(v: View) {
    val intent = new Intent(this, classOf[CreateGestureActivity])
    startActivityForResult(intent, REQUEST_NEW_GESTURE)
  }

  override protected def onActivityResult(requestCode: Int,
                                          resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
        
    if (resultCode == RESULT_OK) {
      requestCode match {
        case REQUEST_NEW_GESTURE =>
          loadGestures()
      }
    }
  }

  private def loadGestures() {
    if (mTask != null && mTask.getStatus != AsyncTask.Status.FINISHED) {
      mTask cancel true
    }        
    mTask = new GesturesLoadTask().execute().asInstanceOf[GesturesLoadTask]
  }

  override protected def onDestroy() {
    super.onDestroy()

    if (mTask != null && mTask.getStatus != AsyncTask.Status.FINISHED) {
      mTask.cancel(true)
      mTask = null
    }

    cleanupRenameDialog()
  }

  private def checkForEmpty() {
    if (mAdapter.getCount == 0) {
      mEmpty setText R.string.gestures_empty
    }
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    if (mCurrentRenameGesture != null) {
      outState.putLong(GESTURES_INFO_ID, mCurrentRenameGesture.gesture.getID)
    }
  }

  override protected def onRestoreInstanceState(state: Bundle) {
    super.onRestoreInstanceState(state)

    val id = state.getLong(GESTURES_INFO_ID, -1)
    if (id != -1) {
      val entries = new JSetWrapper(sStore.getGestureEntries)
      var found = false
      for (name <- entries if !found) {
        val gestures = new JListWrapper(sStore.getGestures(name))
        for (gesture <- gestures if !found) {
          if (gesture.getID == id) {
            mCurrentRenameGesture = new NamedGesture(name, gesture)
            found = true
          }
        }
      }
    }
  }

  override def onCreateContextMenu(menu: ContextMenu, v: View,
                                   menuInfo: ContextMenu.ContextMenuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo)

    val info = menuInfo.asInstanceOf[AdapterView.AdapterContextMenuInfo]
    menu setHeaderTitle info.targetView.asInstanceOf[TextView].getText

    menu.add(0, MENU_ID_RENAME, 0, R.string.gestures_rename)
    menu.add(0, MENU_ID_REMOVE, 0, R.string.gestures_delete)
  }

  override def onContextItemSelected(item: MenuItem): Boolean = {
    val menuInfo =
      item.getMenuInfo.asInstanceOf[AdapterView.AdapterContextMenuInfo]
    val gesture = menuInfo.targetView.getTag.asInstanceOf[NamedGesture]

    item.getItemId match {
      case MENU_ID_RENAME =>
        renameGesture(gesture)
        true
      case MENU_ID_REMOVE =>
        deleteGesture(gesture)
        true
      case _ =>
        super.onContextItemSelected(item)
    }
  }

  private def renameGesture(gesture: NamedGesture) {
    mCurrentRenameGesture = gesture
    showDialog(DIALOG_RENAME_GESTURE)
  }

  override protected def onCreateDialog(id: Int): Dialog = {
    if (id == DIALOG_RENAME_GESTURE)
      createRenameDialog()
    else
      super.onCreateDialog(id)
  }

  override protected def onPrepareDialog(id: Int, dialog: Dialog) {
    super.onPrepareDialog(id, dialog)
    if (id == DIALOG_RENAME_GESTURE) {
      mInput setText mCurrentRenameGesture.name
    }
  }

  private def createRenameDialog(): Dialog = {
    val layout = View.inflate(this, R.layout.dialog_rename, null)
    mInput = layout.findViewById(R.id.name).asInstanceOf[EditText]
    val tv = layout.findViewById(R.id.label).asInstanceOf[TextView]
    tv setText R.string.gestures_rename_label

    val builder = new AlertDialog.Builder(this)
    builder setIcon 0
    builder setTitle getString(R.string.gestures_rename_title)
    builder setCancelable true
    builder setOnCancelListener new /*Dialog*/DialogInterface.OnCancelListener {
      def onCancel(dialog: DialogInterface) {
        cleanupRenameDialog()
      }
    }
    builder.setNegativeButton(getString(R.string.cancel_action),
      new /*Dialog*/DialogInterface.OnClickListener {
        def onClick(dialog: DialogInterface, which: Int) {
          cleanupRenameDialog()
        }
      }
    )
    builder.setPositiveButton(getString(R.string.rename_action),
      new /*Dialog*/DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {
          changeGestureName()
        }
      }
    )
    builder setView layout
    builder.create()
  }

  private def changeGestureName() {
    val name = mInput.getText.toString
    if (!TextUtils.isEmpty(name)) {
      val renameGesture = mCurrentRenameGesture
      val adapter = mAdapter
      val count = adapter.getCount

      // Simple linear search, there should not be enough items to warrant
      // a more sophisticated search
      var found = false
      for (i <- 0 until count if !found) {
        val gesture = adapter.getItem(i)
        if (gesture.gesture.getID == renameGesture.gesture.getID) {
          sStore.removeGesture(gesture.name, gesture.gesture)
          gesture.name = mInput.getText.toString
          sStore.addGesture(gesture.name, gesture.gesture)
          found = true
        }
      }

      adapter.notifyDataSetChanged()
    }
    mCurrentRenameGesture = null
  }

  private def cleanupRenameDialog() {
    if (mRenameDialog != null) {
      mRenameDialog.dismiss()
      mRenameDialog = null
    }
    mCurrentRenameGesture = null
  }

  private def deleteGesture(gesture: NamedGesture) {
    sStore.removeGesture(gesture.name, gesture.gesture)
    sStore.save()

    val adapter = mAdapter
    adapter setNotifyOnChange false
    adapter remove gesture
    adapter sort mSorter
    checkForEmpty()
    adapter.notifyDataSetChanged()

    Toast.makeText(this, R.string.gestures_delete_success,
                   Toast.LENGTH_SHORT).show()
  }

  private class GesturesLoadTask extends AsyncTask[AnyRef, NamedGesture, Int] {
    private var mThumbnailSize: Int = _
    private var mThumbnailInset: Int = _
    private var mPathColor: Int = _

    override protected def onPreExecute() {
      super.onPreExecute()

      val resources = getResources
      mPathColor = resources getColor R.color.gesture_color
      mThumbnailInset =
        resources.getDimension(R.dimen.gesture_thumbnail_inset).toInt
      mThumbnailSize =
        resources.getDimension(R.dimen.gesture_thumbnail_size).toInt

      findViewById(R.id.addButton) setEnabled false
      findViewById(R.id.reloadButton) setEnabled false
            
      mAdapter setNotifyOnChange false           
      mAdapter.clear()
    }

    // temporary workaround (compiler bug !?)
    private def publishProgress1(values: NamedGesture*) {
      super.publishProgress(values: _*)
    }

    override protected def doInBackground(params: AnyRef*): Int = {
      if (isCancelled())
        STATUS_CANCELLED
      else if (! (Environment.MEDIA_MOUNTED equals
                  Environment.getExternalStorageState))
        STATUS_NO_STORAGE
      else if (sStore.load()) {
        val entries = new JSetWrapper(sStore.getGestureEntries)
        for (name <- entries if !isCancelled()) {
          val gestures = new JListWrapper(sStore.getGestures(name))
          for (gesture <- gestures) {
            val bitmap = gesture.toBitmap(mThumbnailSize, mThumbnailSize,
                                          mThumbnailInset, mPathColor)
            val namedGesture = new NamedGesture(name, gesture)

            mAdapter.addBitmap(namedGesture.gesture.getID, bitmap)
            publishProgress1(namedGesture)
          }
        }

        STATUS_SUCCESS
      } else
        STATUS_NOT_LOADED
    }

    override protected def onProgressUpdate(values: NamedGesture*) {
      super.onProgressUpdate(values: _*)

      val adapter = mAdapter
      adapter setNotifyOnChange false

      for (gesture <- values) {
        adapter add gesture
      }

      adapter sort mSorter
     
      adapter.notifyDataSetChanged()
    }

    override protected def onPostExecute(result: Int) {
      super.onPostExecute(result)

      if (result == STATUS_NO_STORAGE) {
        getListView setVisibility View.GONE
        mEmpty setVisibility View.VISIBLE
        mEmpty setText getString(R.string.gestures_error_loading,
                                 mStoreFile.getAbsolutePath)
      } else {
        findViewById(R.id.addButton) setEnabled true
        findViewById(R.id.reloadButton) setEnabled true
        checkForEmpty()
      }
    }
  }

  private class GesturesAdapter(context: Context)
  extends ArrayAdapter[NamedGesture](context, 0) {
    private val mInflater: LayoutInflater =
      context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    private val mThumbnails =
      new HashMap[Long, Drawable] with SynchronizedMap[Long, Drawable]

    def addBitmap(id: Long, bitmap: Bitmap) {
      mThumbnails(id) = new BitmapDrawable(bitmap)
    }

    override def getView(position: Int, convertView: View,
                         parent: ViewGroup): View = {
      val convertView1 = if (convertView == null)
        mInflater.inflate(R.layout.gestures_item, parent, false)
      else
        convertView

      val gesture = getItem(position)
      val label = convertView1.asInstanceOf[TextView]

      label setTag gesture
      label setText gesture.name
      label.setCompoundDrawablesWithIntrinsicBounds(
         mThumbnails(gesture.gesture.getID), null, null, null)

      convertView1
    }
  }
}
