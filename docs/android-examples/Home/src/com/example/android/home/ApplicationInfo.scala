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

package com.example.android.home

import android.content.{ComponentName, Intent}
import android.graphics.drawable.Drawable

/**
 * Represents a launchable application. An application is made of a name
 * (or title), an intent and an icon.
 */
class ApplicationInfo {
  /**
   * The application name.
   */
  var title: CharSequence = _

  /**
   * The intent used to start the application.
   */
  var intent: Intent = _

  /**
   * The application icon.
   */
  var icon: Drawable = _

  /**
    * When set to true, indicates that the icon has been resized.
   */
  var filtered: Boolean = _

  /**
   * Creates the application intent based on a component name and
   * various launch flags.
   *
   * @param className the class name of the component representing the intent
   * @param launchFlags the launch flags
   */
  final def setActivity(className: ComponentName, launchFlags: Int) {
    intent = new Intent(Intent.ACTION_MAIN)
    intent addCategory Intent.CATEGORY_LAUNCHER
    intent setComponent className
    intent setFlags launchFlags
  }

  override def equals(o: Any): Boolean =
    if (this == o)
      true
    else o match {
      case that: ApplicationInfo =>
        (title equals that.title) &&
        (intent.getComponent.getClassName equals
         that.intent.getComponent.getClassName)
      case _ =>
        false
    }

  override def hashCode(): Int = {
    var result = if (title != null) title.hashCode() else 0
    val name = intent.getComponent.getClassName
    31 * result + (if (name != null) name.hashCode() else 0)
  }
}
