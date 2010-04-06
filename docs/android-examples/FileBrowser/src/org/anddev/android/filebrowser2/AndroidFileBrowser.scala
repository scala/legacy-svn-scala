package org.anddev.android.filebrowser2

import java.io.File
import java.util.Collections

import scala.collection.jcl.ArrayList

import _root_.android.app.{AlertDialog, ListActivity}
import _root_.android.content.{DialogInterface, Intent}
import _root_.android.content.DialogInterface.{OnCancelListener, OnClickListener}
import _root_.android.graphics.drawable.Drawable
import _root_.android.net.Uri
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.widget.ListView

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
  private val directoryEntries = new ArrayList[IconifiedText]()
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
        _root_.android.content.Intent.ACTION_VIEW,
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
    directoryEntries.add(
      new IconifiedText(
        getString(R.string.current_dir),
        getDrawable(R.drawable.folder)))
    // and the ".." == 'Up one level'
    if (currentDirectory.getParent != null)
      directoryEntries.add(
        new IconifiedText(
          getString(R.string.up_one_level),
          getDrawable(R.drawable.uponelevel)))

    var currentIcon: Drawable = null
    for (currentFile <- files) {
      if (currentFile.isDirectory)
        currentIcon = getDrawable(R.drawable.folder)
      else {
        val fileName = currentFile.getName();
        /* Determine the Icon to be used,
         * depending on the FileEndings defined in:
         * res/values/fileendings.xml. */
        if (checkEndsWithInStringArray(fileName, getResources().
                                       getStringArray(R.array.fileEndingImage))) {
          currentIcon = getDrawable(R.drawable.image)
        } else if (checkEndsWithInStringArray(fileName, getResources.
                                        getStringArray(R.array.fileEndingWebText))) {
          currentIcon = getDrawable(R.drawable.webtext)
        } else if (checkEndsWithInStringArray(fileName, getResources.
                                        getStringArray(R.array.fileEndingPackage))) {
          currentIcon = getDrawable(R.drawable.packed)
        } else if (checkEndsWithInStringArray(fileName, getResources.
                                        getStringArray(R.array.fileEndingAudio))){
          currentIcon = getDrawable(R.drawable.audio)
        } else {
          currentIcon = getDrawable(R.drawable.text)
        }                   
      }
      displayMode match {
        case ABSOLUTE =>
          /* On absolute Mode, we show the full path */
          directoryEntries.add(new IconifiedText(currentFile.getPath, currentIcon))
        case RELATIVE =>
          /* On relative Mode, we have to cut the
           * current-path at the beginning */
          val currentPathStringLength = currentDirectory.getAbsolutePath.length
          directoryEntries.add(new IconifiedText(
                                   currentFile.getAbsolutePath.
                                   substring(currentPathStringLength),
                                   currentIcon))
      }
    }
    Collections.sort(directoryEntries.underlying)
          
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
