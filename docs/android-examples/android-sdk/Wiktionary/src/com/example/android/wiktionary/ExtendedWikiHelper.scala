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

import org.json.{JSONArray, JSONException, JSONObject}

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.webkit.WebView

import java.util.regex.{Matcher, Pattern}

import scala.collection.mutable.HashSet

/**
 * Extended version of {@link SimpleWikiHelper}. This version adds methods to
 * pick a random word, and to format generic wiki-style text into HTML.
 */
object ExtendedWikiHelper { //extends SimpleWikiHelper {
  import SimpleWikiHelper._

  @inline
  def prepareUserAgent(context: Context) =
    SimpleWikiHelper.prepareUserAgent(context)

  @inline
  def getPageContent(title: String, expandTemplates: Boolean) =
    SimpleWikiHelper.getPageContent(title, expandTemplates)

  /**
   * HTML style sheet to include with any {@link #formatWikiText(String)} HTML
   * results. It formats nicely for a mobile screen, and hides some content
   * boxes to keep things tidy.
   */
  private final val STYLE_SHEET =
    "<style>h2 {font-size:1.2em;font-weight:normal;} " +
    "a {color:#6688cc;} ol {padding-left:1.5em;} blockquote {margin-left:0em;} " +
    ".interProject, .noprint {display:none;} " +
    "li, blockquote {margin-top:0.5em;margin-bottom:0.5em;}</style>"

  /**
   * Pattern of section titles we're interested in showing. This trims out
   * extra sections that can clutter things up on a mobile screen.
   */
  private final val sValidSections =
    Pattern.compile("(verb|noun|adjective|pronoun|interjection)",
                    Pattern.CASE_INSENSITIVE)

  /**
   * Pattern that can be used to split a returned wiki page into its various
   * sections. Doesn't treat children sections differently.
   */
  private final val sSectionSplit =
    Pattern.compile("^=+(.+?)=+.+?(?=^=)", Pattern.MULTILINE | Pattern.DOTALL)

  /**
   * When picking random words in {@link #getRandomWord()}, we sometimes
   * encounter special articles or templates. This pattern ignores any words
   * like those, usually because they have ":" or other punctuation.
   */
  private final val sInvalidWord = Pattern.compile("[^A-Za-z0-9 ]")

  /**
   * {@link Uri} authority to use when creating internal links.
   */
  final val WIKI_AUTHORITY = "wiktionary"

  /**
   * {@link Uri} host to use when creating internal links.
   */
  final val WIKI_LOOKUP_HOST = "lookup"

  /**
   * Mime-type to use when showing parsed results in a {@link WebView}.
   */
  final val MIME_TYPE = "text/html"

  /**
   * Encoding to use when showing parsed results in a {@link WebView}.
   */
  final val ENCODING = "utf-8"

  /**
   * {@link Uri} to use when requesting a random page.
   */
  private final val WIKTIONARY_RANDOM =
    "http://en.wiktionary.org/w/api.php?action=query&list=random&format=json"

  /**
   * Fake section to insert at the bottom of a wiki response before parsing.
   * This ensures that {@link #sSectionSplit} will always catch the last
   * section, as it uses section headers in its searching.
   */
  private final val STUB_SECTION = "\n=Stub section="

  /**
   * Number of times to try finding a random word in {@link #getRandomWord()}.
   * These failures are usually when the found word fails the
   * {@link #sInvalidWord} test, or when a network error happens.
   */
  private final val RANDOM_TRIES = 3

  /**
   * Internal class to hold a wiki formatting rule. It's mostly a wrapper to
   * simplify {@link Matcher#replaceAll(String)}.
   *
   * Create a wiki formatting rule.
   *
   * @param pattern Search string to be compiled into a {@link Pattern}.
   * @param replaceWith String to replace any found occurances with. This
   *            string can also include back-references into the given
   *            pattern.
   * @param flags Any flags to compile the {@link Pattern} with.
   */
  private class FormatRule(pattern: String, replaceWith: String, flags: Int) {

    private val mPattern = Pattern.compile(pattern, flags)

    /**
     * Create a wiki formatting rule.
     *
     * @param pattern Search string to be compiled into a {@link Pattern}.
     * @param replaceWith String to replace any found occurances with. This
     *            string can also include back-references into the given
     *            pattern.
     */
    def this(pattern: String, replaceWith: String) =
      this(pattern, replaceWith, 0)

    /**
     * Apply this formatting rule to the given input string, and return the
     * resulting new string.
     */
    def apply(input: String): String = {
      val m = mPattern.matcher(input)
      m.replaceAll(replaceWith)
    }

  }

  /**
   * List of internal formatting rules to apply when parsing wiki text. These
   * include indenting various bullets, apply italic and bold styles, and
   * adding internal linking.
   */
  private final val sFormatRules = List(
    // Format header blocks and wrap outside content in ordered list
    new FormatRule("^=+(.+?)=+", "</ol><h2>$1</h2><ol>", Pattern.MULTILINE),

    // Indent quoted blocks, handle ordered and bullet lists
    new FormatRule("^#+\\*?:(.+?)$", "<blockquote>$1</blockquote>", Pattern.MULTILINE),
    new FormatRule("^#+:?\\*(.+?)$", "<ul><li>$1</li></ul>", Pattern.MULTILINE),
    new FormatRule("^#+(.+?)$", "<li>$1</li>", Pattern.MULTILINE),

    // Add internal links
    new FormatRule("\\[\\[([^:\\|\\]]+)\\]\\]",
                   String.format("<a href=\"%s://%s/$1\">$1</a>",
                   WIKI_AUTHORITY, WIKI_LOOKUP_HOST)),
    new FormatRule("\\[\\[([^:\\|\\]]+)\\|([^\\]]+)\\]\\]",
                   String.format("<a href=\"%s://%s/$1\">$2</a>",
                   WIKI_AUTHORITY, WIKI_LOOKUP_HOST)),

    // Add bold and italic formatting
    new FormatRule("'''(.+?)'''", "<b>$1</b>"),
    new FormatRule("([^'])''([^'].*?[^'])''([^'])", "$1<i>$2</i>$3"),

    // Remove odd category links and convert remaining links into flat text
    new FormatRule("(\\{+.+?\\}+|\\[\\[[^:]+:[^\\\\|\\]]+\\]\\]|" +
                   "\\[http.+?\\]|\\[\\[Category:.+?\\]\\])", "",
                   Pattern.MULTILINE | Pattern.DOTALL),
    new FormatRule("\\[\\[([^\\|\\]]+\\|)?(.+?)\\]\\]", "$2", Pattern.MULTILINE)
  )

  /**
   * Query the Wiktionary API to pick a random dictionary word. Will try
   * multiple times to find a valid word before giving up.
   *
   * @return Random dictionary word, or null if no valid word was found.
   * @throws ApiException If any connection or server error occurs.
   * @throws ParseException If there are problems parsing the response.
   */
  @throws(classOf[ApiException])
  @throws(classOf[ParseException])
  def getRandomWord: String = {
    // Keep trying a few times until we find a valid word
    for (tries <- 0 until RANDOM_TRIES) {
      // Query the API for a random word
      val content = getUrlContent(WIKTIONARY_RANDOM)
      try {
        // Drill into the JSON response to find the returned word
        val response = new JSONObject(content)
        val query = response getJSONObject "query"
        val random = query getJSONArray "random"
        val word = random getJSONObject 0
        val foundWord = word getString "title"

        // If we found an actual word, and it wasn't rejected by our invalid
        // filter, then accept and return it.
        if (foundWord != null &&
             !sInvalidWord.matcher(foundWord).find()) {
          return foundWord
        }
      } catch {
        case e: JSONException =>
          throw new ParseException("Problem parsing API response", e)
      }
    }

    // No valid word found in number of tries, so return null
    null
  }

  /**
   * Format the given wiki-style text into formatted HTML content. This will
   * create headers, lists, internal links, and style formatting for any wiki
   * markup found.
   *
   * @param wikiText The raw text to format, with wiki-markup included.
   * @return HTML formatted content, ready for display in {@link WebView}.
   */
  def formatWikiText(wikiText0: String): String = {
    if (wikiText0 == null) {
      return null
    }

    // Insert a fake last section into the document so our section splitter
    // can correctly catch the last section.
    var wikiText = wikiText0 concat STUB_SECTION

    // Read through all sections, keeping only those matching our filter,
    // and only including the first entry for each title.
    val foundSections = new HashSet[String]()
    val builder = new StringBuilder()

    val sectionMatcher = sSectionSplit matcher wikiText
    while (sectionMatcher.find()) {
      val title = sectionMatcher group 1
      if (!foundSections.contains(title) &&
          sValidSections.matcher(title).matches()) {
        val sectionContent = sectionMatcher.group()
        foundSections add title
        builder append sectionContent
      }
    }

    // Our new wiki text is the selected sections only
    wikiText = builder.toString

    // Apply all formatting rules, in order, to the wiki text
    for (rule <- sFormatRules) {
      wikiText = rule apply wikiText
    }

    // Return the resulting HTML with style sheet, if we have content left
    if (!TextUtils.isEmpty(wikiText)) {
      STYLE_SHEET + wikiText
    } else {
      null
    }
  }

}
