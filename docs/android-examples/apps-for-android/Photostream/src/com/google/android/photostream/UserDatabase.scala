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

import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}
import android.content.{Context, ContentValues}
import android.util.Log
import android.graphics.{Bitmap, BitmapFactory}
import android.provider.BaseColumns

import java.io.{ByteArrayOutputStream, IOException}

/**
 * Helper class to interact with the database that stores the Flickr contacts.
 */
class UserDatabase(mContext: Context)
extends SQLiteOpenHelper(mContext, UserDatabase.DATABASE_NAME, null,
                         UserDatabase.DATABASE_VERSION) {
//with BaseColumns {
  import UserDatabase._  // companion object

  override def onCreate(db: SQLiteDatabase) {
    db.execSQL("CREATE TABLE users ("
                + "_id INTEGER PRIMARY KEY, "
                + "username TEXT, "
                + "realname TEXT, "
                + "nsid TEXT, "
                + "buddy_icon BLOB,"
                + "last_update INTEGER);");

    addUser(db, "Bob Lee", "Bob Lee", "45701389@N00",
            R.drawable.boblee_buddyicon)
    addUser(db, "ericktseng", "Erick Tseng", "76701017@N00",
            R.drawable.ericktseng_buddyicon)
    addUser(db, "romainguy", "Romain Guy", "24046097@N00",
            R.drawable.romainguy_buddyicon)
  }

  private def addUser(db: SQLiteDatabase, userName: String, realName: String,
                      nsid: String, icon: Int) {

    val values = new ContentValues()
    values.put(COLUMN_USERNAME, userName)
    values.put(COLUMN_REALNAME, realName)
    values.put(COLUMN_NSID, nsid)
    values.put(COLUMN_LAST_UPDATE, System.currentTimeMillis.toDouble)

    val bitmap = BitmapFactory.decodeResource(mContext.getResources, icon)
    writeBitmap(values, COLUMN_BUDDY_ICON, bitmap)

    db.insert(TABLE_USERS, COLUMN_LAST_UPDATE, values)
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    Log.w(Flickr.LOG_TAG, "Upgrading database from version " + oldVersion +
          " to " + newVersion + ", which will destroy all old data")

    db execSQL "DROP TABLE IF EXISTS users"
    onCreate(db)
  }

}

object UserDatabase {

  final val _ID = BaseColumns._ID

  private final val DATABASE_NAME = "flickr"
  private final val DATABASE_VERSION = 1

  final val TABLE_USERS = "users"
  final val COLUMN_USERNAME = "username"
  final val COLUMN_REALNAME = "realname"
  final val COLUMN_NSID = "nsid"
  final val COLUMN_BUDDY_ICON = "buddy_icon"
  final val COLUMN_LAST_UPDATE = "last_update"

  final val SORT_DEFAULT = COLUMN_USERNAME + " ASC"

  def writeBitmap(values: ContentValues, name: String, bitmap: Bitmap) {
    if (bitmap != null) {
      // Try go guesstimate how much space the icon will take when serialized
      // to avoid unnecessary allocations/copies during the write.
      val size = bitmap.getWidth * bitmap.getHeight * 2
      val out = new ByteArrayOutputStream(size)
      try {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()

        values.put(name, out.toByteArray)
      } catch {
        case e: IOException => // Ignore
      }
    }
  }

}

