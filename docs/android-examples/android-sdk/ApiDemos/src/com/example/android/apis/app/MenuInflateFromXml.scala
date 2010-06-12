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

package com.example.android.apis.app

import com.example.android.apis.R

import android.app.{Activity, NotificationManager}
import android.content.Context
import android.os.Bundle
import android.view.{Menu, MenuInflater, MenuItem, ViewGroup}
import android.widget.{ArrayAdapter, FrameLayout, LinearLayout, Spinner,
                       TextView, Toast}
import android.widget.LinearLayout.LayoutParams

/**
 * Demonstrates inflating menus from XML. There are different menu XML resources
 * that the user can choose to inflate. First, select an example resource from
 * the spinner, and then hit the menu button. To choose another, back out of the
 * activity and start over.
 */
object MenuInflateFromXml {
  /**
   * Different example menu resources.
   */
  private final val sMenuExampleResources = Array(
    R.menu.title_only, R.menu.title_icon, R.menu.submenu, R.menu.groups,
    R.menu.checkable, R.menu.shortcuts, R.menu.order, R.menu.category_order,
    R.menu.visible, R.menu.disabled
  )

  /**
   * Names corresponding to the different example menu resources.
   */
  private final val sMenuExampleNames = Array(
    "Title only", "Title and Icon", "Submenu", "Groups",
    "Checkable", "Shortcuts", "Order", "Category and Order",
    "Visible", "Disabled"
  )
}

class MenuInflateFromXml extends Activity {
  import MenuInflateFromXml._  // companion object
   
  /**
   * Lets the user choose a menu resource.
   */
  private var mSpinner: Spinner = _

  /**
   * Shown as instructions.
   */
  private var mInstructionsText: TextView = _
    
  /**
   * Safe to hold on to this.
   */
  private var mMenu: Menu = _
    
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
        
    // Create a simple layout
    val layout = new LinearLayout(this)
    layout setOrientation LinearLayout.VERTICAL
        
    // Create the spinner to allow the user to choose a menu XML
    val adapter = new ArrayAdapter[String](this,
                android.R.layout.simple_spinner_item, sMenuExampleNames)
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    mSpinner = new Spinner(this)
    // When programmatically creating views, make sure to set an ID
    // so it will automatically save its instance state
    mSpinner setId R.id.spinner
    mSpinner setAdapter adapter

    // Add the spinner
    layout.addView(mSpinner,
      new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                       ViewGroup.LayoutParams.WRAP_CONTENT))

    // Create help text
    mInstructionsText = new TextView(this)
    mInstructionsText setText getResources.getString(
      R.string.menu_from_xml_instructions_press_menu)
        
    // Add the help, make it look decent
    val lp = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                              ViewGroup.LayoutParams.WRAP_CONTENT)
    lp.setMargins(10, 10, 10, 10)
    layout.addView(mInstructionsText, lp)
        
    // Set the layout as our content view
    setContentView(layout)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    // Hold on to this
    mMenu = menu
        
    // Inflate the currently selected menu XML resource.
    val inflater = getMenuInflater
    inflater.inflate(sMenuExampleResources(mSpinner.getSelectedItemPosition), menu)

    // Disable the spinner since we've already created the menu and the user
    // can no longer pick a different menu XML.
    mSpinner setEnabled false

    // Change instructions
    mInstructionsText setText getResources.getString(
      R.string.menu_from_xml_instructions_go_back)
        
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      // For "Title only": Examples of matching an ID with one assigned in
      //                   the XML
      case R.id.jump =>
        Toast.makeText(this, "Jump up in the air!", Toast.LENGTH_SHORT).show()
        true

      case R.id.dive =>
        Toast.makeText(this, "Dive into the water!", Toast.LENGTH_SHORT).show();
        true

      // For "Groups": Toggle visibility of grouped menu items with
      //               nongrouped menu items
      case R.id.browser_visibility =>
        // The refresh item is part of the browser group
        val shouldShowBrowser = !mMenu.findItem(R.id.refresh).isVisible
        mMenu.setGroupVisible(R.id.browser, shouldShowBrowser)
        false
  
      case R.id.email_visibility =>
        // The reply item is part of the email group
        val shouldShowEmail = !mMenu.findItem(R.id.reply).isVisible
        mMenu.setGroupVisible(R.id.email, shouldShowEmail)
        false
                
      // Generic catch all for all the other menu resources
      case _ =>
        // Don't toast text when a submenu is clicked
        if (!item.hasSubMenu()) {
          Toast.makeText(this, item.getTitle, Toast.LENGTH_SHORT).show()
          true
        } else
          false
    }
  }

}
