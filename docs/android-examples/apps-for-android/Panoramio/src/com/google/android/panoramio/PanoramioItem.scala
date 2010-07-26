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

package com.google.android.panoramio

import com.google.android.maps.GeoPoint

import android.graphics.Bitmap
import android.os.{Parcel, Parcelable}

/**
 * Holds one item returned from the Panoramio server. This includes
 * the bitmap along with other meta info.
 *
 */
class PanoramioItem private (in: Parcel, id: Long, thumbUrl: String, b: Bitmap,
                             latitudeE6: Int, longitudeE6: Int,
                             title: String, owner: String,
                             ownerUrl: String, photoUrl: String)
extends AnyRef with Parcelable {
  import PanoramioItem._  // companion object

  private var mId: Long = _
  private var mBitmap: Bitmap = _
  private var mLocation: GeoPoint = _
  private var mTitle: String = _
  private var mOwner: String = _
  private var mThumbUrl: String = _
  private var mOwnerUrl: String = _
  private var mPhotoUrl: String = _

  def this(in: Parcel) =
    this(in, 0, null, null, 0, 0, null, null, null, null)

  def this(id: Long, thumbUrl: String, b: Bitmap,
                             latitudeE6: Int, longitudeE6: Int,
                             title: String, owner: String,
                             ownerUrl: String, photoUrl: String) =
    this(null, id, thumbUrl, b, latitudeE6, longitudeE6,
         title, owner, ownerUrl, photoUrl)

  if (in != null) {
    mId = in.readLong()
    mBitmap = Bitmap.CREATOR.createFromParcel(in)
    mLocation = new GeoPoint(in.readInt(), in.readInt())
    mTitle = in.readString()
    mOwner = in.readString()
    mThumbUrl = in.readString()
    mOwnerUrl = in.readString()
    mPhotoUrl = in.readString()
  } else {
    mBitmap = b
    mLocation = new GeoPoint(latitudeE6, longitudeE6)
    mTitle = title
    mOwner = owner
    mThumbUrl = thumbUrl
    mOwnerUrl = ownerUrl
    mPhotoUrl = photoUrl
  }

  def getId: Long = mId

  def getBitmap: Bitmap = mBitmap

  def getLocation: GeoPoint = mLocation

  def getTitle: String = mTitle
    
  def getOwner: String = mOwner
    
  def getThumbUrl: String = mThumbUrl

  def getOwnerUrl: String = mOwnerUrl

  def getPhotoUrl: String = mPhotoUrl

  def describeContents: Int = 0

  def writeToParcel(parcel: Parcel, flags: Int) {
    parcel writeLong mId
    mBitmap.writeToParcel(parcel, 0)
    parcel writeInt mLocation.getLatitudeE6
    parcel writeInt mLocation.getLongitudeE6
    parcel writeString mTitle
    parcel writeString mOwner
    parcel writeString mThumbUrl
    parcel writeString mOwnerUrl
    parcel writeString mPhotoUrl
  }
}

object PanoramioItem {

  final val CREATOR = new Parcelable.Creator[PanoramioItem]() {
    def createFromParcel(in: Parcel): PanoramioItem = new PanoramioItem(in)
    def newArray(size: Int): Array[PanoramioItem] =
      new Array[PanoramioItem](size)
  }

}
