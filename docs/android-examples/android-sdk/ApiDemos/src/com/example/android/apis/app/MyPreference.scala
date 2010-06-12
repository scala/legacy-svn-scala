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

import android.content.Context
import android.content.res.TypedArray
import android.os.{Parcel, Parcelable}
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

/**
 * This is an example of a custom preference type. The preference counts the
 * number of clicks it has received and stores/retrieves it from the storage.
 */
object MyPreference {
  /**
   * SavedState, a subclass of {@link BaseSavedState}, will store the state
   * of MyPreference, a subclass of Preference.
   * <p>
   * It is important to always call through to super methods.
   */
  private object SaveState {
    final val CREATOR = new Parcelable.Creator[SavedState] {
      def createFromParcel(in: Parcel): SavedState = {
        new SavedState(in)
      }

      def newArray(size: Int): Array[SavedState] = {
        new Array[SavedState](size)
      }
    }
  }

  private class SavedState(source: Parcel) extends View.BaseSavedState(source) {
    var clickCounter = source.readInt

    override def writeToParcel(dest: Parcel, flags: Int) {
      super.writeToParcel(dest, flags)
            
      // Save the click counter
      dest writeInt clickCounter
    }

  }

  private class SavedState2(source: Parcelable) extends View.BaseSavedState(source) {
    var clickCounter: Int = _

    override def writeToParcel(dest: Parcel, flags: Int) {
      super.writeToParcel(dest, flags)
            
      // Save the click counter
      dest writeInt clickCounter
    }

  }

}

class MyPreference(context: Context, attrs: AttributeSet)
extends Preference(context, attrs) {
  import MyPreference._  // companion object

  private var mClickCounter: Int = _

  override protected def onBindView(view: View) {
    super.onBindView(view)
        
    // Set our custom views inside the layout
    val myTextView =
      view.findViewById(R.id.mypreference_widget).asInstanceOf[TextView]
    if (myTextView != null) {
      myTextView setText String.valueOf(mClickCounter)
    }
  }

  override protected def onClick() {
    val newValue = mClickCounter + 1
    // Give the client a chance to ignore this change if they deem it
    // invalid
    if (!callChangeListener(newValue)) {
      // They don't want the value to be set
      return
    }
        
    // Increment counter
    mClickCounter = newValue
        
    // Save to persistent storage (this method will make sure this
    // preference should be persistent, along with other useful checks)
    persistInt(mClickCounter)
        
    // Data has changed, notify so UI can be refreshed!
    notifyChanged()
  }

  override protected def onGetDefaultValue(a: TypedArray, index: Int): Object = {
    // This preference type's value type is Integer, so we read the default
    // value from the attributes as an Integer.
    a.getInteger(index, 0).asInstanceOf[Object]
  }

  override protected def onSetInitialValue(restoreValue: Boolean, defaultValue: Object) {
    if (restoreValue) {
      // Restore state
      mClickCounter = getPersistedInt(mClickCounter)
    } else {
      // Set state
      val value = defaultValue.asInstanceOf[Int]
      mClickCounter = value
      persistInt(value)
    }
  }

  override protected def onSaveInstanceState(): Parcelable = {
    /*
     * Suppose a client uses this preference type without persisting. We
     * must save the instance state so it is able to, for example, survive
     * orientation changes.
     */
    val superState = super.onSaveInstanceState()
    if (isPersistent()) {
      // No need to save instance state since it's persistent
      superState
    } else {
      // Save the instance state
      val myState = new SavedState2(superState)
      myState.clickCounter = mClickCounter
      myState
    }
  }

  override protected def onRestoreInstanceState(state: Parcelable) {
    if (!state.getClass.equals(classOf[SavedState])) {
      // Didn't save state for us in onSaveInstanceState
      super.onRestoreInstanceState(state)
      return
    }

    // Restore the instance state
    val myState = state.asInstanceOf[SavedState]
    super.onRestoreInstanceState(myState.getSuperState)
    mClickCounter = myState.clickCounter
    notifyChanged()
  }
    
}
