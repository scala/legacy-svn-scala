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

package com.google.provider

import _root_.android.net.ContentURI
import _root_.android.provider.BaseColumns

/**
 * Convenience definitions for NotePadProvider
 */
object NotePad {
  /**
   * Notes table
   */
  object Notes {
  
    val _ID = BaseColumns._ID

    val _COUNT = BaseColumns._COUNT
    /**
     * The content:// style URL for this table
     */
    val CONTENT_URI =
      ContentURI.create("content://com.google.provider.NotePad/notes")

    /**
     * The default sort order for this table
     */
    val DEFAULT_SORT_ORDER = "modified DESC"

    /**
     * The title of the note
     * <P>Type: TEXT</P>
     */
    val TITLE = "title"

    /**
     * The note itself
     * <P>Type: TEXT</P>
     */
    val NOTE = "note"

    /**
     * The timestamp for when the note was created
     * <P>Type: INTEGER (long)</P>
     */
    val CREATED_DATE = "created"

    /**
     * The timestamp for when the note was last modified
     * <P>Type: INTEGER (long)</P>
     */
    val MODIFIED_DATE = "modified"
  }
}
