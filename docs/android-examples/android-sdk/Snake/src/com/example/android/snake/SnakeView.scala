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

package com.example.android.snake

import scala.collection.mutable.ListBuffer
import scala.util.Random

import android.content.Context
import android.os.{Bundle, Handler, Message}
import android.util.{AttributeSet, Log}
import android.view.{KeyEvent, View}
import android.widget.TextView

object SnakeView {
  private val TAG = "SnakeView"

  object Mode extends Enumeration {
    val PAUSE, READY, RUNNING, LOSE = Value
  }
  type Mode = Mode.Value

  object Direction extends Enumeration {
    val NORTH, SOUTH, EAST, WEST = Value
  }
  type Direction = Direction.Value

  /** Labels for the drawables that will be loaded into the TileView class
   */
  private val RED_STAR    = 1
  private val YELLOW_STAR = 2
  private val GREEN_STAR  = 3

  /** Everyone needs a little randomness in their life
   */
  private val RNG = new Random()
}

/** SnakeView: implementation of a simple game of Snake
 */
class SnakeView(context: Context, attrs: AttributeSet, defStyle: Int)
extends TileView(context, attrs, defStyle) {
  import SnakeView._, Mode._, Direction._

  /** Current mode of application: READY to run, RUNNING, or you have already
   *  lost. static final ints are used instead of an enum for performance
   *  reasons.
   */
  private var mMode = READY

  /** Current direction the snake is headed.
   */
  private var mDirection = NORTH
  private var mNextDirection = NORTH

  /** mScore: used to track the number of apples captured mMoveDelay: number of
   *  milliseconds between snake movements. This will decrease as apples are
   *  captured.
   */
  private var mScore = 0L
  private var mMoveDelay = 600L

  /** mLastMove: tracks the absolute time when the snake last moved, and is used
   *  to determine if a move should be made based on mMoveDelay.
   */
  private var mLastMove = 0L
    
  /** mStatusText: text shows to the user in some run states
   */
  private var mStatusText: TextView = null

  /** mSnakeTrail: a list of Coordinates that make up the snake's body
   *  mAppleList: the secret location of the juicy apples the snake craves.
   */
  private var mSnakeTrail = new ListBuffer[Coordinate]
  private var mAppleList = new ListBuffer[Coordinate]

  /** Create a simple handler that we can use to cause animation to happen.  We
   *  set ourselves as a target and we can use the sleep()
   *  function to cause an update/invalidate to occur at a later date.
   */
  private val mRedrawHandler = new RefreshHandler()

  private class RefreshHandler extends Handler {
    override def handleMessage(msg: Message) {
      SnakeView.this.update()
      SnakeView.this.invalidate()
    }
    def sleep(delayMillis: Long) {
      removeMessages(0)
      sendMessageDelayed(obtainMessage(0), delayMillis)
     }
  }

  /** Constructs a SnakeView based on inflation from XML
   * 
   * @param context
   * @param attrs
   */
  def this(context: Context, attrs: AttributeSet) =
    this(context, attrs, 0)

  initSnakeView()

  private def initSnakeView() {
    setFocusable(true)

    val r = this.getContext.getResources

    resetTiles(4)
    loadTile(RED_STAR, r getDrawable R.drawable.redstar)
    loadTile(YELLOW_STAR, r getDrawable R.drawable.yellowstar)
    loadTile(GREEN_STAR, r getDrawable R.drawable.greenstar)
  }

  private def initNewGame() {
    mSnakeTrail.clear()
    mAppleList.clear()

    // For now we're just going to load up a short default eastbound snake
    // that's just turned north
        
    mSnakeTrail += Coordinate(7, 7)
    mSnakeTrail += Coordinate(6, 7)
    mSnakeTrail += Coordinate(5, 7)
    mSnakeTrail += Coordinate(4, 7)
    mSnakeTrail += Coordinate(3, 7)
    mSnakeTrail += Coordinate(2, 7)
    mNextDirection = NORTH

    // Two apples to start with
    addRandomApple()
    addRandomApple()

    mMoveDelay = 600
    mScore = 0
  }

  /** Given a List of coordinates, we need to flatten them into an array of
   *  ints before we can stuff them into a map for flattening and storage.
   * 
   *  @param cvec : a ListBuffer of Coordinate objects
   *  @return : a simple array containing the x/y values of the coordinates
   *  as [x1,y1,x2,y2,x3,y3...]
   */
  private def coordListToArray(cvec: ListBuffer[Coordinate]): Array[Int] = {
    val len = cvec.length
    val rawArray = new Array[Int](len * 2)
    for (index <- 0 until len) {
      val c = cvec(index)
      val i2 = 2 * index
      rawArray(i2) = c.x
      rawArray(i2 + 1) = c.y
    }
    rawArray
  }

  /** Save game state so that the user does not lose anything
   *  if the game process is killed while we are in the 
   *  background.
   * 
   * @return a Bundle with this view's state
   */
  def saveState(): Bundle = {
    val map = new Bundle()

    map.putIntArray("mAppleList", coordListToArray(mAppleList))
    map.putInt("mDirection", mDirection.id)
    map.putInt("mNextDirection", mNextDirection.id)
    map.putLong("mMoveDelay", mMoveDelay)
    map.putLong("mScore", mScore)
    map.putIntArray("mSnakeTrail", coordListToArray(mSnakeTrail))

    map
  }

  /** Given a flattened array of ordinate pairs, we reconstitute them into a
   *  ListBuffer of Coordinate objects
   * 
   * @param rawArray : [x1,y1,x2,y2,...]
   * @return a ListBuffer of Coordinates
   */
  private def coordArrayToList(rawArray: Array[Int]): ListBuffer[Coordinate] = {
    val coordList = new ListBuffer[Coordinate]
    for (index <- (0 until rawArray.length) map (_ * 2)) {
      coordList += Coordinate(rawArray(index), rawArray(index + 1))
    }
    coordList
  }

  /** Restore game state if our process is being relaunched
   * 
   * @param icicle a Bundle containing the game state
   */
  def restoreState(icicle: Bundle) {
    setMode(PAUSE)

    mAppleList = coordArrayToList(icicle getIntArray"mAppleList")
    mDirection = Direction(icicle getInt "mDirection")
    mNextDirection = Direction(icicle getInt "mNextDirection")
    mMoveDelay = icicle getLong "mMoveDelay"
    mScore = icicle getLong "mScore"
    mSnakeTrail = coordArrayToList(icicle getIntArray "mSnakeTrail")
  }

  /** Handles key events in the game. Update the direction our snake is traveling
   *  based on the DPAD. Ignore events that would cause the snake to immediately
   *  turn back on itself.
   * 
   * (non-Javadoc)
   * 
   * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
   */
  override def onKeyDown(keyCode: Int, msg: KeyEvent): Boolean = {
    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
      if (mMode == READY | mMode == LOSE) {
        /*
         * At the beginning of the game, or the end of a previous one,
         * we should start a new game.
         */
        initNewGame()
        setMode(RUNNING)
        update()
        return true
      }

      if (mMode == PAUSE) {
        /*
         * If the game is merely paused, we should just continue where
         * we left off.
         */
        setMode(RUNNING)
        update()
        return true
      }

      if (mDirection != SOUTH) mNextDirection = NORTH
      return true
    }

    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
      if (mDirection != NORTH) mNextDirection = SOUTH
      return true
    }

    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
      if (mDirection != EAST) mNextDirection = WEST
      return true
    }

    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
      if (mDirection != WEST) mNextDirection = EAST
      return true
    }

    super.onKeyDown(keyCode, msg)
  }

  /** Sets the TextView that will be used to give information (such as "Game
   *  Over" to the user.
   * 
   * @param newView
   */
  def setTextView(newView: TextView) {
    mStatusText = newView
  }

  /** Updates the current mode of the application (RUNNING or PAUSED or the like)
   *  as well as sets the visibility of textview for notification
   * 
   * @param newMode
   */
  def setMode(newMode: Mode) {
    val oldMode = mMode
    mMode = newMode

    if (newMode == RUNNING & oldMode != RUNNING) {
      mStatusText setVisibility View.INVISIBLE
      update()
      return
    }

    val res = getContext().getResources()
    val str: CharSequence = newMode match {
      case PAUSE => res getText R.string.mode_pause
      case READY => res getText R.string.mode_ready
      case LOSE  => (res getString R.string.mode_lose_prefix) +
                    mScore + (res getString R.string.mode_lose_suffix)
      case _     => ""
    }
    mStatusText setText str
    mStatusText setVisibility View.VISIBLE
  }

  /** Selects a random location within the garden that is not currently covered
   * by the snake. Currently _could_ go into an infinite loop if the snake
   * currently fills the garden, but we'll leave discovery of this prize to a
   * truly excellent snake-player. 
   */
  private def addRandomApple() {
    var newCoord: Coordinate = null
    var found = false
    while (!found) {
      // Choose a new location for our apple
      val newX = 1 + RNG.nextInt(mXTileCount - 2)
      val newY = 1 + RNG.nextInt(mYTileCount - 2)
      newCoord = Coordinate(newX, newY)

      // Make sure it's not already under the snake
      var collision = false
      for (index <- 0 until mSnakeTrail.length) {
        if (mSnakeTrail(index) equals newCoord) {
          collision = true
        }
      }
      // if we're here and there's been no collision, then we have
      // a good location for an apple. Otherwise, we'll circle back
      // and try again
      found = !collision
    }
    if (newCoord == null) {
      Log.e(TAG, "Somehow ended up with a null newCoord!")
    }
    mAppleList += newCoord
  }

  /** Handles the basic update loop, checking to see if we are in the running
   * state, determining if a move should be made, updating the snake's location.
   */
  def update() {
    if (mMode == RUNNING) {
      val now = System.currentTimeMillis

      if (now - mLastMove > mMoveDelay) {
        clearTiles()
        updateWalls()
        updateSnake()
        updateApples()
        mLastMove = now
      }
      mRedrawHandler sleep mMoveDelay
    }
  }

  /** Draws some walls. 
   */
  private def updateWalls() {
    for (x <- 0 until mXTileCount) {
      setTile(GREEN_STAR, x, 0)
      setTile(GREEN_STAR, x, mYTileCount - 1)
    }
    for (y <- 1 until (mYTileCount - 1)) {
      setTile(GREEN_STAR, 0, y)
      setTile(GREEN_STAR, mXTileCount - 1, y)
    }
  }

  /** Draws some apples.
   */
  private def updateApples() {
    for (c <- mAppleList) {
      setTile(YELLOW_STAR, c.x, c.y)
    }
  }

  /** Figure out which way the snake is going, see if he's run into anything (the
   *  walls, himself, or an apple). If he's not going to die, we then add to the
   *  front and subtract from the rear in order to simulate motion. If we want to
   *  grow him, we don't subtract from the rear.
   */
  private def updateSnake() {
    var growSnake = false

    // grab the snake by the head
    val head = mSnakeTrail(0)

    mDirection = mNextDirection

    val newHead = mDirection match {
      case EAST  => new Coordinate(head.x + 1, head.y)
      case WEST  => new Coordinate(head.x - 1, head.y)
      case NORTH => new Coordinate(head.x, head.y - 1)
      case SOUTH => new Coordinate(head.x, head.y + 1)
    }

    // Collision detection
    // For now we have a 1-square wall around the entire arena
    if ((newHead.x < 1) || (newHead.y < 1)
     || (newHead.x > mXTileCount - 2)
     || (newHead.y > mYTileCount - 2)) {
      setMode(LOSE)
      return
    }

    // Look for collisions with itself
    for (snakeindex <- 0 until mSnakeTrail.length) {
      val c = mSnakeTrail(snakeindex)
      if (c equals newHead) {
        setMode(LOSE)
        return
      }
    }

    // Look for apples
    for (appleindex <- 0 until mAppleList.length) {
      val c = mAppleList(appleindex)
      if (c equals newHead) {
        mAppleList -= c
        addRandomApple()
                
         mScore += 1
         mMoveDelay = mMoveDelay * 9 / 10

         growSnake = true
       }
     }

    // push a new head onto the ListBuffer and pull off the tail
    newHead +=: mSnakeTrail
    // except if we want the snake to grow
    if (!growSnake) {
      mSnakeTrail remove (mSnakeTrail.length - 1)
    }

    var index = 0
    for (c <- mSnakeTrail) {
      setTile(if (index == 0) YELLOW_STAR else RED_STAR, c.x, c.y)
      index += 1
    }

  }

  /** Simple class containing two integer values and a comparison function.
   *  There's probably something I should use instead, but this was quick and
   *  easy to build. 
   */
  private case class Coordinate(x: Int, y: Int) {
    override def toString() = "Coordinate: [" + x + "," + y + "]"
  }
  
}
