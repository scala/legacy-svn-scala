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

package com.msi.manning.unlockingandroid

import android.app.{Activity, AlertDialog}
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.{Button, EditText}

class AWhereDoYouLive extends Activity {

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)

    val adb = new AlertDialog.Builder(this)
    val addressfield = findViewById(R.id.address).asInstanceOf[EditText]

    val button = findViewById(R.id.launchmap).asInstanceOf[Button]
    button setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        try {
          // Perform action on click
          val address = addressfield.getText.toString.replace(' ', '+')
          val geoIntent = new Intent(android.content.Intent.ACTION_VIEW, 
                                     Uri.parse("geo:0,0?q=" + address))
          startActivity(geoIntent)
        } catch {
          case e: Exception =>
            val ad: AlertDialog = adb.create()
            ad setMessage "Failed to Launch"
            ad show()
        }
      }
    }

  }
}
