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

package com.manning.unlockingandroid

import java.text.NumberFormat

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.{Button, EditText, TextView}

class ChapterTwo extends Activity {
  import ChapterTwo._  // companion object

  /** Called when the activity is first created. */
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)

    val mealpricefield: EditText = find(R.id.mealprice)
    val answerfield: TextView = find(R.id.answer)

    val button: Button = find(R.id.calculate)
    button setOnClickListener new /*Button*/View.OnClickListener() {
      def onClick(v: View) {
        try {
          // Perform action on click
          Log.i(ChapterTwo.tag, "onClick invoked.")

          // grab the meal price from the UI
          var mealprice = mealpricefield.getText.toString

          Log.i(ChapterTwo.tag, "mealprice is [" + mealprice + "]")
          var answer = ""

          // check to see if the meal price includes a "$"
          if (mealprice.indexOf("$") == -1) {
            mealprice = "$" + mealprice
          }

          var fmp = 0.0F

          // get currency formatter
          val nf = NumberFormat.getCurrencyInstance

          if (nf == null) {
            Log.i(ChapterTwo.tag, "punt - NumberFormat is null")
          }

          // grab the input meal price
          fmp = nf.parse(mealprice).floatValue

          // let's give a nice tip -> 20%
          fmp *= 1.2F

          Log.i(ChapterTwo.tag, "Total Meal price (unformatted) is [" + fmp + "]")
          // format our result
          answer = "Full Price, Including 20% Tip: " + nf.format(fmp)

          // display the answer
          answerfield setText answer

          Log.i(ChapterTwo.tag, "onClick complete.")
        } catch {
          case pe: java.text.ParseException =>
            Log.i(ChapterTwo.tag, "Parse exception caught")
            answerfield setText "Failed to parse amount?"
          case e: Exception =>
            Log.e(ChapterTwo.tag, "Failed to Calculate Tip:" + e.getMessage)
            e.printStackTrace()
            answerfield setText e.getMessage
        }
      }
    }
  }

  @inline
  private final def find[V <: View](id: Int) =
    findViewById(id).asInstanceOf[V]
}

object ChapterTwo {
  final val tag = "Chapter2"
}
