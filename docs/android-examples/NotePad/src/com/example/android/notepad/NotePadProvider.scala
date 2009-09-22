package com.example.android.notepad

import com.example.android.notepad.NotePad.Notes

import _root_.android.content.{ContentProvider, ContentUris, ContentValues, Context}
import _root_.android.content.UriMatcher
import _root_.android.content.res.Resources
import _root_.android.database.{Cursor, SQLException}
import _root_.android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper, SQLiteQueryBuilder}
import _root_.android.net.Uri
import _root_.android.text.TextUtils
import _root_.android.util.Log

import java.util.HashMap // note: setProjectionMap expects a Java map

object NotePadProvider {

  private final val TAG = "NotePadProvider"

  private final val DATABASE_NAME = "note_pad.db"
  private final val DATABASE_VERSION = 2
  private final val NOTES_TABLE_NAME = "notes"

  private val sNotesProjectionMap = new HashMap[String, String]()
  sNotesProjectionMap.put(Notes._ID, Notes._ID)
  sNotesProjectionMap.put(Notes.TITLE, Notes.TITLE)
  sNotesProjectionMap.put(Notes.NOTE, Notes.NOTE)
  sNotesProjectionMap.put(Notes.CREATED_DATE, Notes.CREATED_DATE)
  sNotesProjectionMap.put(Notes.MODIFIED_DATE, Notes.MODIFIED_DATE)

  private final val NOTES = 1
  private final val NOTE_ID = 2

  private final val sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH)
  sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES)
  sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID)

  /** This class helps open, create, and upgrade the database file.
   */
  private class DatabaseHelper(context: Context)
  extends SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override def onCreate(db: SQLiteDatabase) {
      db.execSQL("CREATE TABLE " + NOTES_TABLE_NAME + " ("
               + Notes._ID + " INTEGER PRIMARY KEY,"
               + Notes.TITLE + " TEXT,"
               + Notes.NOTE + " TEXT,"
               + Notes.CREATED_DATE + " INTEGER,"
               + Notes.MODIFIED_DATE + " INTEGER"
               + ");")
    }

    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
              + newVersion + ", which will destroy all old data")
      db execSQL "DROP TABLE IF EXISTS notes"
      onCreate(db)
    }
  }
}

/** Provides access to a database of notes. Each note has a title, the note
 *  itself, a creation date and a modified data.
 */
class NotePadProvider extends ContentProvider {
  import NotePadProvider._ // companion object

  private var mOpenHelper: DatabaseHelper = _

  override def onCreate(): Boolean = {
    mOpenHelper = new DatabaseHelper(getContext)
    true
  }

  override def query(uri: Uri, projection: Array[String],
                     selection: String, selectionArgs: Array[String],
                     sortOrder: String): Cursor = {
    val qb = new SQLiteQueryBuilder

    sUriMatcher `match` uri match {
      case NOTES =>
        qb setTables NOTES_TABLE_NAME
        qb setProjectionMap sNotesProjectionMap

      case NOTE_ID =>
        qb setTables NOTES_TABLE_NAME
        qb setProjectionMap sNotesProjectionMap
       qb.appendWhere(Notes._ID + "=" + uri.getPathSegments().get(1))

      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }

    // If no sort order is specified use the default
    val orderBy =
      if (TextUtils.isEmpty(sortOrder)) NotePad.Notes.DEFAULT_SORT_ORDER
      else sortOrder

    // Get the database and run the query
    val db = mOpenHelper.getReadableDatabase
    val c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy)

    // Tell the cursor what uri to watch, so it knows when its source data changes
    c.setNotificationUri(getContext.getContentResolver, uri)
    c
  }

  override def getType(uri: Uri): String =
    sUriMatcher `match` uri match {
      case NOTES   => Notes.CONTENT_TYPE
      case NOTE_ID => Notes.CONTENT_ITEM_TYPE
      case _       => throw new IllegalArgumentException("Unknown URI " + uri)
    }

  override def insert(uri: Uri, initialValues: ContentValues): Uri = {
    // Validate the requested uri
    if ((sUriMatcher `match` uri) != NOTES)
       throw new IllegalArgumentException("Unknown URI " + uri)

    val values =
      if (initialValues != null) new ContentValues(initialValues)
      else new ContentValues()

    val now = System.currentTimeMillis

    // Make sure that the fields are all set
    if (! (values containsKey NotePad.Notes.CREATED_DATE))
      values.put(NotePad.Notes.CREATED_DATE, now)

    if (! (values containsKey NotePad.Notes.MODIFIED_DATE))
      values.put(NotePad.Notes.MODIFIED_DATE, now)

    if (! (values containsKey NotePad.Notes.TITLE)) {
      val r = Resources.getSystem()
      values.put(NotePad.Notes.TITLE, r.getString(_root_.android.R.string.untitled))
    }

    if (! (values containsKey NotePad.Notes.NOTE))
      values.put(NotePad.Notes.NOTE, "")

    val db = mOpenHelper.getWritableDatabase
    val rowId = db.insert(NOTES_TABLE_NAME, Notes.NOTE, values)
    if (rowId > 0) {
      val noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, rowId)
      getContext.getContentResolver.notifyChange(noteUri, null)
      noteUri
    } else
      throw new SQLException("Failed to insert row into " + uri)
  }

  override def delete(uri: Uri,
                      where: String, whereArgs: Array[String]): Int = {
    val db = mOpenHelper.getWritableDatabase
    val count = sUriMatcher `match` uri match {
      case NOTES =>
        db.delete(NOTES_TABLE_NAME, where, whereArgs)

      case NOTE_ID =>
        val noteId = uri.getPathSegments.get(1)
        db.delete(NOTES_TABLE_NAME, Notes._ID + "=" + noteId
                + (if (TextUtils isEmpty where) "" else " AND (" + where + ')'), whereArgs)

      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }

    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  override def update(uri: Uri, values: ContentValues,
                      where: String, whereArgs: Array[String]): Int = {
    val db = mOpenHelper.getWritableDatabase
    val count = sUriMatcher `match` uri match {
      case NOTES =>
        db.update(NOTES_TABLE_NAME, values, where, whereArgs)

      case NOTE_ID =>
        val noteId = uri.getPathSegments().get(1)
        db.update(NOTES_TABLE_NAME, values, Notes._ID + "=" + noteId
               + (if (TextUtils isEmpty where) "" else " AND (" + where + ')'), whereArgs)

      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }

    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

}
