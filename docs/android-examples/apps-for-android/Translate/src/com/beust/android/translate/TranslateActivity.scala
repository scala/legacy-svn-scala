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

package com.beust.android.translate

import com.beust.android.translate.Languages.Language

import android.app.{Activity, AlertDialog, Dialog}
import android.content.{ComponentName, Context, DialogInterface, Intent,
                        ServiceConnection, SharedPreferences}
import android.content.SharedPreferences.Editor
import android.database.Cursor
import android.net.Uri
import android.os.{Bundle, Handler, IBinder}
import android.provider.Contacts
import android.text.TextUtils
import android.util.Log
import android.view.{Menu, MenuInflater, MenuItem, View}
import android.view.View.OnClickListener
import android.widget.{Button, EditText, ProgressBar, TextView}

import java.io.{BufferedReader, FileNotFoundException, IOException}
import java.util.zip.GZIPInputStream

import scala.collection.mutable.ListBuffer

/**
 * Main activity for the Translate application.
 *
 * @author Cedric Beust
 * @author Daniel Rall
 */
class TranslateActivity extends Activity with OnClickListener {
  import TranslateActivity._  // companion object

  private var mToEditText: EditText = _
  private var mFromEditText: EditText = _
  private var mFromButton: Button = _
  private var mToButton: Button = _
  private var mTranslateButton: Button = _
  private var mSwapButton: Button = _
  private val mHandler = new Handler()
  private var mProgressBar: ProgressBar = _
  private var mStatusView: TextView = _
    
  // true if changing a language should automatically trigger a translation
  private var mDoTranslate = true

  private var mLatestButton: Button = _

  private val mClickListener = new OnClickListener() {
    def onClick(v: View) {
      mLatestButton = v.asInstanceOf[Button]
      showDialog(LANGUAGE_DIALOG_ID)
    }
  }

  // Translation service handle.
  private var mTranslateService: ITranslate = _

  // ServiceConnection implementation for translation.
  private val mTranslateConn = new ServiceConnection() {
    def onServiceConnected(name: ComponentName, service: IBinder) {
      mTranslateService = ITranslate.Stub.asInterface(service)
      /* TODO(dlr): Register a callback to assure we don't lose our svc.
      try {
        mTranslateervice.registerCallback(mTranslateCallback)
      } catch (RemoteException e) {
        log("Failed to establish Translate service connection: " + e)
        return
      }
      */
      if (mTranslateService != null) {
        mTranslateButton setEnabled true
      } else {
        mTranslateButton setEnabled false
        mStatusView setText getString(R.string.error)
        log("Unable to acquire TranslateService")
      }
    }

    def onServiceDisconnected(name: ComponentName) {
      mTranslateButton setEnabled false
      mTranslateService = null
    }
  }

  override protected def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    setContentView(R.layout.translate_activity)
    mFromEditText = findViewById(R.id.input).asInstanceOf[EditText]
    mToEditText = findViewById(R.id.translation).asInstanceOf[EditText]
    mFromButton = findViewById(R.id.from).asInstanceOf[Button]
    mToButton = findViewById(R.id.to).asInstanceOf[Button]
    mTranslateButton = findViewById(R.id.button_translate).asInstanceOf[Button]
    mSwapButton = findViewById(R.id.button_swap).asInstanceOf[Button]
    mProgressBar = findViewById(R.id.progress_bar).asInstanceOf[ProgressBar]
    mStatusView = findViewById(R.id.status).asInstanceOf[TextView]
        
    //
    // Install the language adapters on both the From and To spinners.
    //
    mFromButton setOnClickListener mClickListener
    mToButton setOnClickListener mClickListener

    mTranslateButton setOnClickListener this
    mSwapButton setOnClickListener this
    mFromEditText.selectAll()

    connectToTranslateService()
  }
    
  private def connectToTranslateService() {
    val intent = new Intent(Intent.ACTION_VIEW)
    bindService(intent, mTranslateConn, Context.BIND_AUTO_CREATE)
  }

  override def onResume() {
    super.onResume()
    val prefs = getPrefs(this)
    mDoTranslate = false

    //
    // See if we have any saved preference for From
    //
    val from = Language.findLanguageByShortName(prefs.getString(FROM, DEFAULT_FROM))
    updateButton(mFromButton, from, false /* don't translate */)

    //
    // See if we have any saved preference for To
    //
    //
    val to = Language.findLanguageByShortName(prefs.getString(TO, DEFAULT_TO))
    updateButton(mToButton, to, true /* translate */)

    //
    // Restore input and output, if any
    //
    mFromEditText setText prefs.getString(INPUT, "")
    setOutputText(prefs.getString(OUTPUT, ""))
    mDoTranslate = true
  }

  private def setOutputText(string: String) {
    log("Setting output to " + string)
    mToEditText setText new Entities().unescape(string)
  }

  private def updateButton(button: Button, language: Language, translate: Boolean) {
    language.configureButton(this, button)
    if (translate) maybeTranslate()
  }

  /**
   * Launch the translation if the input text field is not empty.
   */
  private def maybeTranslate() {
    if (mDoTranslate && !TextUtils.isEmpty(mFromEditText.getText.toString)) {
      doTranslate()
    }
  }
    
  override def onPause() {
    super.onPause()

    //
    // Save the content of our views to the shared preferences
    //
    val edit = getPrefs(this).edit()
    val f = mFromButton.getTag.asInstanceOf[Language].shortName
    val t = mToButton.getTag.asInstanceOf[Language].shortName
    val input = mFromEditText.getText.toString
    val output = mToEditText.getText.toString
    savePreferences(edit, f, t, input, output)
  }
    
  override protected def onDestroy() {
    super.onDestroy()
    unbindService(mTranslateConn)
  }

  def onClick(v: View) {
    if (v == mTranslateButton) {
      maybeTranslate()
    } else if (v == mSwapButton) {
      val newFrom = mToButton.getTag.asInstanceOf[Language]
      val newTo = mFromButton.getTag.asInstanceOf[Language]
      mFromEditText setText mToEditText.getText
      mToEditText setText ""
      setNewLanguage(newFrom, true /* from */, false /* don't translate */)
      setNewLanguage(newTo, false /* to */, true /* translate */)
      mFromEditText.requestFocus()
      mStatusView setText R.string.languages_swapped
    }
  }

  private def doTranslate() {
    mStatusView setText R.string.retrieving_translation
    mHandler post new Runnable() {
      def run() {
        mProgressBar setVisibility View.VISIBLE
        var result = ""
        try {
          val from = mFromButton.getTag.asInstanceOf[Language]
          val to = mToButton.getTag.asInstanceOf[Language]
          val fromShortName = from.shortName
          val toShortName = to.shortName
          val input = mFromEditText.getText.toString
          log("Translating from " + fromShortName + " to " + toShortName)
          result = mTranslateService.translate(input, fromShortName, toShortName)
          if (result == null) {
            throw new Exception(getString(R.string.translation_failed))
          }
          History.addHistoryRecord(TranslateActivity.this, from, to, input, result)
          mStatusView setText R.string.found_translation
          setOutputText(result)
          mProgressBar setVisibility View.INVISIBLE
          mFromEditText.selectAll()
        } catch {
          case e: Exception =>
            mProgressBar setVisibility View.INVISIBLE
            mStatusView.setText("Error:" + e.getMessage)
        }
      }
    }
  }

  override protected def onPrepareDialog(id: Int, d: Dialog) {
    if (id == LANGUAGE_DIALOG_ID) {
      val from = mLatestButton == mFromButton
      d.asInstanceOf[LanguageDialog] setFrom from
    }
  }

  override protected def onCreateDialog(id: Int): Dialog =
    if (id == LANGUAGE_DIALOG_ID) {
      new LanguageDialog(this)
    } else if (id == ABOUT_DIALOG_ID) {
      val builder = new AlertDialog.Builder(this)
      builder setTitle R.string.about_title
      builder setMessage getString(R.string.about_message)
      builder setIcon R.drawable.babelfish
      builder.setPositiveButton(android.R.string.ok, null)
      builder.setNeutralButton(R.string.send_email,
        new android.content.DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which: Int) {
            val intent = new Intent(Intent.ACTION_SENDTO)
            intent setData Uri.parse("mailto:cedric@beust.com")
            startActivity(intent)
          }
        })
      builder setCancelable true
      builder.create()
    } else
      null
    
  /**
   * Pick a random word and set it as the input word.
   */
  def selectRandomWord() {
    var fr: BufferedReader = null
    try {
      val is = new GZIPInputStream(getResources openRawResource R.raw.dictionary)
      if (mWordBuffer == null) {
        mWordBuffer = new Array[Byte](601000)
        var n = is.read(mWordBuffer, 0, mWordBuffer.length)
        var current = n
        while (n != -1) {
          n = is.read(mWordBuffer, current, mWordBuffer.length - current)
          current += n
        }
        is.close()
        mWordCount = 0
        mWordIndices = new ListBuffer[Int]()
        for (i <- 0 until mWordBuffer.length) {
          if (mWordBuffer(i) == '\n') {
            mWordCount += 1
            mWordIndices += i
          }
        }
        log("Found " + mWordCount + " words")
      }

      val randomWordIndex = (System.currentTimeMillis % (mWordCount - 1)).toInt
      log("Random word index:" + randomWordIndex + " wordCount:" + mWordCount)
      val start = mWordIndices(randomWordIndex)
      val end = mWordIndices(randomWordIndex + 1)
      val b = new Array[Byte](end - start - 2)
      System.arraycopy(mWordBuffer, start + 1, b, 0, (end - start - 2))
      val randomWord = new String(b)
      mFromEditText setText randomWord
      updateButton(mFromButton,
                   Language.findLanguageByShortName(Language.ENGLISH.shortName),
                   true /* translate */);
    } catch {
      case e: FileNotFoundException =>
        Log.e(TAG, e.getMessage, e)
      case e: IOException =>
        Log.e(TAG, e.getMessage, e)
    }
  }

  def setNewLanguage(language: Language, from: Boolean, translate: Boolean) {
    updateButton(if (from) mFromButton else mToButton, language, translate)
  }
    
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.translate_activity_menu, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.about =>
        showDialog(ABOUT_DIALOG_ID)

      case R.id.show_history =>
        showHistory()

      case R.id.random_word =>
        selectRandomWord()

      // We shouldn't need this menu item but because of a bug in 1.0,
      // neither SMS nor Email filter on the ACTION_SEND intent.  Since they
      // won't be shown in the activity chooser,
      // I need to make an explicit menu for SMS
      case R.id.send_with_sms =>
        val i = new Intent(Intent.ACTION_PICK, Contacts.Phones.CONTENT_URI)
        val intent = new Intent(Intent.ACTION_PICK)
        intent setType Contacts.Phones.CONTENT_TYPE
        startActivityForResult(intent, 42 /* not used */)

      case R.id.send_with_email =>
        val intent = new Intent(Intent.ACTION_SEND)
        intent setType "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, mToEditText.getText)
        startActivity(Intent.createChooser(intent, null))

      case _ =>
    }
    true
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int,
                                          resultIntent: Intent) {
    if (resultIntent != null) {
      val contactURI: Uri = resultIntent.getData
            
      val cursor = getContentResolver.query(contactURI,
                     Array(Contacts.PhonesColumns.NUMBER), 
                     null, null, null)
      if (cursor.moveToFirst()) {
        val phone = cursor.getString(0)
        val intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto://" + phone))
        intent.putExtra("sms_body", mToEditText.getText.toString)
        startActivity(intent)
      }
    }
  }
    
  private def showHistory() {
    startActivity(new Intent(this, classOf[HistoryActivity]))
  }

}

object TranslateActivity {

  final val TAG = "Translate"

  // Dialog id's
  private final val LANGUAGE_DIALOG_ID = 1
  private final val ABOUT_DIALOG_ID = 2

  // Saved preferences
  private final val FROM = "from"
  private final val TO = "to"
  private final val INPUT = "input"
  private final val OUTPUT = "output"

  // Default language pair if no saved preferences are found
  private final val DEFAULT_FROM = Language.ENGLISH.shortName
  private final val DEFAULT_TO = Language.GERMAN.shortName

  // Dictionary
  private var mWordBuffer: Array[Byte] = _
  private var mWordCount: Int = _
  private var mWordIndices: ListBuffer[Int] = _

  def savePreferences(edit: Editor, from: String, to: String,
                     input: String, output: String) {
    log("Saving preferences " + from + " " + to + " " + input + " " + output)
    edit.putString(FROM, from)
    edit.putString(TO, to)
    edit.putString(INPUT, input)
    edit.putString(OUTPUT, output)
    edit.commit()
  }

  def getPrefs(context: Context): SharedPreferences =
    context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

  private def log(s: String) {
    Log.d(TAG, "[TranslateActivity] " + s)
  }

}
