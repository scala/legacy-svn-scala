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

package com.example.android.wiktionary

import com.example.android.wiktionary.SimpleWikiHelper.ApiException
import com.example.android.wiktionary.SimpleWikiHelper.ParseException

import android.app.{Activity, AlertDialog, SearchManager}
import android.content.Intent
import android.net.Uri
import android.os.{AsyncTask, Bundle, SystemClock}
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.{KeyEvent, Menu, MenuInflater, MenuItem, View}
import android.view.animation.{Animation, AnimationUtils}
import android.view.animation.Animation.AnimationListener
import android.webkit.WebView
import android.widget.{ProgressBar, TextView}

import scala.collection.mutable.Stack

/**
 * Activity that lets users browse through Wiktionary content. This is just the
 * user interface, and all API communication and parsing is handled in
 * {@link ExtendedWikiHelper}.
 */
class LookupActivity extends Activity with AnimationListener {
  import LookupActivity._  // companion object

  private var mTitleBar: View = _
  private var mTitle: TextView = _
  private var mProgress: ProgressBar = _
  private var mWebView: WebView = _

  private var mSlideIn: Animation = _
  private var mSlideOut: Animation = _

  /**
   * History stack of previous words browsed in this session. This is
   * referenced when the user taps the "back" key, to possibly intercept and
   * show the last-visited entry, instead of closing the activity.
   */
  private val mHistory = new Stack[String]()

  private var mEntryTitle: String = _

  /**
   * Keep track of last time user tapped "back" hard key. When pressed more
   * than once within {@link #BACK_THRESHOLD}, we treat let the back key fall
   * through and close the app.
   */
  private var mLastPress: Long = -1

  /**
   * {@inheritDoc}
   */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.lookup)

    // Load animations used to show/hide progress bar
    mSlideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in)
    mSlideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out)

    // Listen for the "in" animation so we make the progress bar visible
    // only after the sliding has finished.
    mSlideIn setAnimationListener this

    mTitleBar = findViewById(R.id.title_bar)
    mTitle = findViewById(R.id.title).asInstanceOf[TextView]
    mProgress = findViewById(R.id.progress).asInstanceOf[ProgressBar]
    mWebView = findViewById(R.id.webview).asInstanceOf[WebView]

    // Make the view transparent to show background
    mWebView setBackgroundColor 0

    // Prepare User-Agent string for wiki actions
    ExtendedWikiHelper.prepareUserAgent(this)

    // Handle incoming intents as possible searches or links
    onNewIntent(getIntent)
  }

  /**
   * Intercept the back-key to try walking backwards along our word history
   * stack. If we don't have any remaining history, the key behaves normally
   * and closes this activity.
   */
  override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
    // Handle back key as long we have a history stack
    if (keyCode == KeyEvent.KEYCODE_BACK && !mHistory.isEmpty) {

      // Compare against last pressed time, and if user hit multiple times
      // in quick succession, we should consider bailing out early.
      val currentPress = SystemClock.uptimeMillis
      if (currentPress - mLastPress < BACK_THRESHOLD) {
        return super.onKeyDown(keyCode, event)
      }
      mLastPress = currentPress

      // Pop last entry off stack and start loading
      val lastEntry = mHistory.pop()
      startNavigating(lastEntry, false)

      true
    } else {
      // Otherwise fall through to parent
      super.onKeyDown(keyCode, event)
    }

  /**
   * Start navigating to the given word, pushing any current word onto the
   * history stack if requested. The navigation happens on a background thread
   * and updates the GUI when finished.
   *
   * @param word The dictionary word to navigate to.
   * @param pushHistory If true, push the current word onto history stack.
   */
  private def startNavigating(word: String, pushHistory: Boolean) {
    // Push any current word onto the history stack
    if (!TextUtils.isEmpty(mEntryTitle) && pushHistory) {
      mHistory push mEntryTitle
    }

    // Start lookup for new word in background
    new LookupTask().execute(word)
  }

  /**
   * {@inheritDoc}
   */
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.lookup, menu)
    true
  }

  /**
   * {@inheritDoc}
   */
  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case R.id.lookup_search =>
        onSearchRequested()
        true
      case R.id.lookup_random =>
        startNavigating(null, true)
        true
      case R.id.lookup_about =>
        showAbout()
        true
      case _ =>
        false
    }

  /**
   * Show an about dialog that cites data sources.
   */
  protected def showAbout() {
    // Inflate the about message contents
    val messageView = getLayoutInflater.inflate(R.layout.about, null, false)

    // When linking text, force to always use default color. This works
    // around a pressed color state bug.
    val textView = messageView.findViewById(R.id.about_credits).asInstanceOf[TextView]
    val defaultColor = textView.getTextColors.getDefaultColor
    textView setTextColor defaultColor

    val builder = new AlertDialog.Builder(this)
    builder setIcon R.drawable.app_icon
    builder setTitle R.string.app_name
    builder setView messageView
    builder.create()
    builder.show()
  }

  /**
   * Because we're singleTop, we handle our own new intents. These usually
   * come from the {@link SearchManager} when a search is requested, or from
   * internal links the user clicks on.
   */
  override def onNewIntent(intent: Intent) {
    val action = intent.getAction
    if (Intent.ACTION_SEARCH equals action) {
      // Start query for incoming search request
      val query = intent getStringExtra SearchManager.QUERY
      startNavigating(query, true)
    } else if (Intent.ACTION_VIEW equals action) {
      // Treat as internal link only if valid Uri and host matches
      val data = intent.getData
      if (data != null && ExtendedWikiHelper.WIKI_LOOKUP_HOST
                    .equals(data.getHost)) {
        val query = data.getPathSegments().get(0)
        startNavigating(query, true)
      }
    } else {
      // If not recognized, then start showing random word
      startNavigating(null, true)
    }
  }

  /**
   * Set the title for the current entry.
   */
  protected def setEntryTitle(entryText: String) {
    mEntryTitle = entryText
    mTitle setText mEntryTitle
  }

  /**
   * Set the content for the current entry. This will update our
   * {@link WebView} to show the requested content.
   */
  protected def setEntryContent(entryContent: String) {
    mWebView.loadDataWithBaseURL(ExtendedWikiHelper.WIKI_AUTHORITY, entryContent,
                ExtendedWikiHelper.MIME_TYPE, ExtendedWikiHelper.ENCODING, null)
  }

  /**
   * Background task to handle Wiktionary lookups. This correctly shows and
   * hides the loading animation from the GUI thread before starting a
   * background query to the Wiktionary API. When finished, it transitions
   * back to the GUI thread where it updates with the newly-found entry.
   */
  //private class LookupTask extends AsyncTask[String, String, String] {
  private class LookupTask extends MyAsyncTask {
    /**
     * Before jumping into background thread, start sliding in the
     * {@link ProgressBar}. We'll only show it once the animation finishes.
     */
    override protected def onPreExecute() {
      super.onPreExecute()

      mTitleBar startAnimation mSlideIn
    }

    /**
     * Perform the background query using {@link ExtendedWikiHelper}, which
     * may return an error message as the result.
     */
    //override protected def doInBackground(args: String*): String = {
    protected def doInBackground1(args: Array[String]): String = {
      var query = args(0)
      var parsedText: String = null

      try {
        // If query word is null, assume request for random word
        if (query == null) {
          query = ExtendedWikiHelper.getRandomWord
        }

        if (query != null) {
          // Push our requested word to the title bar
          publishProgress(query)
          val wikiText = ExtendedWikiHelper.getPageContent(query, true)
          parsedText = ExtendedWikiHelper.formatWikiText(wikiText)
        }
      } catch {
        case e: ApiException =>
          Log.e(TAG, "Problem making wiktionary request", e)
        case e: ParseException =>
          Log.e(TAG, "Problem making wiktionary request", e)
      }

      if (parsedText == null) {
        parsedText = getString(R.string.empty_result)
      }

      parsedText
    }

    /**
     * Our progress update pushes a title bar update.
     */
    override protected def onProgressUpdate(args: String*) {
      super.onProgressUpdate(args: _*)

      val searchWord = args(0)
      setEntryTitle(searchWord)
    }

    /**
     * When finished, push the newly-found entry content into our
     * {@link WebView} and hide the {@link ProgressBar}.
     */
    override protected def onPostExecute(parsedText: String) {
      super.onPostExecute(parsedText)

      mTitleBar startAnimation mSlideOut
      mProgress setVisibility View.INVISIBLE

      setEntryContent(parsedText)
    }
  }

  /**
   * Make the {@link ProgressBar} visible when our in-animation finishes.
   */
  def onAnimationEnd(animation: Animation) {
    mProgress setVisibility View.VISIBLE
  }

  def onAnimationRepeat(animation: Animation) {
    // Not interested if the animation repeats
  }

  def onAnimationStart(animation: Animation) {
    // Not interested when the animation starts
  }
}

object LookupActivity {

  private final val TAG = "LookupActivity"

  private final val BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2

}
