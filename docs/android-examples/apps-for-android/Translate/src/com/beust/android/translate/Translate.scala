/*
 * Copyright (C) 2008 Google Inc.
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

package com.beust.android.translate

import android.util.Log

import java.io.{BufferedReader, IOException, InputStream, InputStreamReader}
import java.net.{HttpURLConnection, MalformedURLException, URL, URLEncoder}

import org.json.JSONObject

/**
 * Makes the Google Translate API available to Java applications.
 *
 * @author Richard Midwinter
 * @author Emeric Vernat
 * @author Juan B Cabral
 * @author Cedric Beust
 */
object Translate {
    
  private final val ENCODING = "UTF-8"
  private final val URL_STRING =
    "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair="
  private final val TEXT_VAR = "&q="

  /**
   * Translates text from a given language to another given language using
   * Google Translate
   *
   * @param text The String to translate.
   * @param from The language code to translate from.
   * @param to The language code to translate to.
   * @return The translated String.
   * @throws MalformedURLException
   * @throws IOException
   */
  @throws(classOf[Exception])
  def translate(text: String, from: String, to: String): String =
    retrieveTranslation(text, from, to)

  /**
   * Forms an HTTP request and parses the response for a translation.
   *
   * @param text The String to translate.
   * @param from The language code to translate from.
   * @param to The language code to translate to.
   * @return The translated String.
   * @throws Exception
   */
  @throws(classOf[Exception])
  private def retrieveTranslation(text: String, from: String, to: String): String =
    try {
      val url = new StringBuilder()
      url append URL_STRING append from append "%7C" append to
      url append TEXT_VAR append URLEncoder.encode(text, ENCODING)

      Log.d(TranslateService.TAG, "Connecting to " + url.toString)
      val uc = new URL(url.toString).openConnection().asInstanceOf[HttpURLConnection]
      uc setDoInput true
      uc setDoOutput true
      try {
        Log.d(TranslateService.TAG, "getInputStream()")
        val is = uc.getInputStream
        val result = toString(is)
                
        val json = new JSONObject(result)
        json.get("responseData").asInstanceOf[JSONObject].getString("translatedText")
      } finally { // http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
        uc.getInputStream.close()
        if (uc.getErrorStream() != null) uc.getErrorStream().close()
      }
    } catch {
      case ex: Exception => throw ex
    }

  /**
   * Reads an InputStream and returns its contents as a String. Also effects
   * rate control.
   *
   * @param inputStream The InputStream to read from.
   * @return The contents of the InputStream as a String.
   * @throws Exception
   */
  @throws(classOf[Exception])
  private def toString(inputStream: InputStream): String = {
    val outputBuilder = new StringBuilder()
    try {
      if (inputStream != null) {
        val reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING))
        var line = reader.readLine()
        while (null != line) {
          outputBuilder append line append '\n'
          line = reader.readLine()
        }
      }
    } catch {
      case ex: Exception =>
        Log.e(TranslateService.TAG, "Error reading translation stream.", ex)
    }
    outputBuilder.toString
  }

}
