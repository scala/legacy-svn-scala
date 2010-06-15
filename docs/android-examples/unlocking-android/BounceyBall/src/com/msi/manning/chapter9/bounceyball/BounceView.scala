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

package com.msi.manning.chapter9.bounceyball

import android.content.Context
import android.graphics.{Canvas, Point}
import android.graphics.drawable.Drawable
import android.view.View

class BounceView(context: Context) extends View(context) {

  /* Our Ball together with the location it will be painted*/
  protected var mySprite: Drawable = _
  protected val mySpritePos = new Point(0,0)

  /* Working with a Enum is 10000%
   * safer than working with int's
   * to 'remember' the direction. */
  protected object HorizontalDirection extends Enumeration {
    val LEFT, RIGHT = Value
  }
  protected object VerticalDirection extends Enumeration {
    val UP, DOWN = Value
  }
  protected var myXDirection = HorizontalDirection.RIGHT
  protected var myYDirection = VerticalDirection.UP

  // Set the background
  setBackgroundDrawable(getResources.getDrawable(R.drawable.android))
  // Load our "Ball"
  mySprite = getResources.getDrawable(R.drawable.world)

  override protected def onDraw(canvas: Canvas) {
    mySprite.setBounds(mySpritePos.x, mySpritePos.y,
                       mySpritePos.x + 50, mySpritePos.y + 50)

    /* Check if the Ball started to leave the screen on left or right side */
    if (mySpritePos.x >= getWidth - mySprite.getBounds.width) {
      myXDirection = HorizontalDirection.LEFT
    } else if (mySpritePos.x <= 0) {
      myXDirection = HorizontalDirection.RIGHT
    }

    /* Check if the Ball started to leave the screen on bottom or upper side */
    if (mySpritePos.y >= getHeight - mySprite.getBounds.height) {
      myYDirection = VerticalDirection.UP
    } else if (mySpritePos.y <= 0) {
      myYDirection = VerticalDirection.DOWN
    }

    /* Move the ball left or right */
    if (myXDirection == HorizontalDirection.RIGHT) {
      mySpritePos.x += 10
    } else {
      mySpritePos.x -= 10
    }

    /* Move the ball up or down */
    if (myYDirection == VerticalDirection.DOWN) {
      mySpritePos.y += 10
    } else {
      mySpritePos.y -= 10
    }

    /* Set the location, where the sprite will draw itself to the canvas */

    /* Make the sprite draw itself to the canvas */
    mySprite draw canvas
  }
}
