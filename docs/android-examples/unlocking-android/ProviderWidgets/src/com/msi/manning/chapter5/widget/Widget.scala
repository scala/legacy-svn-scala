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

import android.net.Uri
import android.provider.BaseColumns

object Widget extends BaseColumns {

  final val MIME_DIR_PREFIX = "vnd.android.cursor.dir"
  final val MIME_ITEM_PREFIX = "vnd.android.cursor.item"
  final val MIME_ITEM = "vnd.msi.widget"
  final val MIME_TYPE_SINGLE = Widget.MIME_ITEM_PREFIX + "/" + Widget.MIME_ITEM
  final val MIME_TYPE_MULTIPLE = Widget.MIME_DIR_PREFIX + "/" + Widget.MIME_ITEM

  final val AUTHORITY = "com.msi.manning.chapter5.Widget"
  final val PATH_SINGLE = "widgets/#"
  final val PATH_MULTIPLE = "widgets"
  final val CONTENT_URI = Uri.parse("content://" + Widget.AUTHORITY + "/" +
                                                   Widget.PATH_MULTIPLE)

  final val DEFAULT_SORT_ORDER = "updated DESC"

  final val NAME = "name"
  final val TYPE = "type"
  final val CATEGORY = "category"
  final val CREATED = "created"
  final val UPDATED = "updated"
}
