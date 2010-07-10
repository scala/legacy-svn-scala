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

package com.google.android.downloader

import android.app.{Activity, AlertDialog}
import android.app.AlertDialog.Builder
import android.content.{DialogInterface, Intent}
import org.apache.http.impl.client.DefaultHttpClient
import android.os.{Bundle, Handler, Message, SystemClock}
import java.security.MessageDigest
import android.util.{Log, Xml}
import android.view.{View, Window, WindowManager}
import android.widget.{Button, TextView}

import org.apache.http.{Header, HttpEntity, HttpResponse, HttpStatus}
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.methods.{HttpGet, HttpHead}
import org.xml.sax.{Attributes, SAXException}
import org.xml.sax.helpers.DefaultHandler

import java.io.{File, FileInputStream, FileNotFoundException, FileOutputStream,
                IOException, InputStream, OutputStream, UnsupportedEncodingException}
import java.net.{MalformedURLException, URL}
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat

import scala.collection.mutable.{HashMap, HashSet, ListBuffer}

class DownloaderActivity extends Activity {
  import DownloaderActivity._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val intent = getIntent
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
    setContentView(R.layout.downloader)
    getWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.downloader_title)
    val tv = findViewById(R.id.customText).asInstanceOf[TextView]
    tv setText intent.getStringExtra(EXTRA_CUSTOM_TEXT)
    mProgress = findViewById(R.id.progress).asInstanceOf[TextView]
    mTimeRemaining = findViewById(R.id.time_remaining).asInstanceOf[TextView]
    val button = findViewById(R.id.cancel).asInstanceOf[Button]
    button setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        if (mDownloadThread != null) {
          mSuppressErrorMessages = true
          mDownloadThread.interrupt()
        }
      }
    }
    startDownloadThread()
  }

  private def startDownloadThread() {
    mSuppressErrorMessages = false
    mProgress setText ""
    mTimeRemaining setText ""
    mDownloadThread = new Thread(new Downloader(), "Downloader")
    mDownloadThread.setPriority(Thread.NORM_PRIORITY - 1)
    mDownloadThread.start()
  }

  override protected def onResume() {
    super.onResume()
  }

  override protected def onDestroy() {
    super.onDestroy()
    mSuppressErrorMessages = true
    mDownloadThread.interrupt()
    try {
      mDownloadThread.join()
    } catch {
      case e: InterruptedException => // Don't care.
    }
  }

  private def onDownloadSucceeded() {
    Log.i(LOG_TAG, "Download succeeded")
    PreconditionActivityHelper.startOriginalActivityAndFinish(this)
  }

  private def onDownloadFailed(reason: String) {
    Log.e(LOG_TAG, "Download stopped: " + reason)
    val index = reason indexOf '\n'
    val shortReason =
      if (index >= 0) reason.substring(0, index)
      else reason
    val alert = new Builder(this).create()
    alert setTitle R.string.download_activity_download_stopped

    if (!mSuppressErrorMessages) {
      alert setMessage shortReason
    }

    alert.setButton(
      getString(R.string.download_activity_retry),
      new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {
          startDownloadThread()
        }
      })
    alert.setButton2(
      getString(R.string.download_activity_quit),
      new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {
          finish()
        }
      })
    try {
      alert.show()
    } catch {
      case e: WindowManager.BadTokenException =>
        // Happens when the Back button is used to exit the activity.
        // ignore.
    }
  }

  private def onReportProgress(progress: Int) {
    mProgress setText mPercentFormat.format(progress / 10000.0)
    val now = SystemClock.elapsedRealtime
    if (mStartTime == 0) {
      mStartTime = now
    }
    val delta = now - mStartTime
    val timeRemaining = if ((delta > 3 * MS_PER_SECOND) && (progress > 100)) {
      val totalTime = 10000 * delta / progress
      val timeLeft = math.max(0L, totalTime - delta)
      val (timeUnit, id) =
        if (timeLeft > MS_PER_DAY)
          (MS_PER_DAY, R.string.download_activity_time_remaining_days)
        else if (timeLeft > MS_PER_HOUR)
          (MS_PER_HOUR, R.string.download_activity_time_remaining_hours)
        else if (timeLeft > MS_PER_MINUTE)
          (MS_PER_MINUTE, R.string.download_activity_time_remaining_minutes)
        else
          (MS_PER_SECOND, R.string.download_activity_time_remaining_seconds)
      java.lang.Long.toString((timeLeft + timeUnit - 1) / timeUnit) + " " + getString(id)
    } else
      getString(R.string.download_activity_time_remaining_unknown)
    mTimeRemaining setText timeRemaining
  }

  private def onReportVerifying() {
    mProgress setText getString(R.string.download_activity_verifying)
    mTimeRemaining setText ""
  }

  private class DownloaderException(reason: String) extends Exception(reason)

  private class Downloader extends AnyRef with Runnable {
    import Downloader._  // companion object

    def run() {
      val intent = getIntent
      mFileConfigUrl = intent getStringExtra EXTRA_FILE_CONFIG_URL
      mConfigVersion = intent getStringExtra EXTRA_CONFIG_VERSION
      mDataPath = intent getStringExtra EXTRA_DATA_PATH
      mUserAgent = intent getStringExtra EXTRA_USER_AGENT

      mDataDir = new File(mDataPath)

      try {
        // Download files.
        mHttpClient = new DefaultHttpClient()
        val config = getConfig
        filter(config)
        persistantDownload(config)
        verify(config)
        cleanup()
        reportSuccess()
      } catch {
        case e: Exception =>
          reportFailure(e.toString() + "\n" + Log.getStackTraceString(e))
      }
    }

    @throws(classOf[ClientProtocolException])
    @throws(classOf[DownloaderException])
    @throws(classOf[IOException])
    private def persistantDownload(config: Config) {
      var retry = true
      while (retry) {
        try {
          download(config)
          retry = false
        } catch {
          case e: java.net.SocketException =>
            if (mSuppressErrorMessages) {
              throw e
            }
          case e: java.net.SocketTimeoutException =>
            if (mSuppressErrorMessages) {
              throw e
            }
        }
        Log.i(LOG_TAG, "Network connectivity issue, retrying.")
      }
    }

    @throws(classOf[IOException])
    @throws(classOf[DownloaderException])
    private def filter(config: Config) {
      val filteredFile = new File(mDataDir, LOCAL_FILTERED_FILE)
      if (filteredFile.exists) {
        return
      }

      val localConfigFile = new File(mDataDir, LOCAL_CONFIG_FILE_TEMP)
      val keepSet = new HashSet[String]()
      keepSet add localConfigFile.getCanonicalPath

      val fileMap = new HashMap[String, Config.File]()
      for (file <- config.mFiles) {
        val canonicalPath = new File(mDataDir, file.dest).getCanonicalPath
        fileMap.put(canonicalPath, file)
      }
      recursiveFilter(mDataDir, fileMap, keepSet, false)
      touch(filteredFile)
    }

    @throws(classOf[FileNotFoundException])
    private def touch(file: File) {
      val os = new FileOutputStream(file)
      quietClose(os)
    }

    @throws(classOf[IOException])
    @throws(classOf[DownloaderException])
    private def recursiveFilter(base: File,
                fileMap: HashMap[String, Config.File],
                keepSet: HashSet[String], filterBase: Boolean): Boolean = {
      var result = true
      if (base.isDirectory) {
        for (child <- base.listFiles) {
          result &= recursiveFilter(child, fileMap, keepSet, true)
        }
      }
      if (filterBase) {
        if (base.isDirectory) {
          if (base.listFiles.length == 0) {
            result &= base.delete()
          }
        } else {
          if (!shouldKeepFile(base, fileMap, keepSet)) {
            result &= base.delete()
          }
        }
      }
      result
    }

    @throws(classOf[IOException])
    @throws(classOf[DownloaderException])
    private def shouldKeepFile(file: File,
                fileMap: HashMap[String, Config.File],
                keepSet: HashSet[String]): Boolean = {
      val canonicalPath = file.getCanonicalPath
      if (keepSet contains canonicalPath) {
        return true
      }
      val configFile = fileMap(canonicalPath)
      if (configFile == null) false
      else verifyFile(configFile, false)
    }

    private def reportSuccess() {
      mHandler sendMessage Message.obtain(mHandler, MSG_DOWNLOAD_SUCCEEDED)
    }

    private def reportFailure(reason: String) {
      mHandler sendMessage Message.obtain(mHandler, MSG_DOWNLOAD_FAILED, reason)
    }

    private def reportProgress(progress: Int) {
      mHandler sendMessage Message.obtain(mHandler, MSG_REPORT_PROGRESS, progress, 0)
    }

    private def reportVerifying() {
      mHandler sendMessage Message.obtain(mHandler, MSG_REPORT_VERIFYING)
    }

    @throws(classOf[DownloaderException])
    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    @throws(classOf[SAXException])
    private def getConfig: Config = {
      var config: Config = null
      if (mDataDir.exists) {
        config = getLocalConfig(mDataDir, LOCAL_CONFIG_FILE_TEMP)
        if ((config == null)
            || !mConfigVersion.equals(config.version)) {
          if (config == null) {
            Log.i(LOG_TAG, "Couldn't find local config.")
          } else {
            Log.i(LOG_TAG, "Local version out of sync. Wanted " +
                           mConfigVersion + " but have " + config.version)
          }
          config = null
        }
      } else {
        Log.i(LOG_TAG, "Creating directory " + mDataPath)
        mDataDir.mkdirs()
        mDataDir.mkdir()
        if (!mDataDir.exists) {
          throw new DownloaderException(
            "Could not create the directory " + mDataPath)
        }
      }
      if (config == null) {
        val localConfig = download(mFileConfigUrl, LOCAL_CONFIG_FILE_TEMP)
        val is = new FileInputStream(localConfig)
        try {
          config = ConfigHandler.parse(is)
        } finally {
          quietClose(is)
        }
        if (! config.version.equals(mConfigVersion)) {
          throw new DownloaderException(
            "Configuration file version mismatch. Expected " +
            mConfigVersion + " received " +
            config.version)
        }
      }
      config
    }

    @throws(classOf[IOException])
    private def noisyDelete(file: File) {
      if (! file.delete()) {
        throw new IOException("could not delete " + file)
      }
    }

    @throws(classOf[DownloaderException])
    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    private def download(config: Config) {
      mDownloadedSize = 0
      getSizes(config)
      Log.i(LOG_TAG, "Total bytes to download: " + mTotalExpectedSize)
      for (file <- config.mFiles) {
        downloadFile(file)
      }
    }

    @throws(classOf[DownloaderException])
    @throws(classOf[FileNotFoundException])
    @throws(classOf[IOException])
    @throws(classOf[ClientProtocolException])
    private def downloadFile(file: Config.File) {
      var append = false
      val dest = new File(mDataDir, file.dest)
      var bytesToSkip = 0L
      if (dest.exists && dest.isFile) {
        append = true
        bytesToSkip = dest.length
        mDownloadedSize += bytesToSkip
      }
      var os: FileOutputStream = null
      var offsetOfCurrentPart = 0L
      try {
        for (part <- file.mParts) {
          // The part.size==0 check below allows us to download
          // zero-length files.
          if ((part.size > bytesToSkip) || (part.size == 0)) {
            var digest: MessageDigest = null
            if (part.md5 != null) {
              digest = createDigest()
              if (bytesToSkip > 0) {
                val is = openInput(file.dest)
                try {
                  is.skip(offsetOfCurrentPart)
                  readIntoDigest(is, bytesToSkip, digest)
                } finally {
                  quietClose(is)
                }
              }
            }
            if (os == null) {
              os = openOutput(file.dest, append)
            }
            downloadPart(part.src, os, bytesToSkip, part.size, digest)
            if (digest != null) {
              val hash = getHash(digest)
              if (!hash.equalsIgnoreCase(part.md5)) {
                Log.e(LOG_TAG, "web MD5 checksums don't match. "
                             + part.src + "\nExpected "
                             + part.md5 + "\n     got " + hash)
                quietClose(os)
                dest.delete()
                throw new DownloaderException(
                  "Received bad data from web server")
              } else {
                Log.i(LOG_TAG, "web MD5 checksum matches.")
              }
            }
          }
          bytesToSkip -= math.min(bytesToSkip, part.size)
          offsetOfCurrentPart += part.size
        }
      } finally {
        quietClose(os)
      }
    }

    @throws(classOf[IOException])
    private def cleanup() {
      val filtered = new File(mDataDir, LOCAL_FILTERED_FILE)
      noisyDelete(filtered)
      val tempConfig = new File(mDataDir, LOCAL_CONFIG_FILE_TEMP)
      val realConfig = new File(mDataDir, LOCAL_CONFIG_FILE)
      tempConfig renameTo realConfig
    }

    @throws(classOf[DownloaderException])
    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    private def verify(config: Config) {
      Log.i(LOG_TAG, "Verifying...")
      var failFiles: String = null
      for (file <- config.mFiles) {
        if (! verifyFile(file, true) ) {
          if (failFiles == null) {
            failFiles = file.dest
          } else {
            failFiles += " " + file.dest
          }
        }
      }
      if (failFiles != null) {
        throw new DownloaderException(
          "Possible bad SD-Card. MD5 sum incorrect for file(s) " + failFiles)
      }
    }

    @throws(classOf[FileNotFoundException])
    @throws(classOf[DownloaderException])
    @throws(classOf[IOException])
    private def verifyFile(file: Config.File, deleteInvalid: Boolean): Boolean = {
      Log.i(LOG_TAG, "verifying " + file.dest)
      reportVerifying()
      val dest = new File(mDataDir, file.dest)
      if (! dest.exists) {
        Log.e(LOG_TAG, "File does not exist: " + dest.toString)
        return false
      }
      val fileSize = file.getSize
      val destLength = dest.length
      if (fileSize != destLength) {
        Log.e(LOG_TAG, "Length doesn't match. Expected " + fileSize
                        + " got " + destLength)
        if (deleteInvalid) {
          dest.delete()
          return false
        }
      }
      val is = new FileInputStream(dest)
      try {
        for (part <- file.mParts if part.md5 != null) {
          val digest = createDigest()
          readIntoDigest(is, part.size, digest)
          val hash = getHash(digest)
          if (!hash.equalsIgnoreCase(part.md5)) {
            Log.e(LOG_TAG, "MD5 checksums don't match. " +
                           part.src + " Expected " +
                           part.md5 + " got " + hash)
            if (deleteInvalid) {
               quietClose(is)
               dest.delete()
            }
            return false
          }
        }
      } finally {
        quietClose(is)
      }
      true
    }

    @throws(classOf[IOException])
    private def readIntoDigest(is: FileInputStream, bytesToRead: Long,
                digest: MessageDigest) {
      var i = bytesToRead
      while (i > 0) {
        val chunkSize = math.min(mFileIOBuffer.length, i).toInt
        val bytesRead = is.read(mFileIOBuffer, 0, chunkSize)
        i = if (bytesRead < 0) 0 else {
          updateDigest(digest, bytesRead)
          i - bytesRead
        }
      }
    }

    @throws(classOf[DownloaderException])
    private def createDigest(): MessageDigest =
      try {
        MessageDigest.getInstance("MD5")
      } catch {
        case e: NoSuchAlgorithmException =>
          throw new DownloaderException("Couldn't create MD5 digest")
      }

    private def updateDigest(digest: MessageDigest, bytesRead: Int) {
      if (bytesRead == mFileIOBuffer.length) {
        digest update mFileIOBuffer
      } else {
        // Work around an awkward API: Create a
        // new buffer with just the valid bytes
        val temp = new Array[Byte](bytesRead)
        System.arraycopy(mFileIOBuffer, 0, temp, 0, bytesRead)
        digest update temp
      }
    }

    private def getHash(digest: MessageDigest): String = {
      val builder = new StringBuilder()
      for (b <- digest.digest()) {
        builder.append(Integer.toHexString((b >> 4) & 0xf))
        builder.append(Integer.toHexString(b & 0xf))
      }
      builder.toString
    }

    /**
     * Ensure we have sizes for all the items.
     * @param config
     * @throws ClientProtocolException
     * @throws IOException
     * @throws DownloaderException
     */
    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    @throws(classOf[DownloaderException])
    private def getSizes(config: Config) {
      for (file <- config.mFiles; part <- file.mParts) {
        if (part.size < 0) {
          part.size = getSize(part.src)
        }
      }
      mTotalExpectedSize = config.getSize
    }

    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    private def getSize(url0: String): Long = {
      val url = normalizeUrl(url0)
      Log.i(LOG_TAG, "Head " + url)
      val httpGet = new HttpHead(url)
      val response = mHttpClient.execute(httpGet)
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new IOException("Unexpected Http status code "
                    + response.getStatusLine.getStatusCode)
      }
      val clHeaders = response getHeaders "Content-Length"
      if (clHeaders.length > 0) {
        val header = clHeaders(0)
        java.lang.Long.parseLong(header.getValue)
      } else
        -1
    }

    @throws(classOf[MalformedURLException])
    private def normalizeUrl(url: String): String =
      (new URL(new URL(mFileConfigUrl), url)).toString

    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    private def get(url0: String, startOffset: Long,
                    expectedLength: Long): InputStream = {
      val url = normalizeUrl(url0)
      Log.i(LOG_TAG, "Get " + url)

      mHttpGet = new HttpGet(url)
      val expectedStatusCode = if (startOffset > 0) {
        var range = "bytes=" + startOffset + "-"
        if (expectedLength >= 0) {
          range += expectedLength-1
        }
        Log.i(LOG_TAG, "requesting byte range " + range)
        mHttpGet.addHeader("Range", range)
        HttpStatus.SC_PARTIAL_CONTENT
      } else
        HttpStatus.SC_OK
      val response: HttpResponse = mHttpClient execute mHttpGet
      var bytesToSkip = 0L
      val statusCode = response.getStatusLine.getStatusCode
      if (statusCode != expectedStatusCode) {
        if ((statusCode == HttpStatus.SC_OK) &&
            (expectedStatusCode == HttpStatus.SC_PARTIAL_CONTENT)) {
          Log.i(LOG_TAG, "Byte range request ignored")
          bytesToSkip = startOffset
        } else {
          throw new IOException("Unexpected Http status code "
                            + statusCode + " expected "
                            + expectedStatusCode)
        }
      }
      val entity = response.getEntity
      val is = entity.getContent
      if (bytesToSkip > 0) {
        is skip bytesToSkip
      }
      is
    }

    @throws(classOf[DownloaderException])
    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    private def download(src: String, dest: String): File = {
      val destFile = new File(mDataDir, dest)
      val os = openOutput(dest, false)
      try {
        downloadPart(src, os, 0, -1, null)
      } finally {
        os.close()
      }
      destFile
    }

    @throws(classOf[ClientProtocolException])
    @throws(classOf[IOException])
    @throws(classOf[DownloaderException])
    private def downloadPart(src: String, os: FileOutputStream,
                             startOffset: Long, expectedLength: Long,
                             digest: MessageDigest) {
      val lengthIsKnown = expectedLength >= 0
      if (startOffset < 0) {
        throw new IllegalArgumentException("Negative startOffset:"
                        + startOffset)
      }
      if (lengthIsKnown && (startOffset > expectedLength)) {
        throw new IllegalArgumentException(
                        "startOffset > expectedLength" + startOffset + " "
                        + expectedLength)
      }
      val is = get(src, startOffset, expectedLength)
      try {
        val bytesRead = downloadStream(is, os, digest)
        if (lengthIsKnown) {
          val expectedBytesRead = expectedLength - startOffset
          if (expectedBytesRead != bytesRead) {
            Log.e(LOG_TAG, "Bad file transfer from server: " + src
                           + " Expected " + expectedBytesRead
                           + " Received " + bytesRead)
            throw new DownloaderException(
              "Incorrect number of bytes received from server")
          }
        }
      } finally {
        is.close()
        mHttpGet = null
      }
    }

    @throws(classOf[FileNotFoundException])
    @throws(classOf[DownloaderException])
    private def openOutput(dest: String, append: Boolean): FileOutputStream = {
      val destFile = new File(mDataDir, dest)
      val parent = destFile.getParentFile
      if (! parent.exists) {
        parent.mkdirs()
      }
      if (! parent.exists) {
        throw new DownloaderException(
          "Could not create directory " + parent.toString)
      }
      new FileOutputStream(destFile, append)
    }

    @throws(classOf[FileNotFoundException])
    @throws(classOf[DownloaderException])
    private def openInput(src: String): FileInputStream = {
      val srcFile = new File(mDataDir, src)
      val parent = srcFile.getParentFile
      if (! parent.exists) {
        parent.mkdirs()
      }
      if (! parent.exists) {
        throw new DownloaderException(
          "Could not create directory " + parent.toString)
      }
      new FileInputStream(srcFile)
    }

    @throws(classOf[DownloaderException])
    @throws(classOf[IOException])
    private def downloadStream(is: InputStream, os: FileOutputStream,
                digest: MessageDigest): Long = {
      var totalBytesRead = 0
      var hasMore = true
      while (hasMore) {
        if (Thread.interrupted()) {
          Log.i(LOG_TAG, "downloader thread interrupted.")
          mHttpGet.abort()
          throw new DownloaderException("Thread interrupted")
        }
        val bytesRead = is.read(mFileIOBuffer)
        if (bytesRead < 0) {
          hasMore = false
        } else {
          if (digest != null) {
            updateDigest(digest, bytesRead)
          }
          totalBytesRead += bytesRead
          os.write(mFileIOBuffer, 0, bytesRead)
          mDownloadedSize += bytesRead
          val progress = (math.min(mTotalExpectedSize,
                         mDownloadedSize * 10000 /
                         math.max(1, mTotalExpectedSize))).toInt
          if (progress != mReportedProgress) {
            mReportedProgress = progress
            reportProgress(progress)
          }
        }
      }
      totalBytesRead
    }

    private var mHttpClient: DefaultHttpClient = _
    private var mHttpGet: HttpGet = _
    private var mFileConfigUrl: String = _
    private var mConfigVersion: String = _
    private var mDataPath: String = _
    private var mDataDir: File = _
    private var mUserAgent: String = _
    private var mTotalExpectedSize: Long = _
    private var mDownloadedSize: Long = _
    private var mReportedProgress: Int = _
    private val mFileIOBuffer = new Array[Byte](CHUNK_SIZE)

  } //class Downloader

  private object Downloader {
    private final val CHUNK_SIZE = 32 * 1024
  }

  private var mProgress: TextView = _
  private var mTimeRemaining: TextView = _
  private final val mPercentFormat = new DecimalFormat("0.00 %")
  private var mStartTime: Long = _
  private var mDownloadThread: Thread = _
  private var mSuppressErrorMessages: Boolean = _

  private final val mHandler = new Handler() {
    override def handleMessage(msg: Message) = msg.what match {
      case MSG_DOWNLOAD_SUCCEEDED =>
        onDownloadSucceeded()
      case MSG_DOWNLOAD_FAILED =>
        onDownloadFailed(msg.obj.toString)
      case MSG_REPORT_PROGRESS =>
        onReportProgress(msg.arg1)
      case MSG_REPORT_VERIFYING =>
        onReportVerifying()
      case _ =>
        throw new IllegalArgumentException("Unknown message id " + msg.what)
    }
  }

}

object DownloaderActivity {

  /**
   * Checks if data has been downloaded. If so, returns true. If not,
   * starts an activity to download the data and returns false. If this
   * function returns false the caller should immediately return from its
   * onCreate method. The calling activity will later be restarted
   * (using a copy of its original intent) once the data download completes.
   *
   * @param activity The calling activity.
   * @param customText A text string that is displayed in the downloader UI.
   * @param fileConfigUrl The URL of the download configuration URL.
   * @param configVersion The version of the configuration file.
   * @param dataPath The directory on the device where we want to store the
   * data.
   * @param userAgent The user agent string to use when fetching URLs.
   * @return true if the data has already been downloaded successfully, or
   * false if the data needs to be downloaded.
   */
  def ensureDownloaded(activity: Activity,
            customText: String, fileConfigUrl: String,
            configVersion: String, dataPath: String,
            userAgent: String): Boolean = {
    val dest = new File(dataPath)
    if (dest.exists) {
      // Check version
      if (versionMatches(dest, configVersion)) {
        Log.i(LOG_TAG, "Versions match, no need to download.")
        return true
      }
    }
    val intent = PreconditionActivityHelper.createPreconditionIntent(
                activity, classOf[DownloaderActivity])
    intent.putExtra(EXTRA_CUSTOM_TEXT, customText)
    intent.putExtra(EXTRA_FILE_CONFIG_URL, fileConfigUrl)
    intent.putExtra(EXTRA_CONFIG_VERSION, configVersion)
    intent.putExtra(EXTRA_DATA_PATH, dataPath)
    intent.putExtra(EXTRA_USER_AGENT, userAgent)
    PreconditionActivityHelper.startPreconditionActivityAndFinish(
                activity, intent)
    false
  }

  /**
   * Delete a directory and all its descendants.
   * @param directory The directory to delete
   * @return true if the directory was deleted successfully.
   */
  def deleteData(directory: String): Boolean =
    deleteTree(new File(directory), true)

  private def deleteTree(base: File, deleteBase: Boolean): Boolean = {
    var result = true
    if (base.isDirectory) {
      for (child <- base.listFiles) {
        result &= deleteTree(child, true)
      }
    }
    if (deleteBase) {
      result &= base.delete()
    }
    result
  }

  private def versionMatches(dest: File, expectedVersion: String): Boolean = {
    val config = getLocalConfig(dest, LOCAL_CONFIG_FILE)
    (config != null) && (config.version equals expectedVersion)
  }

  private def getLocalConfig(destPath: File, configFilename: String): Config = {
    val configPath = new File(destPath, configFilename)
    val is =
      try { new FileInputStream(configPath) }
      catch { case e: FileNotFoundException => return null }
    try {
      ConfigHandler.parse(is)
    } catch {
      case e: Exception =>
        Log.e(LOG_TAG, "Unable to read local config file", e)
        null
    } finally {
      quietClose(is)
    }
  }

  private def quietClose(is: InputStream) {
    try {
      if (is != null) {
        is.close()
      }
    } catch {
      case e: IOException => // Don't care.
    }
  }

  private def quietClose(os: OutputStream) {
    try {
      if (os != null) {
        os.close()
      }
    } catch {
      case e: IOException => // Don't care.
    }
  }

  private class Config {
    def getSize: Long = mFiles.foldLeft(0L)(_ + _.getSize)
    var version: String = _
    val mFiles = new ListBuffer[Config.File]()
  }

  private object Config {
    class File(src: String, val dest: String, md5: String, size: Long) {
      val mParts = new ListBuffer[File.Part]()
      if (src != null) {
        mParts += new File.Part(src, md5, size)
      }
      def getSize: Long =
        mParts.filter(_.size > 0).foldLeft(0L)(_ + _.size)
    }
    object File {
      case class Part(src: String, md5: String, var size: Long)
    }
  }

  /**
   * <config version="">
   *   <file src="http:..." dest ="b.x" />
   *   <file dest="b.x">
   *     <part src="http:..." />
   *     ...
   *   ...
   * </config>
   *
   */
  private class ConfigHandler extends DefaultHandler {
    private val mConfig = new Config()

    @throws(classOf[SAXException])
    override def startElement(uri: String, localName: String, qName: String,
                              attributes: Attributes) {
      if (localName equals "config") {
        mConfig.version = getRequiredString(attributes, "version")
      } else if (localName equals "file") {
        val src = attributes.getValue("", "src")
        val dest = getRequiredString(attributes, "dest")
        val md5 = attributes.getValue("", "md5")
        val size = getLong(attributes, "size", -1)
        mConfig.mFiles += new Config.File(src, dest, md5, size)
      } else if (localName equals "part") {
        val src = getRequiredString(attributes, "src")
        val md5 = attributes.getValue("", "md5")
        val size = getLong(attributes, "size", -1)
        val length = mConfig.mFiles.size
        if (length > 0) {
          mConfig.mFiles(length-1).mParts += new Config.File.Part(src, md5, size)
        }
      }
    }

    @throws(classOf[SAXException])
    private def getRequiredString(attributes: Attributes,
                                  localName: String): String = {
      val result = attributes.getValue("", localName)
      if (result == null) {
        throw new SAXException("Expected attribute " + localName)
      }
      result
    }

    private def getLong(attributes: Attributes, localName: String,
                        defaultValue: Long): Long = {
      val value = attributes.getValue("", localName)
      if (value == null) defaultValue
      else java.lang.Long.parseLong(value)
    }

  } //class ConfigHandler

  private object ConfigHandler {

    @throws(classOf[SAXException])
    @throws(classOf[UnsupportedEncodingException])
    @throws(classOf[IOException])
    def parse(is: InputStream): Config = {
      val handler = new ConfigHandler()
      Xml.parse(is, Xml.findEncodingByName("UTF-8"), handler)
      handler.mConfig
    }

  }

  private final val MS_PER_SECOND = 1000L
  private final val MS_PER_MINUTE = 60 * 1000L
  private final val MS_PER_HOUR = 60 * 60 * 1000L
  private final val MS_PER_DAY = 24 * 60 * 60 * 1000L

  private final val LOCAL_CONFIG_FILE = ".downloadConfig"
  private final val LOCAL_CONFIG_FILE_TEMP = ".downloadConfig_temp"
  private final val LOCAL_FILTERED_FILE = ".downloadConfig_filtered"
  private final val EXTRA_CUSTOM_TEXT = "DownloaderActivity_custom_text"
  private final val EXTRA_FILE_CONFIG_URL = "DownloaderActivity_config_url"
  private final val EXTRA_CONFIG_VERSION = "DownloaderActivity_config_version"
  private final val EXTRA_DATA_PATH = "DownloaderActivity_data_path"
  private final val EXTRA_USER_AGENT = "DownloaderActivity_user_agent"

  private final val MSG_DOWNLOAD_SUCCEEDED = 0
  private final val MSG_DOWNLOAD_FAILED = 1
  private final val MSG_REPORT_PROGRESS = 2
  private final val MSG_REPORT_VERIFYING = 3

  private final val LOG_TAG = "Downloader"

}
