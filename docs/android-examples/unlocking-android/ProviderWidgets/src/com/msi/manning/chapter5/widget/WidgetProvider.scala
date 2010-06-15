/*
 * Copyright (C) 2009 Manning Publications Co.
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

package com.msi.manning.chapter5.widget

import android.content.{ContentProvider, ContentUris, ContentValues,
                        Context, UriMatcher}
import android.database.{Cursor, SQLException}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper, SQLiteQueryBuilder}
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log

import java.util.HashMap;

class WidgetProvider extends ContentProvider {
  import WidgetProvider._  // companion object

  private var db: SQLiteDatabase = _

  override def onCreate(): Boolean = {
    val dbHelper = new DBOpenHelper(getContext)
    db = dbHelper.getWritableDatabase
    db != null
  }

  /**
   * Per Android JavaDoc:
   * 
   * Return the MIME type of the data at the given URI. This should start with
   * vnd.android.cursor.item/ for a single record, or vnd.android.cursor.dir/
   * for multiple items.
   * 
   * (Though this is NOT how the samples provided by Google work.)
   * 
   * (non-Javadoc)
   * 
    * @see android.content.ContentProvider#getType(android.net.Uri)
   */
  override def getType(uri: Uri): String =
    URI_MATCHER `match` uri match {
      case WIDGETS =>
        Widget.MIME_TYPE_MULTIPLE
      case WIDGET =>
        Widget.MIME_TYPE_SINGLE
      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }

  override def query(uri: Uri, projection: Array[String], selection: String,
                    selectionArgs: Array[String], sortOrder: String): Cursor = {
    val queryBuilder = new SQLiteQueryBuilder()

    URI_MATCHER `match` uri match {
      case WIDGETS =>
        queryBuilder setTables DB_TABLE
        queryBuilder setProjectionMap PROJECTION_MAP
      case WIDGET =>
        queryBuilder setTables DB_TABLE
        queryBuilder.appendWhere("_id=" + uri.getPathSegments.get(1))
      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }

    val orderBy =
      if (TextUtils.isEmpty(sortOrder)) Widget.DEFAULT_SORT_ORDER;
      else sortOrder

    val c: Cursor = queryBuilder.query(db, projection, selection,
                                       selectionArgs, null, null, orderBy)
    c.setNotificationUri(getContext.getContentResolver, uri)
    c
  }

  override def insert(uri: Uri, initialValues: ContentValues): Uri = {
    var rowId = 0L
    val values =
      if (initialValues != null) new ContentValues(initialValues)
      else new ContentValues()

    if (URI_MATCHER.`match`(uri) != WIDGETS) {
      throw new IllegalArgumentException("Unknown URI " + uri)
    }

    val now = System.currentTimeMillis

    // default fields if not set
    if (!values.containsKey(Widget.NAME)) {
      values.put(Widget.NAME, "NA")
    }
    if (!values.containsKey(Widget.TYPE)) {
      values.put(Widget.NAME, "NA")
    }
    if (!values.containsKey(Widget.CATEGORY)) {
      values.put(Widget.NAME, "NA")
    }
    if (!values.containsKey(Widget.CREATED)) {
      values.put(Widget.NAME, Predef.float2Float(now))
    }
    if (!values.containsKey(Widget.UPDATED)) {
      values.put(Widget.UPDATED, Predef.float2Float(0))
    }

    rowId = db.insert(DB_TABLE, "widget_hack", values)

    if (rowId > 0) {
      val result = ContentUris.withAppendedId(Widget.CONTENT_URI, rowId)
      getContext.getContentResolver.notifyChange(result, null)
      result
    }
    else
      throw new SQLException("Failed to insert row into " + uri)
  }

  override def update(uri: Uri, values: ContentValues, selection: String,
                      selectionArgs: Array[String]): Int = {
    var count = 0
    URI_MATCHER `match` uri match {
      case WIDGETS =>
        count = db.update(DB_TABLE, values, selection, selectionArgs)
      case WIDGET =>
        val segment = uri.getPathSegments.get(1)
        val where =
          if (!TextUtils.isEmpty(selection)) " AND (" + selection + ")"
          else ""
        count = db.update(DB_TABLE, values, "_id=" + segment + where, selectionArgs)
      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  override def delete(uri: Uri, selection: String, 
                      selectionArgs: Array[String]): Int = {
    var count = 0
    URI_MATCHER `match` uri match {
      case WIDGETS =>
        count = db.delete(DB_TABLE, selection, selectionArgs)
      case WIDGET =>
        val segment = uri.getPathSegments.get(1)
        val where =
          if (!TextUtils.isEmpty(selection)) " AND (" + selection + ")"
          else ""
        count = db.delete(DB_TABLE, "_id=" + segment + where, selectionArgs)
      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }
}

object WidgetProvider {

  private final val CLASSNAME = classOf[WidgetProvider].getSimpleName
  private final val WIDGETS = 1
  private final val WIDGET = 2

  final val DB_NAME = "widgets_db"
  final val DB_TABLE = "widget"
  final val DB_VERSION = 1

  private final val URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH)
  URI_MATCHER.addURI(Widget.AUTHORITY, Widget.PATH_MULTIPLE, WidgetProvider.WIDGETS)
  URI_MATCHER.addURI(Widget.AUTHORITY, Widget.PATH_SINGLE, WidgetProvider.WIDGET)

  private final val PROJECTION_MAP = new HashMap[String, String]()
  PROJECTION_MAP.put(BaseColumns._ID, "_id")
  PROJECTION_MAP.put(Widget.NAME, "name")
  PROJECTION_MAP.put(Widget.TYPE, "type")
  PROJECTION_MAP.put(Widget.CATEGORY, "category")
  PROJECTION_MAP.put(Widget.CREATED, "created")
  PROJECTION_MAP.put(Widget.UPDATED, "updated")

  private class DBOpenHelper(context: Context)
  extends SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    import DBOpenHelper._  // companion object

    override def onCreate(db: SQLiteDatabase) {
      Log.v(Constants.LOGTAG, CLASSNAME + " OpenHelper Creating database")
      try {
        db execSQL DBOpenHelper.DB_CREATE
      } catch {
        case e: SQLException =>
          Log.e(Constants.LOGTAG, CLASSNAME, e)
      }
    }

    override def onOpen(db: SQLiteDatabase) {
      super.onOpen(db)
      Log.v(Constants.LOGTAG, CLASSNAME + " OpenHelper Opening database")
    }

    override def onUpgrade(db: SQLiteDatabase,
                           oldVersion: Int, newVersion: Int) {
      Log.w(Constants.LOGTAG, CLASSNAME +
            " OpenHelper Upgrading database from version " + oldVersion +
            " to " + newVersion + " all data will be clobbered")
      db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE)
      onCreate(db)
    }
  }

  private object DBOpenHelper {
    private final val DB_CREATE = "CREATE TABLE " + DB_TABLE +
                                  " (_id INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL, type TEXT, category TEXT, updated INTEGER, created INTEGER);"
    // private final val DB_UPDATE = ""
  }

}
