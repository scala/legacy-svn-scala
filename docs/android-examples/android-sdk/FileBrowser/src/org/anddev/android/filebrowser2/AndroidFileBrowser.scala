/*
 * Copyright 2007 Steven Osborn
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
package org.anddev.android.filebrowser2

import java.io.File
import java.util.Collections

import scala.collection.mutable.ArrayBuffer

import android.app.{AlertDialog, ListActivity}
import android.content.{DialogInterface, Intent}
import android.content.DialogInterface.{OnCancelListener, OnClickListener}
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ListView

import iconifiedlist.{IconifiedText, IconifiedTextListAdapter}

/**
 *  Based on plusminus's tutorial on anddev.org
 *  (http://www.anddev.org/android_filebrowser__v20-t101.html)
 */
class AndroidFileBrowser extends ListActivity {
     
  private object DISPLAYMODE extends Enumeration {
    val ABSOLUTE, RELATIVE = Value
  }
  import DISPLAYMODE._

  private val displayMode = ABSOLUTE
  private val directoryEntries = new ArrayBuffer[IconifiedText]
  private var currentDirectory = new File("/")

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    browseToRoot()
  }

  /**
   * This function browses to the
   * root-directory of the file-system.
   */
  private def browseToRoot() {
    browseTo(new File("/"))
  }

  /**
   * This function browses up one level
   * according to the field: currentDirectory
   */
  private def upOneLevel() {
    if (currentDirectory.getParent != null)
      browseTo(currentDirectory.getParentFile)
  }

  private def browseTo(aDirectory: File) {
    // On relative we display the full path in the title.
    if (displayMode == RELATIVE)
      setTitle(aDirectory.getAbsolutePath + " :: " +
               getString(R.string.app_name))
    if (aDirectory.isDirectory) {
      currentDirectory = aDirectory
      fill(aDirectory.listFiles)
    } else {
      val okButtonListener = new OnClickListener() {
        // @Override
        def onClick(arg0: DialogInterface, arg1: Int) {
          // Lets start an intent to View the file, that was clicked...
          openFile(aDirectory)
        }
      }
      val cancelButtonListener = new OnClickListener() {
        // @Override
        def onClick(arg0: DialogInterface, arg1: Int) {
          // Do nothing
        }
      }
      val cancelListener = new OnCancelListener {
        def onCancel(arg0: DialogInterface) {
          // Do nothing
        }
      }
      val dialog = new AlertDialog.Builder(this).create()
      dialog.show()
/*
AlertDialog.show(
        this,
        "Question", "Do you want to open that file?\n" + aDirectory.getName(),
        "OK", okButtonListener,
        "Cancel", cancelButtonListener,
        false, null
      )
*/
    }
  }

  private def openFile(aFile: File) {
    try {
      val myIntent = new Intent(
        android.content.Intent.ACTION_VIEW,
        Uri.parse("file://" + aFile.getAbsolutePath))
      startActivity(myIntent)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  } 

  private final def getDrawable(id: Int) = getResources.getDrawable(id)

  private def fill(files: Array[File]) {
    directoryEntries.clear()

    // Add the "." == "current directory"
    directoryEntries +=
      new IconifiedText(
        getString(R.string.current_dir),
        getDrawable(R.drawable.folder))
    // and the ".." == 'Up one level'
    if (currentDirectory.getParent != null)
      directoryEntries +=
        new IconifiedText(
          getString(R.string.up_one_level),
          getDrawable(R.drawable.uponelevel))

    val endingImage = getResources.getStringArray(R.array.fileEndingImage)
    val endingWeb = getResources.getStringArray(R.array.fileEndingWebText)
    val endingPackage = getResources.getStringArray(R.array.fileEndingPackage)
    val endingAudio = getResources.getStringArray(R.array.fileEndingAudio)

    var currentIcon: Drawable = null
    for (currentFile <- files) {
      if (currentFile.isDirectory)
        currentIcon = getDrawable(R.drawable.folder)
      else {
        val fileName = currentFile.getName()
        /* Determine the Icon to be used,
         * depending on the FileEndings defined in:
         * res/values/fileendings.xml. */
        currentIcon =
          if (checkEndsWithInStringArray(fileName, endingImage))
            getDrawable(R.drawable.image)
          else if (checkEndsWithInStringArray(fileName, endingWeb))
            getDrawable(R.drawable.webtext)
          else if (checkEndsWithInStringArray(fileName, endingPackage))
            getDrawable(R.drawable.packed)
          else if (checkEndsWithInStringArray(fileName, endingAudio))
            getDrawable(R.drawable.audio)
          else
            getDrawable(R.drawable.text)                 
      }
      displayMode match {
        case ABSOLUTE =>
          /* On absolute Mode, we show the full path */
          directoryEntries += new IconifiedText(currentFile.getPath, currentIcon)
        case RELATIVE =>
          /* On relative Mode, we have to cut the
           * current-path at the beginning */
          val currentPathStringLength = currentDirectory.getAbsolutePath.length
          directoryEntries += new IconifiedText(
                                   currentFile.getAbsolutePath.
                                   substring(currentPathStringLength),
                                   currentIcon)
      }
    }
    directoryEntries sortWith ((x, y) => x.compareTo(y) < 0)

    val itla = new IconifiedTextListAdapter(this)
    itla setListItems directoryEntries      
    setListAdapter(itla)
  }

  protected override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    super.onListItemClick(l, v, position, id)
    val selectedItemId = getSelectedItemId.toInt
    val selectedFileString = directoryEntries(selectedItemId).getText
    if (selectedFileString equals getString(R.string.current_dir)) {
      // Refresh
      browseTo(currentDirectory)
    } else if (selectedFileString equals getString(R.string.up_one_level)) {
      upOneLevel()
    } else {
      val clickedFile = new File(
        displayMode match {
          case RELATIVE =>
            currentDirectory.getAbsolutePath() + directoryEntries(selectedItemId).getText
          case ABSOLUTE =>
            directoryEntries(selectedItemId).getText
        }
      )
      if (clickedFile != null)
        browseTo(clickedFile)
    }
  }

  /** Checks whether checkItsEnd ends with
   * one of the Strings from fileEndings */
  private def checkEndsWithInStringArray(checkItsEnd: String,
                         fileEndings: Array[String]): Boolean = {
    for (aEnd <- fileEndings) {
      if (checkItsEnd endsWith aEnd)
        return true
    }
    false
  }
}
