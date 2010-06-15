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

package com.msi.manning.chapter5.filestorage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

import org.xmlpull.v1.XmlPullParser

class ReadXMLResourceFile extends Activity {
  import ReadXMLResourceFile._  // companion object

  private var readOutput: TextView = _
  private var gotoReadWriteSDCard: Button = _

  private def find[V <: View](id: Int) = findViewById(id).asInstanceOf[V]

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.read_xmlresource_file)

    readOutput = find(R.id.readxmlres_output)

    val parser = getResources.getXml(R.xml.people)
    val sb = new StringBuffer()

    try {
      while (parser.next() != XmlPullParser.END_DOCUMENT) {
        val name = parser.getName
        Log.v(ReadXMLResourceFile.LOGTAG, "    parser NAME - " + name)
        var first: String = null
        var last: String = null
        if ((name != null) && name.equals("person")) {
          val size = parser.getAttributeCount
          for (i <- 0 until size) {
            val attrName = parser getAttributeName i
            val attrValue = parser getAttributeValue i
            if ((attrName != null) && attrName.equals("firstname")) {
              first = attrValue
            } else if ((attrName != null) && attrName.equals("lastname")) {
              last = attrValue
            }
          }
          if ((first != null) && (last != null)) {
            sb.append(last + ", " + first + "\n")
          }
        }
      }
      readOutput.setText(sb.toString)
    } catch {
      case e: Exception =>
        Log.e(ReadXMLResourceFile.LOGTAG, e.getMessage, e)
    }        

    gotoReadWriteSDCard = find(R.id.readwritesdcard_button)
    gotoReadWriteSDCard setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(ReadXMLResourceFile.this,
                                 classOf[ReadWriteSDCardFile]))
      }
    }

  }
}

object ReadXMLResourceFile {
  private final val LOGTAG = "FileStorage"
}
