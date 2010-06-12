/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.{Activity, AlertDialog, SearchManager}
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.{Menu, MenuItem, View}
import android.view.View.OnClickListener
import android.widget.{AdapterView, ArrayAdapter, Button, EditText, Spinner}
import android.widget.AdapterView.OnItemSelectedListener

object SearchInvoke {
  // Menu mode spinner choices
  // This list must match the list found in samples/ApiDemos/res/values/arrays.xml
  private final val MENUMODE_SEARCH_KEY = 0
  private final val MENUMODE_MENU_ITEM = 1
  private final val MENUMODE_TYPE_TO_SEARCH = 2
  private final val MENUMODE_DISABLED = 3
}

class SearchInvoke extends Activity {  
  import Activity._, SearchInvoke._  // companion object

  // UI elements
  private var mStartSearch: Button = _
  private var mMenuMode: Spinner = _
  private var mQueryPrefill: EditText = _
  private var mQueryAppData: EditText = _
    
  /** 
   * Called with the activity is first created.
   * 
   *  We aren't doing anything special in this implementation, other than
   *  the usual activity setup code. 
   */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
        
    // Inflate our UI from its XML layout description.
    setContentView(R.layout.search_invoke)
        
    // Get display items for later interaction
    mStartSearch = findViewById(R.id.btn_start_search).asInstanceOf[Button]
    mMenuMode = findViewById(R.id.spinner_menu_mode).asInstanceOf[Spinner]
    mQueryPrefill = findViewById(R.id.txt_query_prefill).asInstanceOf[EditText]
    mQueryAppData = findViewById(R.id.txt_query_appdata).asInstanceOf[EditText]
        
    // Populate items
    val adapter = ArrayAdapter.createFromResource( // ArrayAdapter<CharSequence>
                            this, R.array.search_menuModes,
                            android.R.layout.simple_spinner_item)
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    mMenuMode setAdapter adapter
        
    // Create listener for the menu mode dropdown.  We use this to demonstrate control
    // of the default keys handler in every Activity.  More typically, you will simply set
    // the default key mode in your activity's onCreate() handler.
    mMenuMode.setOnItemSelectedListener(
      new OnItemSelectedListener() {
        def onItemSelected(parent: AdapterView[_], view: View,
                           position: Int, id: Long) {
          setDefaultKeyMode(
            if (position == MENUMODE_TYPE_TO_SEARCH) DEFAULT_KEYS_SEARCH_LOCAL
            else DEFAULT_KEYS_DISABLE
          )
        }

        def onNothingSelected(parent: AdapterView[_]) {
          setDefaultKeyMode(DEFAULT_KEYS_DISABLE)
        }
      })

    // Attach actions to buttons
    mStartSearch.setOnClickListener(
      new OnClickListener() {
        def onClick(v: View) {
          onSearchRequested()
        }
      })
  }
    
  /** 
   * Called when your activity's options menu needs to be updated. 
   */
  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    super.onPrepareOptionsMenu(menu)

    // first, get rid of our menus (if any)
    menu removeItem 0
    menu removeItem 1

    // next, add back item(s) based on current menu mode
    mMenuMode.getSelectedItemPosition match {
      case MENUMODE_SEARCH_KEY =>
        menu.add( 0, 0, 0, "(Search Key)")
            
      case MENUMODE_MENU_ITEM =>
        val item = menu.add( 0, 0, 0, "Search")
        item setAlphabeticShortcut SearchManager.MENU_KEY
            
      case MENUMODE_TYPE_TO_SEARCH =>
        menu.add( 0, 0, 0, "(Type-To-Search)")
            
      case MENUMODE_DISABLED =>
        menu.add( 0, 0, 0, "(Disabled)")
      case _ =>
    }
        
    menu.add(0, 1, 0, "Clear History")
    true
  }
    
  /** Handle the menu item selections */
  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case 0 =>
        mMenuMode.getSelectedItemPosition match {
          case MENUMODE_SEARCH_KEY =>
            new AlertDialog.Builder(this)
              .setMessage("To invoke search, dismiss this dialog and press the search key" +
                                " (F5 on the simulator).")
              .setPositiveButton("OK", null)
              .show()
                
          case MENUMODE_MENU_ITEM =>
            onSearchRequested()
                
          case MENUMODE_TYPE_TO_SEARCH =>
            new AlertDialog.Builder(this)
              .setMessage("To invoke search, dismiss this dialog and start typing.")
              .setPositiveButton("OK", null)
              .show()
                
          case MENUMODE_DISABLED =>
             new AlertDialog.Builder(this)
               .setMessage("You have disabled search.")
               .setPositiveButton("OK", null)
               .show()
        }
      case 1 =>
        clearSearchHistory()
    }

    super.onOptionsItemSelected(item)
  }
    
  /**
   * This hook is called when the user signals the desire to start a search.
   * 
   * By overriding this hook we can insert local or context-specific data.
   * 
   * @return Returns true if search launched, false if activity blocks it
   */
  override def onSearchRequested(): Boolean = {
    // If your application absolutely must disable search, do it here.
    if (mMenuMode.getSelectedItemPosition == MENUMODE_DISABLED) {
      return false;
    }
        
    // It's possible to prefill the query string before launching the search
    // UI.  For this demo, we simply copy it from the user input field.
    // For most applications, you can simply pass null to startSearch() to
    // open the UI with an empty query string.
    val queryPrefill = mQueryPrefill.getText.toString
        
    // Next, set up a bundle to send context-specific search data (if any)
    // The bundle can contain any number of elements, using any number of keys;
    // For this Api Demo we copy a string from the user input field, and store
    // it in the bundle as a string with the key "demo_key".
    // For most applications, you can simply pass null to startSearch().
    var appDataBundle: Bundle = null
    val queryAppDataString = mQueryAppData.getText.toString
    if (queryAppDataString != null) {
      appDataBundle = new Bundle()
      appDataBundle.putString("demo_key", queryAppDataString)
    }
        
    // Now call the Activity member function that invokes the Search Manager UI.
    startSearch(queryPrefill, false, appDataBundle, false)
        
    // Returning true indicates that we did launch the search, instead of
    // blocking it.
    true
  }
    
  /**
   * Any application that implements search suggestions based on previous
   * actions (such as recent queries, page/items viewed, etc.) should provide a
   * way for the user to clear the history.  This gives the user a measure of
   * privacy, if they do not wish for their recent searches to be replayed by
   * other users of the device (via suggestions).
   * 
   * This example shows how to clear the search history for apps that use 
   * android.provider.SearchRecentSuggestions.  If you have developed a custom
   * suggestions provider, you'll need to provide a similar API for clearing
   * history.
   * 
   * In this sample app we call this method from a "Clear History" menu item.
   * You could also implement the UI in your preferences, or any other logical
   * place in your UI.
   */
  private def clearSearchHistory() {
    val suggestions = new SearchRecentSuggestions(this, 
      SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE)
    suggestions.clearHistory()
  }
    
}
