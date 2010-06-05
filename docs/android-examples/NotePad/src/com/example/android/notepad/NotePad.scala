/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.notepad

import android.net.Uri
import android.provider.BaseColumns

/** Convenience definitions for <code>NotePadProvider</code>.
 */
object NotePad {

  final val AUTHORITY = "com.google.provider.NotePad"

  /** Notes table
   */
  object Notes {

    val _ID = BaseColumns._ID

    val _COUNT = BaseColumns._COUNT

    /** The content:// style URL for this table
     */
    final val CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notes")

    /** The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    final val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note"

    /** The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    final val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note"

    /** The default sort order for this table
     */
    final val DEFAULT_SORT_ORDER = "modified DESC"

    /** The title of the note
     *  <P>Type: TEXT</P>
     */
    final val TITLE = "title"

    /** The note itself
     *  <P>Type: TEXT</P>
     */
    final val NOTE = "note"

    /** The timestamp for when the note was created
     *  <P>Type: INTEGER (long from System.currentTimeMillis())</P>
     */
    final val CREATED_DATE = "created"

    /** The timestamp for when the note was last modified
     *  <P>Type: INTEGER (long from System.currentTimeMillis())</P>
     */
    final val MODIFIED_DATE = "modified"
  }

}
