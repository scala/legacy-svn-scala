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

package com.google.android.photostream

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.{HttpResponse, HttpStatus, HttpEntity, HttpHost, HttpVersion}
import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.params.{BasicHttpParams, HttpParams, HttpProtocolParams}
import org.xmlpull.v1.{XmlPullParser, XmlPullParserException}

import java.io.{BufferedInputStream, BufferedOutputStream, ByteArrayOutputStream, Closeable, IOException, InputStream, InputStreamReader, OutputStream}
import java.util.{Calendar, GregorianCalendar}
import java.text.{ParseException, SimpleDateFormat}
import java.net.URL

import scala.collection.mutable.ListBuffer

import android.graphics.{Bitmap, BitmapFactory}
import android.net.Uri
import android.os.{Parcel, Parcelable}
import android.util.{Log, Xml}
import android.view.InflateException

/**
 * Utility class to interact with the Flickr REST-based web services.
 *
 * This class uses a default Flickr API key that you should replace with your
 * own if you reuse this code to redistribute it with your application(s).
 *
 * This class is used as a singleton and cannot be instanciated. Instead, you
 * must use {@link #get()} to retrieve the unique instance of this class.
 */
object Flickr {
  final val LOG_TAG = "Photostream"

  // IMPORTANT: Replace this Flickr API key with your own
  private final val API_KEY = "730e3a4f253b30adf30177df803d38c4"  

  private final val API_REST_HOST = "api.flickr.com"
  private final val API_REST_URL = "/services/rest/"
  private final val API_FEED_URL = "/services/feeds/photos_public.gne"

  private final val API_PEOPLE_FIND_BY_USERNAME = "flickr.people.findByUsername"
  private final val API_PEOPLE_GET_INFO = "flickr.people.getInfo"
  private final val API_PEOPLE_GET_PUBLIC_PHOTOS = "flickr.people.getPublicPhotos"
  private final val API_PEOPLE_GET_LOCATION = "flickr.photos.geo.getLocation"

  private final val PARAM_API_KEY = "api_key"
  private final val PARAM_METHOD = "method"
  private final val PARAM_USERNAME = "username"
  private final val PARAM_USERID = "user_id"
  private final val PARAM_PER_PAGE = "per_page"
  private final val PARAM_PAGE = "page"
  private final val PARAM_EXTRAS = "extras"
  private final val PARAM_PHOTO_ID = "photo_id"
  private final val PARAM_FEED_ID = "id"
  private final val PARAM_FEED_FORMAT = "format"

  private final val VALUE_DEFAULT_EXTRAS = "date_taken"
  private final val VALUE_DEFAULT_FORMAT = "atom"

  private final val RESPONSE_TAG_RSP = "rsp"
  private final val RESPONSE_ATTR_STAT = "stat"
  private final val RESPONSE_STATUS_OK = "ok"

  private final val RESPONSE_TAG_USER = "user"
  private final val RESPONSE_ATTR_NSID = "nsid"

  private final val RESPONSE_TAG_PHOTOS = "photos"
  private final val RESPONSE_ATTR_PAGE = "page"
  private final val RESPONSE_ATTR_PAGES = "pages"

  private final val RESPONSE_TAG_PHOTO = "photo"
  private final val RESPONSE_ATTR_ID = "id"
  private final val RESPONSE_ATTR_SECRET = "secret"
  private final val RESPONSE_ATTR_SERVER = "server"
  private final val RESPONSE_ATTR_FARM = "farm"
  private final val RESPONSE_ATTR_TITLE = "title"
  private final val RESPONSE_ATTR_DATE_TAKEN = "datetaken"

  private final val RESPONSE_TAG_PERSON = "person"
  private final val RESPONSE_ATTR_ISPRO = "ispro"
  private final val RESPONSE_ATTR_ICONSERVER = "iconserver"
  private final val RESPONSE_ATTR_ICONFARM = "iconfarm"
  private final val RESPONSE_TAG_USERNAME = "username"
  private final val RESPONSE_TAG_REALNAME = "realname"
  private final val RESPONSE_TAG_LOCATION = "location"
  private final val RESPONSE_ATTR_LATITUDE = "latitude"
  private final val RESPONSE_ATTR_LONGITUDE = "longitude"
  private final val RESPONSE_TAG_PHOTOSURL = "photosurl"
  private final val RESPONSE_TAG_PROFILEURL = "profileurl"
  private final val RESPONSE_TAG_MOBILEURL = "mobileurl"

  private final val RESPONSE_TAG_FEED = "feed"
  private final val RESPONSE_TAG_UPDATED = "updated"

  private final val PHOTO_IMAGE_URL =
    "http://farm%s.static.flickr.com/%s/%s_%s%s.jpg"
  private final val BUDDY_ICON_URL =
    "http://farm%s.static.flickr.com/%s/buddyicons/%s.jpg"
  private final val DEFAULT_BUDDY_ICON_URL =
    "http://www.flickr.com/images/buddyicon.jpg"

  private final val IO_BUFFER_SIZE = 4 * 1024

  private final val FLAG_DECODE_PHOTO_STREAM_WITH_SKIA = false

  private final val sInstance = new Flickr()

  /**
   * Defines the size of the image to download from Flickr.
   *
   * @see com.google.android.photostream.Flickr.Photo
   */
  object PhotoSize extends Enumeration {
    /**
     * Small square image (75x75 px).
     */
    val SMALL_SQUARE = SizeValue("_s", 75)
    /**
     * Thumbnail image (the longest side measures 100 px).
     */
    val THUMBNAIL = SizeValue("_t", 100)
    /**
     * Small image (the longest side measures 240 px).
     */
    val SMALL = SizeValue("_m", 240)
    /**
     * Medium image (the longest side measures 500 px).
     */
    val MEDIUM = SizeValue("", 500)
    /**
     * Large image (the longest side measures 1024 px).
     */
    val LARGE = SizeValue("_b", 1024)

    case class SizeValue(size: String, longSide: Int) extends Val(size) {
      override def toString: String = name + ", longSide=" + longSide
      def name: String = size
    }

  }
  type PhotoSize = PhotoSize.SizeValue

  /**
   * Represents the geographical location of a photo.
   */
  case class Location(var latitude: Float, var longitude: Float)

  /**
   * A Flickr user, in the strictest sense, is only defined by its NSID.
   * The NSID is usually obtained by {@link Flickr#findByUserName(String)
   * looking up a user by its user name}.
   *
   * To obtain more information about a given user, refer to the UserInfo class.
   *
   * @see Flickr#findByUserName(String)
   * @see Flickr#getUserInfo(com.google.android.photostream.Flickr.User) 
   * @see com.google.android.photostream.Flickr.UserInfo
   */
  class User private[photostream] (val id: String) extends AnyRef with Parcelable {

    private def this(in: Parcel) = this(in.readString())

    override def toString: String = "User[" + id + "]"

    def describeContents: Int = 0

    def writeToParcel(dest: Parcel, flags: Int) {
      dest writeString id
    }

  }

  object User {
    /**
     * Creates a new instance of this class from the specified Flickr NSID.
     *
     * @param id The NSID of the Flickr user.
     *
     * @return An instance of User whose id might not be valid.
     */
    def fromId(id: String): User = new User(id)

    final val CREATOR = new Parcelable.Creator[User]() {
      def createFromParcel(in: Parcel): User = new User(in)
      def newArray(size: Int): Array[User] = new Array[User](size)
    }
  }

  /**
   * A set of information for a given Flickr user. The information exposed include:
   * - The user's NSDID
   * - The user's name
   * - The user's real name
   * - The user's location
   * - The URL to the user's photos
   * - The URL to the user's profile
   * - The URL to the user's mobile web site
   * - Whether the user has a pro account
   */
  class UserInfo private (nsid: String, in: Parcel) extends AnyRef with Parcelable {
    private[photostream] var mId: String = nsid
    private[photostream] var mUserName: String = _
    private[photostream] var mRealName: String = _
    private[photostream] var mLocation: String = _
    private[photostream] var mPhotosUrl: String = _
    private[photostream] var mProfileUrl: String = _
    private[photostream] var mMobileUrl: String = _
    private[photostream] var mIsPro: Boolean = _
    private[photostream] var mIconServer: String = _
    private[photostream] var mIconFarm: String = _

    private[photostream] def this(nsid: String) =
      this(nsid, null)

    private[photostream] def this(in: Parcel) =
      this(null, in)

    init()

    private def init() {
      assert(nsid != null && in == null || nsid == null && in != null) // xor
      if (in != null) {
        mId = in.readString()
        mUserName = in.readString()
        mRealName = in.readString()
        mLocation = in.readString()
        mPhotosUrl = in.readString()
        mProfileUrl = in.readString()
        mMobileUrl = in.readString()
        mIsPro = in.readInt() == 1
        mIconServer = in.readString()
        mIconFarm = in.readString()
      }
    }

    /**
     * Returns the Flickr NSID that identifies this user.
     *
     * @return The Flickr NSID.
     */
    def id: String = mId

    /**
     * Returns the user's name. This is the name that the user authenticates with,
     * and the name that Flickr uses in the URLs
     * (for instance, http://flickr.com/photos/romainguy, where romainguy is the user
     * name.)
     *
     * @return The user's Flickr name.
     */
    def userName: String = mUserName

    /**
     * Returns the user's real name. The real name is chosen by the user when
     * creating his account and might not reflect his civil name.
     *
     * @return The real name of the user.
     */
    def realName: String = mRealName

    /**
     * Returns the user's location, if publicly exposed.
     *
     * @return The location of the user.
     */
    def location: String = mLocation

    /**
     * Returns the URL to the photos of the user. For instance,
     * http://flickr.com/photos/romainguy.
     *
     * @return The URL to the photos of the user.
     */
    def photosUrl: String = mPhotosUrl

    /**
     * Returns the URL to the profile of the user. For instance,
     * http://flickr.com/people/romainguy/.
     *
     * @return The URL to the photos of the user.
     */
    def profileUrl: String = mProfileUrl

    /**
     * Returns the mobile URL of the user.
     *
     * @return The mobile URL of the user.
     */
    def mobileUrl: String = mMobileUrl

    /**
     * Indicates whether the user owns a pro account.
     *
     * @return true, if the user has a pro account, false otherwise.
     */
    def isPro: Boolean = mIsPro

    /**
     * Returns the URL to the user's buddy icon. The buddy icon is a 48x48
     * image chosen by the user. If no icon can be found, a default image
     * URL is returned.
     *
     * @return The URL to the user's buddy icon.
     */
    def buddyIconUrl: String =
      if (mIconFarm == null || mIconServer == null || mId == null)
        DEFAULT_BUDDY_ICON_URL
      else
        String.format(BUDDY_ICON_URL, mIconFarm, mIconServer, mId)

    /**
     * Loads the user's buddy icon as a Bitmap. The user's buddy icon is loaded
     * from the URL returned by {@link #buddyIconUrl}. The buddy icon is
     * not cached locally.
     *
     * @return A 48x48 bitmap if the icon was loaded successfully or null otherwise.
     */
    def loadBuddyIcon(): Bitmap = {
      var bitmap: Bitmap = null
      var in: InputStream = null
      var out: OutputStream = null

      try {
        in = new BufferedInputStream(new URL(buddyIconUrl).openStream(),
                        IO_BUFFER_SIZE)

        if (FLAG_DECODE_PHOTO_STREAM_WITH_SKIA) {
          bitmap = BitmapFactory.decodeStream(in)
        } else {
          val dataStream = new ByteArrayOutputStream()
          out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE)
          copy(in, out)
          out.flush()

          val data = dataStream.toByteArray
          bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
      } catch {
        case e: IOException =>
          Log.e(Flickr.LOG_TAG, "Could not load buddy icon: " + this, e)
      } finally {
        closeStream(in)
        closeStream(out)
      }

      bitmap
    }

    override def toString: String =
      mRealName + " (" + mUserName + ", " + mId + ")"

    def describeContents: Int = 0

    def writeToParcel(dest: Parcel, flags: Int) {
      dest writeString mId
      dest writeString mUserName
      dest writeString mRealName
      dest writeString mLocation
      dest writeString mPhotosUrl
      dest writeString mProfileUrl
      dest writeString mMobileUrl
      dest writeInt (if (mIsPro) 1 else 0)
      dest writeString mIconServer
      dest writeString mIconFarm
    }

  }

  object UserInfo {
    final val CREATOR = new Parcelable.Creator[UserInfo]() {
      def createFromParcel(in: Parcel): UserInfo = new UserInfo(in)
      def newArray(size: Int): Array[UserInfo] = new Array[UserInfo](size)
    }
  }

  /**
   * A photo is represented by a title, the date at which it was taken and a URL.
   * The URL depends on the desired {@link com.google.android.photostream.Flickr.PhotoSize}.
   */
  class Photo private (in: Parcel) extends AnyRef with Parcelable {
    private[photostream] var mId: String = _
    private[photostream] var mSecret: String = _
    private[photostream] var mServer: String = _
    private[photostream] var mFarm: String = _
    private[photostream] var mTitle: String = _
    private[photostream] var mDate: String = _

    private[photostream] def this() = this(null)

    init()

    def init() {
      if (in != null) {
        mId = in.readString()
        mSecret = in.readString()
        mServer = in.readString()
        mFarm = in.readString()
        mTitle = in.readString()
        mDate = in.readString()
      }
    }

    def id: String = mId

    /**
     * Returns the title of the photo, if specified.
     *
     * @return The title of the photo. The returned value can be empty or null.
     */
    def title: String = mTitle

    /**
     * Returns the date at which the photo was taken, formatted in the current locale
     * with the following pattern: MMMM d, yyyy.
     *
     * @return The title of the photo. The returned value can be empty or null.
     */
    def date: String = mDate

    /**
     * Returns the URL to the photo for the specified size.
     *
     * @param photoSize The required size of the photo.
     *
     * @return A URL to the photo for the specified size.
     *
     * @see com.google.android.photostream.Flickr.PhotoSize
     */
    def getUrl(photoSize: PhotoSize): String =
      String.format(PHOTO_IMAGE_URL, mFarm, mServer, mId, mSecret, photoSize.size)

    /**
     * Loads a Bitmap representing the photo for the specified size. The Bitmap
     * is loaded from the URL returned by
     * {@link #getUrl(com.google.android.photostream.Flickr.PhotoSize)}. 
     *
     * @param size The size of the photo to load.
     *
     * @return A Bitmap whose longest size is the same as the longest side of
     *         the specified {@link com.google.android.photostream.Flickr.PhotoSize},
     *         or null if the photo could not be loaded.
     */
    def loadPhotoBitmap(size: PhotoSize): Bitmap = {
      var bitmap: Bitmap = null
      var in: InputStream = null
      var out: BufferedOutputStream = null

      try {
        in = new BufferedInputStream(new URL(getUrl(size)).openStream(),
                        IO_BUFFER_SIZE)

        if (FLAG_DECODE_PHOTO_STREAM_WITH_SKIA) {
          bitmap = BitmapFactory.decodeStream(in);
        } else {
          val dataStream = new ByteArrayOutputStream()
          out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE)
          copy(in, out)
          out.flush()

          val data = dataStream.toByteArray
          bitmap = BitmapFactory.decodeByteArray(data, 0, data.length)
        }
      } catch {
        case e: IOException =>
          Log.e(Flickr.LOG_TAG, "Could not load photo: " + this, e)
      } finally {
        closeStream(in)
        closeStream(out)
      }

      bitmap
    }

    override def toString: String =
      mTitle + ", " + mDate + " @" + mId

    def describeContents: Int = 0

    def writeToParcel(dest: Parcel, flags: Int) {
      dest writeString mId
      dest writeString mSecret
      dest writeString mServer
      dest writeString mFarm
      dest writeString mTitle
      dest writeString mDate
    }

  }

  object Photo {
    final val CREATOR = new Parcelable.Creator[Photo]() {
      def createFromParcel(in: Parcel): Photo = new Photo(in)
      def newArray(size: Int): Array[Photo] = new Array[Photo](size)
    }
  }

  /**
   * A list of {@link com.google.android.photostream.Flickr.Photo photos}.
   * A list represents a series of photo on a page from the user's photostream,
   * a list is therefore associated with a page index and a page count. The
   * page index and the page count both depend on the number of photos per page.
   */
  class PhotoList {
    private[photostream] var mPhotos: ListBuffer[Photo] = _
    private[photostream] var mPage: Int = _
    private[photostream] var mPageCount: Int = _

    private[photostream] def add(photo: Photo) {
      mPhotos += photo
    }

    /**
     * Returns the photo at the specified index in the current set. An
     * {@link ArrayIndexOutOfBoundsException} can be thrown if the index is
     * less than 0 or greater then or equals to {@link #getCount()}.
     *
     * @param index The index of the photo to retrieve from the list.
     *
     * @return A valid {@link com.google.android.photostream.Flickr.Photo}.
     */
    def get(index: Int): Photo = mPhotos(index)

    /**
     * Returns the number of photos in the list.
     *
     * @return A positive integer, or 0 if the list is empty.
     */
    def getCount: Int = mPhotos.size

    /**
     * Returns the page index of the photos from this list.
     *
     * @return The index of the Flickr page that contains the photos of this list.
     */
    def getPage: Int = mPage

    /**
     * Returns the total number of photo pages.
     *
     * @return A positive integer, or 0 if the photostream is empty.
     */
    def getPageCount: Int = mPageCount

  }

  /**
   * Returns the unique instance of this class.
   *
   * @return The unique instance of this class.
   */
  def get: Flickr = sInstance 

  /**
   * Builds an HTTP GET request for the specified Flickr API method. The
   * returned request contains the web service path, the query parameter for
   * the API KEY and the query parameter for the specified method.
   *
   * @param method The Flickr API method to invoke.
   *
   * @return A Uri.Builder containing the GET path, the API key and the method
   *         already encoded.
   */
  private def buildGetMethod(method: String): Uri.Builder = {
    val builder = new Uri.Builder()
    builder.path(API_REST_URL).appendQueryParameter(PARAM_API_KEY, API_KEY)
    builder.appendQueryParameter(PARAM_METHOD, method)
    builder
  }

  /**
   * Copy the content of the input stream into the output stream, using a temporary
   * byte array buffer whose size is defined by {@link #IO_BUFFER_SIZE}.
   *
   * @param in The input stream to copy from.
   * @param out The output stream to copy to.
   *
   * @throws IOException If any error occurs during the copy.
   */
  @throws(classOf[IOException])
  private def copy(in: InputStream, out: OutputStream) {
    val b = new Array[Byte](IO_BUFFER_SIZE)
    var read = in read b
    while (read != -1) {
      out.write(b, 0, read)
      read = in read b
    }
  }

  /**
   * Closes the specified stream.
   *
   * @param stream The stream to close.
   */
  private def closeStream(stream: Closeable) {
    if (stream != null) {
      try {
        stream.close()
      } catch {
        case e: IOException =>
          Log.e(Flickr.LOG_TAG, "Could not close stream", e)
      }
    }
  }

  /**
   * Response handler used with
   * {@link Flickr#executeRequest(org.apache.http.client.methods.HttpGet,
   * com.google.android.photostream.Flickr.ResponseHandler)}. The handler is
   * invoked when a response is sent by the server. The response is made
   * available as an input stream. 
   */
  private trait ResponseHandler {
    /**
     * Processes the responses sent by the HTTP server following a GET request.
     *
     * @param in The stream containing the server's response.
     *
     * @throws IOException
     */
    @throws(classOf[IOException])
    def handleResponse(in: InputStream)
  }

  /**
   * Response parser used with {@link Flickr#parseResponse(java.io.InputStream,
   * com.google.android.photostream.Flickr.ResponseParser)}. When Flickr returns
   * a valid response, this parser is invoked to process the XML response.
   */
  private trait ResponseParser {
    /**
     * Processes the XML response sent by the Flickr web service after a successful
     * request.
     *
     * @param parser The parser containing the XML responses.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    @throws(classOf[XmlPullParserException])
    @throws(classOf[IOException])
    def parseResponse(parser: XmlPullParser)
  }

}

class Flickr {
  import Flickr._  // companion object

  private var mClient: HttpClient = _

  init()

  private def init() {
    val params = new BasicHttpParams()
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1)
    HttpProtocolParams.setContentCharset(params, "UTF-8")

    val registry = new SchemeRegistry()
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory, 80))

    val manager = new ThreadSafeClientConnManager(params, registry)

    mClient = new DefaultHttpClient(manager, params)
  }

  /**
   * Finds a user by its user name. This method will return an instance of
   * {@link com.google.android.photostream.Flickr.User} containing the user's
   * NSID, or null if the user could not be found.
   *
   * The returned User contains only the user's NSID. To retrieve more information
   * about the user, please refer to
   * {@link #getUserInfo(com.google.android.photostream.Flickr.User)}
   *
   * @param userName The name of the user to find.
   *
   * @return A User instance with a valid NSID, or null if the user cannot be found.
   *
   * @see #getUserInfo(com.google.android.photostream.Flickr.User)
   * @see com.google.android.photostream.Flickr.User
   * @see com.google.android.photostream.Flickr.UserInfo
   */
  def findByUserName(userName: String): User = {
    val uri: Uri.Builder = buildGetMethod(API_PEOPLE_FIND_BY_USERNAME)
    uri.appendQueryParameter(PARAM_USERNAME, userName)

    val get = new HttpGet(uri.build().toString)
    val userId = new Array[String](1)

    try {
      executeRequest(get, new ResponseHandler() {
        @throws(classOf[IOException])
        def handleResponse(in: InputStream) {
          parseResponse(in, new ResponseParser() {
            @throws(classOf[XmlPullParserException])
            @throws(classOf[IOException])
            def parseResponse(parser: XmlPullParser) {
              parseUser(parser, userId)
            }
          })
        }
      })

      if (userId(0) != null) new User(userId(0)) else null
    } catch {
      case e: IOException =>
        Log.e(LOG_TAG, "Could not find the user with name: " + userName)
        null
    }
  }

  /**
   * Retrieves a public set of information about the specified user. The user
   * can either be {@link com.google.android.photostream.Flickr.User#fromId(String)
   * created manually}
   * or {@link #findByUserName(String) obtained from a user name}.
   *
   * @param user The user, whose NSID is valid, to retrive public information for.
   *
   * @return An instance of {@link com.google.android.photostream.Flickr.UserInfo}
   *         or null if the user could not be found.
   *
   * @see com.google.android.photostream.Flickr.UserInfo
   * @see com.google.android.photostream.Flickr.User
   * @see #findByUserName(String) 
   */
  def getUserInfo(user: User): UserInfo = {
    val nsid = user.id
    val uri: Uri.Builder = buildGetMethod(API_PEOPLE_GET_INFO)
    uri.appendQueryParameter(PARAM_USERID, nsid)

    val get = new HttpGet(uri.build().toString)

    try {
      val info = new UserInfo(nsid)

      executeRequest(get, new ResponseHandler() {
        @throws(classOf[IOException])
        def handleResponse(in: InputStream) {
          parseResponse(in, new ResponseParser() {
            @throws(classOf[XmlPullParserException])
            @throws(classOf[IOException])
            def parseResponse(parser: XmlPullParser) {
              parseUserInfo(parser, info)
            }
          })
        }
      })

      info
    } catch {
      case e: IOException =>
        Log.e(LOG_TAG, "Could not find the user with id: " + nsid)
        null
    }
  }

  /**
   * Retrieves a list of photos for the specified user. The list contains at
   * most the number of photos specified by <code>perPage</code>. The photos
   * are retrieved starting a the specified page index. For instance, if a user
   * has 10 photos in his photostream, calling getPublicPhotos(user, 5, 2) will
   * return the last 5 photos of the photo stream.
   *
   * The page index starts at 1, not 0.
   *
   * @param user The user to retrieve photos from.
   * @param perPage The maximum number of photos to retrieve.
   * @param page The index (starting at 1) of the page in the photostream.
   *
   * @return A list of at most perPage photos.
   *
   * @see com.google.android.photostream.Flickr.Photo
   * @see com.google.android.photostream.Flickr.PhotoList
   * @see #downloadPhoto(com.google.android.photostream.Flickr.Photo,
   *          com.google.android.photostream.Flickr.PhotoSize, java.io.OutputStream) 
   */
  def getPublicPhotos(user: User, perPage: Int, page: Int): PhotoList = {
    val uri: Uri.Builder = buildGetMethod(API_PEOPLE_GET_PUBLIC_PHOTOS)
    uri.appendQueryParameter(PARAM_USERID, user.id)
    uri.appendQueryParameter(PARAM_PER_PAGE, String.valueOf(perPage))
    uri.appendQueryParameter(PARAM_PAGE, String.valueOf(page))
    uri.appendQueryParameter(PARAM_EXTRAS, VALUE_DEFAULT_EXTRAS)

    val get = new HttpGet(uri.build().toString)
    val photos = new PhotoList()

    try {
      executeRequest(get, new ResponseHandler() {
        @throws(classOf[IOException])
        def handleResponse(in: InputStream) {
          parseResponse(in, new ResponseParser() {
            @throws(classOf[XmlPullParserException])
            @throws(classOf[IOException])
            def parseResponse(parser: XmlPullParser) {
              parsePhotos(parser, photos)
            }
          })
        }
      })
    } catch {
      case e: IOException =>
        Log.e(LOG_TAG, "Could not find photos for user: " + user)
    }

    photos
  }

  /**
   * Retrieves the geographical location of the specified photo. If the photo
   * has no geodata associated with it, this method returns null.
   *
   * @param photo The photo to get the location of.
   *
   * @return The geo location of the photo, or null if the photo has no geodata
   *         or the photo cannot be found.
   *
   * @see com.google.android.photostream.Flickr.Location
   */
  def getLocation(photo: Flickr.Photo): Location = {
    val uri: Uri.Builder = buildGetMethod(API_PEOPLE_GET_LOCATION)
    uri.appendQueryParameter(PARAM_PHOTO_ID, photo.id)

    val get = new HttpGet(uri.build().toString)
    val location = new Location(0.0f, 0.0f)

    try {
      executeRequest(get, new ResponseHandler() {
        @throws(classOf[IOException])
        def handleResponse(in: InputStream) {
          parseResponse(in, new ResponseParser() {
            @throws(classOf[XmlPullParserException])
            @throws(classOf[IOException])
            def parseResponse(parser: XmlPullParser) {
              parsePhotoLocation(parser, location)
            }
          })
        }
      })
      location
    } catch {
      case e: IOException =>
        Log.e(LOG_TAG, "Could not find location for photo: " + photo)
        null
    }
  }

  /**
   * Checks the specified user's feed to see if any updated occured after the
   * specified date.
   *
   * @param user The user whose feed must be checked.
   * @param reference The date after which to check for updates.
   *
   * @return True if any update occured after the reference date, false otherwise.
   */
  def hasUpdates(user: User, reference: Calendar): Boolean = {
    val uri = new Uri.Builder()
    uri.path(API_FEED_URL)
    uri.appendQueryParameter(PARAM_FEED_ID, user.id)
    uri.appendQueryParameter(PARAM_FEED_FORMAT, VALUE_DEFAULT_FORMAT)

    val get = new HttpGet(uri.build().toString)
    val updated = new Array[Boolean](1)

    try {
      executeRequest(get, new ResponseHandler() {
        @throws(classOf[IOException])
        def handleResponse(in: InputStream) {
          parseFeedResponse(in, new ResponseParser() {
            @throws(classOf[XmlPullParserException])
            @throws(classOf[IOException])
            def parseResponse(parser: XmlPullParser) {
              updated(0) = parseUpdated(parser, reference)
            }
          })
        }
      })
    } catch {
      case e: IOException =>
        Log.e(LOG_TAG, "Could not find feed for user: " + user)
    }

    updated(0)
  }

  /**
   * Downloads the specified photo at the specified size in the specified destination.
   *
   * @param photo The photo to download.
   * @param size The size of the photo to download.
   * @param destination The output stream in which to write the downloaded photo.
   *
   * @throws IOException If any network exception occurs during the download.
   */
  @ throws(classOf[IOException])
  def downloadPhoto(photo: Photo, size: PhotoSize, destination: OutputStream) {
    val out = new BufferedOutputStream(destination, IO_BUFFER_SIZE)
    val url = photo.getUrl(size)
    val get = new HttpGet(url)

    var entity: HttpEntity = null
    try {
      val response: HttpResponse = mClient execute get
      if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
        entity = response.getEntity
        entity writeTo out
        out.flush()
      }
    } finally {
      if (entity != null) {
        entity.consumeContent()
      }
    }
  }

  @throws(classOf[IOException])
  @throws(classOf[XmlPullParserException])
  private def parseUpdated(parser: XmlPullParser, reference: Calendar): Boolean = {
    val depth = parser.getDepth

    var tpe = parser.next()
    while ((tpe != XmlPullParser.END_TAG || parser.getDepth > depth) &&
           tpe != XmlPullParser.END_DOCUMENT) {
      if (tpe == XmlPullParser.START_TAG) {
        val name = parser.getName
        if (RESPONSE_TAG_UPDATED equals name) {
          if (parser.next() == XmlPullParser.TEXT) {
            val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
              val text = parser.getText.replace('T', ' ').replace('Z', ' ')
              val calendar = new GregorianCalendar()
              calendar.setTimeInMillis(format.parse(text).getTime)

              return calendar.after(reference);
            } catch {
              case e: ParseException => // Ignore
            }
          }
        }
      }
      tpe = parser.next()
    }

    false
  }    

  @throws(classOf[XmlPullParserException])
  @throws(classOf[IOException])
  private def parsePhotos(parser: XmlPullParser, photos: PhotoList) {
    var parseFormat: SimpleDateFormat = null
    var outputFormat: SimpleDateFormat = null

    val depth = parser.getDepth

    var tpe = parser.next()
    while ((tpe != XmlPullParser.END_TAG || parser.getDepth > depth) &&
           tpe != XmlPullParser.END_DOCUMENT) {
      if (tpe == XmlPullParser.START_TAG) {
        val name = parser.getName
        if (RESPONSE_TAG_PHOTOS equals name) {
          photos.mPage = java.lang.Integer.parseInt(parser.getAttributeValue(null, RESPONSE_ATTR_PAGE))
          photos.mPageCount = java.lang.Integer.parseInt(parser.getAttributeValue(null,
                        RESPONSE_ATTR_PAGES));
          photos.mPhotos = new ListBuffer[Photo]()
        } else if (RESPONSE_TAG_PHOTO equals name) {
          val photo = new Photo()
          photo.mId = parser.getAttributeValue(null, RESPONSE_ATTR_ID)
          photo.mSecret = parser.getAttributeValue(null, RESPONSE_ATTR_SECRET)
          photo.mServer = parser.getAttributeValue(null, RESPONSE_ATTR_SERVER)
          photo.mFarm = parser.getAttributeValue(null, RESPONSE_ATTR_FARM)
          photo.mTitle = parser.getAttributeValue(null, RESPONSE_ATTR_TITLE)
          photo.mDate = parser.getAttributeValue(null, RESPONSE_ATTR_DATE_TAKEN)

          if (parseFormat == null) {
            parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            outputFormat = new SimpleDateFormat("MMMM d, yyyy")
          }

          try {
            photo.mDate = outputFormat.format(parseFormat.parse(photo.mDate))
          } catch {
            case e: ParseException =>
              Log.w(LOG_TAG, "Could not parse photo date", e)
          }

          photos add photo
        }
      }
      tpe = parser.next()
    }
  }

  @throws(classOf[XmlPullParserException])
  @throws(classOf[IOException])
  private def parsePhotoLocation(parser: XmlPullParser, location: Location) {
    val depth = parser.getDepth

    var tpe = parser.next()
    while ((tpe != XmlPullParser.END_TAG || parser.getDepth() > depth) &&
           tpe != XmlPullParser.END_DOCUMENT) {
      if (tpe == XmlPullParser.START_TAG) {
        val name = parser.getName
        if (RESPONSE_TAG_LOCATION equals name) {
          try {
            location.latitude =
              parser.getAttributeValue(null, RESPONSE_ATTR_LATITUDE).toFloat
            location.longitude =
              parser.getAttributeValue(null, RESPONSE_ATTR_LONGITUDE).toFloat
          } catch {
            case e: NumberFormatException =>
              throw new XmlPullParserException("Could not parse lat/lon", parser, e)
          }
        }
      }
      tpe = parser.next()
    }
  }

  @throws(classOf[XmlPullParserException])
  @throws(classOf[IOException])
  private def parseUser(parser: XmlPullParser, userId: Array[String]) {
    val depth = parser.getDepth

    var tpe = parser.next()
    while ((tpe != XmlPullParser.END_TAG || parser.getDepth > depth) &&
           tpe != XmlPullParser.END_DOCUMENT) {
      if (tpe == XmlPullParser.START_TAG) {
        val name = parser.getName
        if (RESPONSE_TAG_USER equals name) {
          userId(0) = parser.getAttributeValue(null, RESPONSE_ATTR_NSID)
        }
      }
      tpe = parser.next()
    }
  }

  @throws(classOf[XmlPullParserException])
  @throws(classOf[IOException])
  private def parseUserInfo(parser: XmlPullParser, info: UserInfo) {
    val depth = parser.getDepth

    var tpe = parser.next()
    while ((tpe != XmlPullParser.END_TAG || parser.getDepth > depth) &&
           tpe != XmlPullParser.END_DOCUMENT) {
      if (tpe == XmlPullParser.START_TAG) parser.getName match {
        case RESPONSE_TAG_PERSON =>
          info.mIsPro = "1" equals parser.getAttributeValue(null, RESPONSE_ATTR_ISPRO)
          info.mIconServer = parser.getAttributeValue(null, RESPONSE_ATTR_ICONSERVER)
          info.mIconFarm = parser.getAttributeValue(null, RESPONSE_ATTR_ICONFARM)
        case RESPONSE_TAG_USERNAME =>
          if (parser.next() == XmlPullParser.TEXT) {
            info.mUserName = parser.getText
          }
        case RESPONSE_TAG_REALNAME =>
          if (parser.next() == XmlPullParser.TEXT) {
            info.mRealName = parser.getText
          }
        case RESPONSE_TAG_LOCATION =>
          if (parser.next() == XmlPullParser.TEXT) {
            info.mLocation = parser.getText
          }
        case RESPONSE_TAG_PHOTOSURL =>
          if (parser.next() == XmlPullParser.TEXT) {
            info.mPhotosUrl = parser.getText
          }
        case RESPONSE_TAG_PROFILEURL =>
          if (parser.next() == XmlPullParser.TEXT) {
            info.mProfileUrl = parser.getText
          }
        case RESPONSE_TAG_MOBILEURL =>
          if (parser.next() == XmlPullParser.TEXT) {
            info.mMobileUrl = parser.getText
          }
        case _ =>
      }
      tpe = parser.next()
    }
  }

  /**
   * Parses a valid Flickr XML response from the specified input stream. When
   * the Flickr response contains the OK tag, the response is sent to the
   * specified response parser.
   *
   * @param in The input stream containing the response sent by Flickr.
   * @param responseParser The parser to use when the response is valid.
   * 
   * @throws IOException
   */
  @throws(classOf[IOException])
  private def parseResponse(in: InputStream, responseParser: ResponseParser) {
    val parser = Xml.newPullParser()
    try {
      parser.setInput(new InputStreamReader(in))

      var tpe = parser.next()
      while (tpe != XmlPullParser.START_TAG &&
             tpe != XmlPullParser.END_DOCUMENT) {
        // Empty
      }

      if (tpe != XmlPullParser.START_TAG) {
        throw new InflateException(parser.getPositionDescription
                        + ": No start tag found!")
      }

      val name = parser.getName
      if (RESPONSE_TAG_RSP equals name) {
        val value = parser.getAttributeValue(null, RESPONSE_ATTR_STAT)
        if (!RESPONSE_STATUS_OK.equals(value)) {
          throw new IOException("Wrong status: " + value)
        }
      }

      responseParser parseResponse parser

    } catch {
      case e: XmlPullParserException =>
       val ioe = new IOException("Could not parser the response")
       ioe.initCause(e)
       throw ioe
    }
  }

  /**
   * Parses a valid Flickr Atom feed response from the specified input stream.
   *
   * @param in The input stream containing the response sent by Flickr.
   * @param responseParser The parser to use when the response is valid.
   *
   * @throws IOException
   */
  @throws(classOf[IOException])
  private def parseFeedResponse(in: InputStream, responseParser: ResponseParser) {
    val parser = Xml.newPullParser()
    try {
      parser setInput new InputStreamReader(in)

      var tpe = parser.next()
      while (tpe != XmlPullParser.START_TAG &&
             tpe != XmlPullParser.END_DOCUMENT) {
        // Empty
      }

      if (tpe != XmlPullParser.START_TAG) {
        throw new InflateException(parser.getPositionDescription
                        + ": No start tag found!")
      }

      val name = parser.getName
      if (RESPONSE_TAG_FEED equals name) {
        responseParser parseResponse parser
      } else {
        throw new IOException("Wrong start tag: " + name)            
      }

    } catch {
      case e: XmlPullParserException =>
        val ioe = new IOException("Could not parser the response")
        ioe.initCause(e)
        throw ioe
    }
  }

  /**
   * Executes an HTTP request on Flickr's web service. If the response is ok,
   * the content is sent to the specified response handler.
   *
   * @param get The GET request to executed.
   * @param handler The handler which will parse the response.
   * 
   * @throws IOException
   */
  @throws(classOf[IOException])
  private def executeRequest(get: HttpGet, handler: ResponseHandler) {
    var entity: HttpEntity = null
    val host = new HttpHost(API_REST_HOST, 80, "http")
    try {
      val response: HttpResponse = mClient.execute(host, get)
      if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
        entity = response.getEntity
        handler handleResponse entity.getContent
      }
    } finally {
      if (entity != null) {
        entity.consumeContent()
      }
    }
  }

}
