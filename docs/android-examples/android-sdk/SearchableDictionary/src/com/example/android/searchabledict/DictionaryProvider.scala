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

package com.example.android.searchabledict

import android.app.SearchManager
import android.content.{ContentProvider, ContentValues, UriMatcher}
import android.content.res.Resources
import android.database.{Cursor, MatrixCursor}
import android.net.Uri
import android.text.TextUtils

/**
 * Provides search suggestions for a list of words and their definitions.
 */
class DictionaryProvider extends ContentProvider {
  import DictionaryProvider._  // companion object

  override def onCreate(): Boolean = {
    val resources = getContext.getResources
    Dictionary.getInstance ensureLoaded resources
    true
  }

  override def query(uri: Uri, projection: Array[String],
                     selection: String, selectionArgs: Array[String],
                     sortOrder: String): Cursor = {
    if (!TextUtils.isEmpty(selection)) {
      throw new IllegalArgumentException("selection not allowed for " + uri)
    }
    if (selectionArgs != null && selectionArgs.length != 0) {
      throw new IllegalArgumentException("selectionArgs not allowed for " + uri)
    }
    if (!TextUtils.isEmpty(sortOrder)) {
      throw new IllegalArgumentException("sortOrder not allowed for " + uri)
    }
    sURIMatcher `match` uri match {
      case SEARCH_SUGGEST =>
        val query =
          if (uri.getPathSegments.size > 1) uri.getLastPathSegment.toLowerCase
          else null
        getSuggestions(query, projection)
      case SHORTCUT_REFRESH =>
        val shortcutId =
          if (uri.getPathSegments.size > 1) uri.getLastPathSegment
          else null
        refreshShortcut(shortcutId, projection)
      case _ =>
        throw new IllegalArgumentException("Unknown URL " + uri)
    }
  }

  private def getSuggestions(query: String, projection: Array[String]): Cursor = {
    val processedQuery = if (query == null) "" else query.toLowerCase
    val words = Dictionary.getInstance getMatches processedQuery

    val cursor = new MatrixCursor(COLUMNS)
    var id = 0
    for (word <- words) {
      cursor addRow columnValuesOfWord(id, word)
      id += 1
    }

    cursor
  }

  private def columnValuesOfWord(id: Long, word: Dictionary.Word): Array[AnyRef] =
    Array(
      id.asInstanceOf[AnyRef], // _id
      word.word,               // text1
      word.definition,         // text2
      word.word)               // intent_data (included when clicking on item)

  /**
   * Note: this is unused as is, but if we included
   * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our
   * results, we could expect to receive refresh queries on this uri for
   * the id provided, in which case we would return a cursor with a single
   * item representing the refreshed suggestion data.
   */
  private def refreshShortcut(shortcutId: String,
                              projection: Array[String]): Cursor =
    null

  /**
   * All queries for this provider are for the search suggestion and shortcut
   * refresh mime type.
   */
  def getType(uri: Uri): String =
    sURIMatcher `match` uri match {
      case SEARCH_SUGGEST =>
        SearchManager.SUGGEST_MIME_TYPE
      case SHORTCUT_REFRESH =>
        SearchManager.SHORTCUT_MIME_TYPE
      case _ =>
        throw new IllegalArgumentException("Unknown URL " + uri)
    }

  def insert(uri: Uri, values: ContentValues): Uri =
    throw new UnsupportedOperationException()

  def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int =
    throw new UnsupportedOperationException()

  def update(uri: Uri, values: ContentValues,
             selection: String, selectionArgs: Array[String]): Int =
    throw new UnsupportedOperationException()

}

object DictionaryProvider {

  final val AUTHORITY = "dictionary"

  private final val SEARCH_SUGGEST = 0
  private final val SHORTCUT_REFRESH = 1
  private final val sURIMatcher = buildUriMatcher()

  /**
   * The columns we'll include in our search suggestions.  There are others
   * that could be used to further customize the suggestions, see the docs in
   * {@link SearchManager} for the details on additional columns that are
   * supported.
   */
  private final val COLUMNS = Array(
    "_id",  // must include this column
    SearchManager.SUGGEST_COLUMN_TEXT_1,
    SearchManager.SUGGEST_COLUMN_TEXT_2,
    SearchManager.SUGGEST_COLUMN_INTENT_DATA)

  /**
   * Sets up a uri matcher for search suggestion and shortcut refresh queries.
   */
  private def buildUriMatcher(): UriMatcher = {
    val matcher =  new UriMatcher(UriMatcher.NO_MATCH)
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
    matcher
  }
}
