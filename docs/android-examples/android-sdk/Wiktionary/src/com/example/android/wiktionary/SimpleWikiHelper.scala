/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.wiktionary

import org.apache.http.{HttpEntity, HttpResponse, StatusLine}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.json.{JSONArray, JSONException, JSONObject}

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.util.Log

import java.io.{ByteArrayOutputStream, IOException, InputStream}

/**
 * Helper methods to simplify talking with and parsing responses from a
 * lightweight Wiktionary API. Before making any requests, you should call
 * {@link #prepareUserAgent(Context)} to generate a User-Agent string based on
 * your application package name and version.
 */
object SimpleWikiHelper {

  private final val TAG = "SimpleWikiHelper"

  /**
   * Partial URL to use when requesting the detailed entry for a specific
   * Wiktionary page. Use {@link String#format(String, Object...)} to insert
   * the desired page title after escaping it as needed.
   */
  private final val WIKTIONARY_PAGE =
    "http://en.wiktionary.org/w/api.php?action=query&prop=revisions&titles=%s&" +
    "rvprop=content&format=json%s"

  /**
   * Partial URL to append to {@link #WIKTIONARY_PAGE} when you want to expand
   * any templates found on the requested page. This is useful when browsing
   * full entries, but may use more network bandwidth.
   */
  private final val WIKTIONARY_EXPAND_TEMPLATES = "&rvexpandtemplates=true"

  /**
   * {@link StatusLine} HTTP status code when no server error has occurred.
   */
  private final val HTTP_STATUS_OK = 200

  /**
   * Shared buffer used by {@link #getUrlContent(String)} when reading results
   * from an API request.
   */
  private val sBuffer = new Array[Byte](512)

  /**
   * User-agent string to use when making requests. Should be filled using
   * {@link #prepareUserAgent(Context)} before making any other calls.
   */
  private var sUserAgent: String = _

  /**
   * Thrown when there were problems contacting the remote API server, either
   * because of a network error, or the server returned a bad status code.
   */
  class ApiException(detailMessage: String, throwable: Throwable)
  extends Exception(detailMessage, throwable) {
    def this(detailMessage: String) = this(detailMessage, null)
  }

  /**
   * Thrown when there were problems parsing the response to an API call,
   * either because the response was empty, or it was malformed.
   */
  class ParseException(detailMessage: String, throwable: Throwable)
  extends Exception(detailMessage, throwable)

  /**
   * Prepare the internal User-Agent string for use. This requires a
   * {@link Context} to pull the package name and version number for this
   * application.
   */
  def prepareUserAgent(context: Context) {
    try {
      // Read package name and version number from manifest
      val manager = context.getPackageManager
      val info = manager.getPackageInfo(context.getPackageName, 0)
      sUserAgent =
        String.format(context getString R.string.template_user_agent,
                      info.packageName, info.versionName)

    } catch {
      case e: NameNotFoundException =>
        Log.e(TAG, "Couldn't find package information in PackageManager", e)
    }
  }

  /**
   * Read and return the content for a specific Wiktionary page. This makes a
   * lightweight API call, and trims out just the page content returned.
   * Because this call blocks until results are available, it should not be
   * run from a UI thread.
   *
   * @param title The exact title of the Wiktionary page requested.
   * @param expandTemplates If true, expand any wiki templates found.
   * @return Exact content of page.
   * @throws ApiException If any connection or server error occurs.
   * @throws ParseException If there are problems parsing the response.
   */
  @throws(classOf[ApiException])
  @throws(classOf[ParseException])
  def getPageContent(title: String, expandTemplates: Boolean): String = {
    // Encode page title and expand templates if requested
    val encodedTitle = Uri.encode(title)
    val expandClause = if (expandTemplates) WIKTIONARY_EXPAND_TEMPLATES else ""

    // Query the API for content
    val content = getUrlContent(
      String.format(WIKTIONARY_PAGE, encodedTitle, expandClause))
    try {
      // Drill into the JSON response to find the content body
      val response = new JSONObject(content)
      val query = response getJSONObject "query"
      val pages = query getJSONObject "pages"
      val page = pages getJSONObject pages.keys.next.asInstanceOf[String]
      val revisions = page getJSONArray "revisions"
      val revision = revisions getJSONObject 0
      revision getString "*"
    } catch {
      case e: JSONException =>
        throw new ParseException("Problem parsing API response", e)
    }
  }

  /**
   * Pull the raw text content of the given URL. This call blocks until the
   * operation has completed, and is synchronized because it uses a shared
   * buffer {@link #sBuffer}.
   *
   * @param url The exact URL to request.
   * @return The raw content returned by the server.
   * @throws ApiException If any connection or server error occurs.
   */
  @throws(classOf[ApiException])
  protected[wiktionary] def getUrlContent(url: String): String = synchronized {
    if (sUserAgent == null) {
      throw new ApiException("User-Agent string must be prepared")
    }

    // Create client and set our specific user-agent string
    val client = new DefaultHttpClient()
    val request = new HttpGet(url)
    request.setHeader("User-Agent", sUserAgent)

    try {
      val response: HttpResponse = client.execute(request)

      // Check if server response is valid
      val status = response.getStatusLine
      if (status.getStatusCode != HTTP_STATUS_OK) {
        throw new ApiException(
          "Invalid response from server: " + status.toString)
      }

      // Pull content stream from response
      val entity: HttpEntity = response.getEntity
      val inputStream: InputStream = entity.getContent

      val content = new ByteArrayOutputStream()

      // Read response into a buffered stream
      var readBytes = inputStream read sBuffer
      while (readBytes != -1) {
        content.write(sBuffer, 0, readBytes)
        readBytes = inputStream read sBuffer
      }

      // Return result from buffered stream
      new String(content.toByteArray)
    } catch {
      case e: IOException =>
        throw new ApiException("Problem communicating with API", e)
    }
  }

}
