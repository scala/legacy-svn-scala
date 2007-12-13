/* 
 * Copyright (C) 2007 Google Inc.
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

package com.google.android.notepad

import com.google.provider.NotePad

import _root_.android.content.{ContentProvider, ContentProviderDatabaseHelper,
                               ContentURIParser, ContentValues, QueryBuilder,
                               Resources}
import _root_.android.database.{Cursor, SQLException}
import _root_.android.database.sqlite.SQLiteDatabase
import _root_.android.net.ContentURI
import _root_.android.text.TextUtils
import _root_.android.util.Log

import java.util.HashMap // note: setProjectionMap expects a Java map

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 *
 */
object NotePadProvider {
  private val TAG = "NotePadProvider"
  private val DATABASE_NAME = "note_pad.db"
  private val DATABASE_VERSION = 2

  private val NOTES_LIST_PROJECTION_MAP = new HashMap/*[String, String]*/()
  NOTES_LIST_PROJECTION_MAP.put(NotePad.Notes._ID, "_id")
  NOTES_LIST_PROJECTION_MAP.put(NotePad.Notes.TITLE, "title")
  NOTES_LIST_PROJECTION_MAP.put(NotePad.Notes.NOTE, "note")
  NOTES_LIST_PROJECTION_MAP.put(NotePad.Notes.CREATED_DATE, "created")
  NOTES_LIST_PROJECTION_MAP.put(NotePad.Notes.MODIFIED_DATE, "modified")

  private val NOTES = 1
  private val NOTE_ID = 2

  private val URL_MATCHER = new ContentURIParser(ContentURIParser.NO_MATCH)
  URL_MATCHER.addURI("com.google.provider.NotePad", "notes", NOTES)
  URL_MATCHER.addURI("com.google.provider.NotePad", "notes/#", NOTE_ID)

  private class DatabaseHelper extends ContentProviderDatabaseHelper {
    override def onCreate(db: SQLiteDatabase) {
      db execSQL "CREATE TABLE notes (_id INTEGER PRIMARY KEY," +
                 "title TEXT," + "note TEXT," + "created INTEGER," +
                 "modified INTEGER" + ");"
    }

    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to " +
                 newVersion + ", which will destroy all old data")
      db execSQL "DROP TABLE IF EXISTS notes"
      onCreate(db)
    }
  }
}

class NotePadProvider extends ContentProvider {
  import NotePadProvider._

  private var mDB: SQLiteDatabase = _

  override def onCreate(): Boolean = {
    val dbHelper = new DatabaseHelper()
    mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null, DATABASE_VERSION)
    mDB != null
  }

  override def query(url: ContentURI, projection: Array[String],
                     selection: String, selectionArgs: Array[String],
                     groupBy: String, having: String, sort: String): Cursor = {
    val qb = new QueryBuilder()

    (URL_MATCHER `match` url) match {
      case NOTES =>
        qb setTables "notes" 
        qb setProjectionMap NOTES_LIST_PROJECTION_MAP

       case NOTE_ID =>
         qb setTables "notes"
         qb appendWhere ("_id=" + url.getPathSegment(1))

       case _ =>
         throw new IllegalArgumentException("Unknown URL " + url)
    }

    // If no sort order is specified use the default
    val orderBy =
      if (TextUtils isEmpty sort)
        NotePad.Notes.DEFAULT_SORT_ORDER
      else
        sort

    val c = qb.query(mDB, projection, selection, selectionArgs, groupBy,
                having, orderBy)
    c.setNotificationUri(getContext().getContentResolver(), url);
    c
  }

  override def getType(url: ContentURI): String = (URL_MATCHER `match` url) match {
    case NOTES =>
      "vnd.android.cursor.dir/vnd.google.note"
    case NOTE_ID =>
      "vnd.android.cursor.item/vnd.google.note"
    case _ =>
      throw new IllegalArgumentException("Unknown URL " + url)
  }

  override def insert(url: ContentURI, initialValues: ContentValues): ContentURI = {
    val values =
      if (initialValues != null)
        new ContentValues(initialValues)
      else
        new ContentValues()

    if ((URL_MATCHER `match` url) != NOTES)
      throw new IllegalArgumentException("Unknown URL " + url)

    val now = System.currentTimeMillis()
    val r = Resources.getSystem()

    // Make sure that the fields are all set
    if (! values.containsKey(NotePad.Notes.CREATED_DATE))
      values.put(NotePad.Notes.CREATED_DATE, now)

    if (! values.containsKey(NotePad.Notes.MODIFIED_DATE))
      values.put(NotePad.Notes.MODIFIED_DATE, now)

    if (! values.containsKey(NotePad.Notes.TITLE))
      values.put(NotePad.Notes.TITLE, r.getString(_root_.android.R.string.untitled))

    if (! values.containsKey(NotePad.Notes.NOTE))
      values.put(NotePad.Notes.NOTE, "")

    val rowID = mDB.insert("notes", "note", values)
    if (rowID > 0) {
      val uri = NotePad.Notes.CONTENT_URI addId rowID
      getContext().getContentResolver().notifyChange(uri, null)
      uri
    } else
      throw new SQLException("Failed to insert row into " + url)
  }

  override def delete(url: ContentURI, where: String, whereArgs: Array[String]): Int = {
    val count = (URL_MATCHER `match` url) match {
      case NOTES =>
        mDB.delete("note_pad", where, whereArgs)

      case NOTE_ID =>
        val segment = url.getPathSegment(1)
        //rowId = Long.parseLong(segment);
        mDB.delete("notes", "_id=" +
                   segment +
                   (if (!TextUtils.isEmpty(where)) " AND (" + where + ')' else ""),
                   whereArgs)

      case _ =>
        throw new IllegalArgumentException("Unknown URL " + url)
    }

    getContext().getContentResolver().notifyChange(url, null)
    count
  }

  override def update(url: ContentURI, values: ContentValues,
                      where: String, whereArgs: Array[String]): Int = {
    val count = (URL_MATCHER `match` url) match {
      case NOTES =>
        mDB.update("notes", values, where, whereArgs)

      case NOTE_ID =>
        val segment = url getPathSegment 1
        mDB.update("notes", values, "_id=" +
                   segment +
                   (if (!TextUtils.isEmpty(where)) " AND (" + where + ')'
                    else ""),
                   whereArgs)

      case _ =>
        throw new IllegalArgumentException("Unknown URL " + url)
    }

    getContext().getContentResolver().notifyChange(url, null)
    count
  }

}
