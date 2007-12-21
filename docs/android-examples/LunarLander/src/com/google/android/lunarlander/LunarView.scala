/* 
 * Copyright (C) 2007 Google Inc.
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

package com.google.android.lunarlander

import _root_.android.content.{Context, Resources}
import _root_.android.graphics.{Canvas, Paint, RectF}
import _root_.android.graphics.drawable.Drawable
import _root_.android.os.Bundle
import _root_.android.util.AttributeSet
import _root_.android.view.{KeyEvent, View}
import _root_.android.widget.TextView

import java.util.Map

object LunarView {
  final val READY = 0
  final val RUNNING = 1
  final val PAUSE = 2
  final val LOSE = 3
  final val WIN = 4
   
  final val EASY = 0
  final val MEDIUM = 1
  final val HARD = 2
   
  // Parameters for how the physics works.
  final val FIRE_ACCEL_SEC = 80
  final val DOWN_ACCEL_SEC = 35
  final val FUEL_SEC = 10

  final val SLEW_SEC = 120 // degrees/second rotate
   
  final val FUEL_INIT = 60d
  final val FUEL_MAX = 100
   
  final val SPEED_INIT = 30
  final val SPEED_MAX = 120
  final val SPEED_HYPERSPACE = 180
   
  // Parameters for landing successfully (MEDIUM difficulty).
  final val TARGET_SPEED = 28
  final val TARGET_WIDTH = 1.6 // how much wider than lander
  final val TARGET_ANGLE = 18
   
  /**
   * Pixel height of the fuel/speed bar.
   */
  final val BAR_HEIGHT = 10
   
  /**
   * Pixel width of the fuel/speed bar.
   */
  final val BAR = 100
   
  /**
   * Height of the landing pad off the bottom.
   */
  final val PAD_HEIGHT = 8
   
  /**
   * Extra pixels below the landing gear in the images
   */
  final val BOTTOM_PADDING = 17
}

/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 * 
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class LunarView(context: Context, attrs: AttributeSet, inflateParams: Map) 
      extends View(context, attrs, inflateParams) {
  import LunarView._  // companion object
   
  /**
   * The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
   */
  private var mMode = READY

  /**
   * Current difficulty -- amount of fuel, allowed angle, etc.
   * Default is MEDIUM.
   */
  private var mDifficulty = MEDIUM

  /**
   * Velocity dx.
   */
  private var mDX = 0.0
    
  /**
   * Velocity dy.
   */
  private var mDY = 0.0

  /**
   * Lander heading in degrees, with 0 up, 90 right.
   * Kept in the range 0..360.
   */
  private var mHeading = 0.0
   
  /**
   * Currently rotating, -1 left, 0 none, 1 right.
   */
  private var mRotating = 0

  /**
   * X of the landing pad.
   */
  private var mGoalX = 0
    
  /**
   * Allowed speed.
   */
  private var mGoalSpeed = 0
    
  /**
   * Allowed angle.
   */
  private var mGoalAngle = 0
    
  /**
   * Width of the landing pad.
   */
  private var mGoalWidth = 0
    
  /**
   * Number of wins in a row.
   */
  private var mWinsInARow = 0
 
  /**
   * Fuel remaining
   */
  private var mFuel = FUEL_INIT

  /**
   * Is the engine burning?
   */
  private var mEngineFiring = true
    
    
  /**
   * Used to figure out elapsed time between frames
   */
  private var mLastTime: Long = _

  /**
   * Paint to draw the lines on screen.
   */
  private val mLinePaint = new Paint
  mLinePaint setAntiAlias true
  mLinePaint.setARGB(255, 0, 255, 0)
    
  /**
   * "Bad" speed-too-high variant of the line color.
   */
  private val mLinePaintBad = new Paint
  mLinePaintBad setAntiAlias true
  mLinePaintBad.setARGB(255, 120, 180, 0)

  /**
   * What to draw for the Lander in its normal state
   */
  private val mLanderImage =
    context.getResources getDrawable R.drawable.lander_plain

  /**
   * What to draw for the Lander when the engine is firing
   */
  private val mFiringImage =
    context.getResources getDrawable R.drawable.lander_firing

  /**
   * What to draw for the Lander when it has crashed
   */
  private val mCrashedImage =
    context.getResources getDrawable R.drawable.lander_crashed

  /**
   * Pixel width of lander image.
   */
  var mLanderWidth = mLanderImage.getIntrinsicWidth
      
  /**
   * Pixel height of lander image.
   */
  var mLanderHeight = mLanderImage.getIntrinsicHeight
      
  /**
   * X of lander center.
   */
  private var mX: Double = mLanderWidth

  /**
   * Y of lander center.
   */
  private var mY: Double = mLanderHeight * 2

  /**
   * Pointer to the text view to display "Paused.." etc.
   */
  private var mStatusText: TextView = _
    
  /**
   * Scratch rect object.
   */
  private val mScratchRect= new RectF(0, 0, 0, 0)


  setBackground(R.drawable.earthrise)
  // Make sure we get keys
  setFocusable(true)

  /**
   * Save game state so that the user does not lose anything
   * if the game process is killed while we are in the 
   * background.
   * 
   * @return Map with this view's state
   */
  def saveState(): Bundle = {
    val map = new Bundle

    map.putInteger("mDifficulty", mDifficulty)
    map.putDouble("mX", mX)
    map.putDouble("mY", mY)
    map.putDouble("mDX", mDX)
    map.putDouble("mDY", mDY)
    map.putDouble("mHeading", mHeading)
    map.putInteger("mLanderWidth", mLanderWidth)
    map.putInteger("mLanderHeight", mLanderHeight)
    map.putInteger("mGoalX", mGoalX)
    map.putInteger("mGoalSpeed", mGoalSpeed)
    map.putInteger("mGoalAngle", mGoalAngle)
    map.putInteger("mGoalWidth", mGoalWidth)
    map.putInteger("mWinsInARow", mWinsInARow)
    map.putDouble("mFuel", mFuel)

    map
  }

  /**
   * Restore game state if our process is being relaunched
   * 
   * @param icicle Map containing the game state
   */
  def restoreState(icicle: Bundle) {
    mode = PAUSE
    mRotating = 0
    mEngineFiring = false
        
    mDifficulty = icicle.getInteger("mDifficulty").intValue
    mX = icicle.getDouble("mX").doubleValue
    mY = icicle.getDouble("mY").doubleValue
    mDX = icicle.getDouble("mDX").doubleValue
    mDY = icicle.getDouble("mDY").doubleValue
    mHeading = icicle.getDouble("mHeading").doubleValue
 
    mLanderWidth = icicle.getInteger("mLanderWidth").intValue
    mLanderHeight = icicle.getInteger("mLanderHeight").intValue
    mGoalX = icicle.getInteger("mGoalX").intValue
    mGoalSpeed = icicle.getInteger("mGoalSpeed").intValue
    mGoalAngle = icicle.getInteger("mGoalAngle").intValue
    mGoalWidth = icicle.getInteger("mGoalWidth").intValue
    mWinsInARow = icicle.getInteger("mWinsInARow").intValue
    mFuel = icicle.getDouble("mFuel").doubleValue
  }

  /**
   * Installs a pointer to the text view used
   * for messages.
   */
  def setTextView(textView: TextView) { mStatusText = textView }

  /**
   * Standard window-focus override.
   * Notice focus lost so we can pause on focus lost.
   * e.g. user switches to take a call.
   */
  override def windowFocusChanged(hasWindowFocus: Boolean) {
    if (!hasWindowFocus) doPause()
  }

  /**
   * Standard override of View.draw.
   * Draws the ship and fuel/speed bars.
   */
  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (mMode == RUNNING) updatePhysics()

    val screenWidth = getWidth
    val screenHeight = getHeight

    val yTop = screenHeight - (mY.toInt + mLanderHeight/2)
    val xLeft = mX.toInt - mLanderWidth/2

    // Draw fuel rect
    val fuelWidth = (BAR * mFuel / FUEL_MAX).toInt
    mScratchRect.set(4, 4, 4 + fuelWidth, 4 + BAR_HEIGHT)
    canvas.drawRect(mScratchRect, mLinePaint)
        
    val speed = Math.sqrt(mDX*mDX + mDY*mDY)
    val speedWidth = (BAR * speed / SPEED_MAX).toInt
        
    if (speed <= mGoalSpeed) {
      mScratchRect.set(4 + BAR + 4, 4, 4 + BAR + 4 + speedWidth, 4 + BAR_HEIGHT)
      canvas.drawRect(mScratchRect, mLinePaint)
    } else {
      // Draw the bad color in back, with the good color in front of it
      mScratchRect.set(4 + BAR + 4, 4, 4 + BAR + 4 + speedWidth, 4 + BAR_HEIGHT)
      canvas.drawRect(mScratchRect, mLinePaintBad)
      val goalWidth = (BAR * mGoalSpeed / SPEED_MAX)
      mScratchRect.set(4 + BAR + 4, 4, 4 + BAR + 4 + goalWidth, 4 + BAR_HEIGHT)
      canvas.drawRect(mScratchRect, mLinePaint)
    }

    // Draw the landing pad
    canvas.drawLine(mGoalX, 1 + screenHeight - PAD_HEIGHT,
                    mGoalX + mGoalWidth, 1 + screenHeight - PAD_HEIGHT,  mLinePaint)


    // Draw the ship with its current rotation
    canvas.save()
    canvas.rotate(mHeading.toFloat, mX.toFloat, screenHeight - mY.toFloat)

    if (mMode == LOSE) {
      mCrashedImage.setBounds(xLeft, yTop, xLeft+mLanderWidth, yTop+mLanderHeight)
      mCrashedImage.draw(canvas)
    } else if (mEngineFiring) {
      mFiringImage.setBounds(xLeft, yTop, xLeft+mLanderWidth, yTop+mLanderHeight)
      mFiringImage.draw(canvas)
    } else {
      mLanderImage.setBounds(xLeft, yTop, xLeft+mLanderWidth, yTop+mLanderHeight)          
      mLanderImage.draw(canvas)
    }

    /*
     * Our animation strategy is that each draw() does an invalidate(),
     * so we get a series of draws. This is a known animation strategy
     * within Android, and the system throttles the draws down to match
     * the refresh rate.
     */

    if (mMode == RUNNING) {
      // Invalidate a space around the current lander + the bars at the top.
      // Note: invalidating a relatively small part of the screen to draw
      // is a good optimization. In this case, the bars and the ship
      // may be far apart, limiting the value of the optimization.
      invalidate(xLeft-20, yTop-20, xLeft+mLanderWidth+20, yTop+mLanderHeight+20)
      invalidate(0, 0, screenWidth, 4 + BAR_HEIGHT)
    }
        
    canvas.restore()
  }

  /**
   * Figures the lander state (x, y, fuel, ...) based on the passage of
   * realtime. Does not invalidate(). Called at the start
   * of draw(). Detects the end-of-game and sets the UI to the next state.
   */
  def updatePhysics() {
    val now = System.currentTimeMillis

    // Do nothing if mLastTime is in the future.
    // This allows the game-start to delay the start of the physics
    // by 100ms or whatever.
    if (mLastTime > now) return
        
    val elapsed = (now - mLastTime) / 1000.0
        
    // mRotating -- update heading
    if (mRotating != 0) {
      mHeading += mRotating * (SLEW_SEC * elapsed)
          
      // Bring things back into the range 0..360
      if (mHeading < 0) mHeading += 360
      else if (mHeading >= 360) mHeading -= 360
    }
        
    // Base accelerations -- 0 for x, gravity for y
    var ddx = 0.0
    var ddy = -DOWN_ACCEL_SEC * elapsed

    if (mEngineFiring) {
      // taking 0 as up, 90 as to the right
      // cos(deg) is ddy component, sin(deg) is ddx component
      var elapsedFiring = elapsed
      var fuelUsed = elapsedFiring * FUEL_SEC

      // tricky case where we run out of fuel partway through the elapsed
      if (fuelUsed > mFuel) {
        elapsedFiring = mFuel / fuelUsed * elapsed
        fuelUsed = mFuel

        // Oddball case where we adjust the "control" from here
        mEngineFiring = false
      }

      mFuel -= fuelUsed
            
      // have this much acceleration from the engine
      val accel = FIRE_ACCEL_SEC * elapsedFiring
            
      val radians = 2 * Math.Pi * mHeading / 360
      ddx = Math.sin(radians) * accel
      ddy += Math.cos(radians) * accel
    }

    val dxOld = mDX
    val dyOld = mDY
        
    // figure speeds for the end of the period
    mDX += ddx
    mDY += ddy

    // figure position based on average speed during the period
    mX += elapsed * (mDX + dxOld)/2
    mY += elapsed * (mDY + dyOld)/2

    mLastTime = now

    checkLanding()
  }

  def checkLanding() {
    val yLowerBound = PAD_HEIGHT + mLanderHeight/2 - BOTTOM_PADDING
    if (mY <= yLowerBound) {
      mY = yLowerBound

      val res = getContext.getResources
      val speed = Math.sqrt(mDX*mDX + mDY*mDY)
      val onGoal = (mGoalX <= mX - mLanderWidth/2  &&
                    mX + mLanderWidth/2 <= mGoalX + mGoalWidth)
        
      // "Hyperspace" win -- upside down, going fast,
      // puts you back at the top.
      if (onGoal && Math.abs(mHeading - 180) < mGoalAngle &&
        speed > SPEED_HYPERSPACE) {
        mWinsInARow += 1
        doStart()
      } else {
        val (result, message) = 
          if (!onGoal) 
            (LOSE, res.getText(R.string.message_off_pad))
          else if (!(mHeading <= mGoalAngle || mHeading >= 360 - mGoalAngle))
            (LOSE, res.getText(R.string.message_bad_angle))
          else if (speed  > mGoalSpeed) 
            (LOSE, res.getText(R.string.message_too_fast))
          else {
            mWinsInARow += 1
            (WIN, "")
          }
        setMode(result, message)
      }
    }
  }

  /**
   * Sets if the engine is currently firing.
   */
  def isFiring_=(firing: Boolean) { mEngineFiring = firing }
  def isFiring = mEngineFiring

  /**
   * Sets the game mode, RUNNING, PAUSED, etc.
   * @param mode RUNNING, PAUSED, ...
   */
  def mode_=(mode: Int) { setMode(mode, null) }
  def mode = mMode
  
  /**
   * Sets the game mode, RUNNING, PAUSED, etc.
   * @param mode RUNNING, PAUSED, ...
   * @param message string to add to screen or null
   */
  def setMode(mode: Int, message: CharSequence) {
    mMode = mode
    invalidate()
	
    if (mMode == RUNNING) {
      mStatusText.setVisibility(View.INVISIBLE)
    } else {
      mRotating = 0
      mEngineFiring = false
      val res = getContext.getResources
      var str = mMode match {
        case READY => res.getText(R.string.mode_ready)
        case PAUSE => res.getText(R.string.mode_pause)
        case LOSE => mWinsInARow = 0; res.getText(R.string.mode_lose)
        case WIN => res.getString(R.string.mode_win_prefix)
          + mWinsInARow + " " + res.getString(R.string.mode_win_suffix)
        case _ => ""
      }
      mStatusText setText (if (message != null) message + "\n" + str else str)
      mStatusText setVisibility View.VISIBLE
    }
  }

  /**
   * Starts the game, setter parameters for the current
   * difficulty.
   */
  def doStart() {
    // First set the game for Medium difficulty
    mFuel = FUEL_INIT
    mEngineFiring = false
    mGoalWidth = (mLanderWidth * TARGET_WIDTH).toInt
    mGoalSpeed = TARGET_SPEED
    mGoalAngle = TARGET_ANGLE
    var speedInit = SPEED_INIT
        
    // Adjust difficulty params for EASY/HARD
    if (mDifficulty == EASY) {
      mFuel = mFuel * 3 / 2
      mGoalWidth = mGoalWidth * 4 / 3
      mGoalSpeed = mGoalSpeed * 3 / 2
      mGoalAngle = mGoalAngle * 4 / 3
      speedInit = speedInit * 3 / 4
    } else if (mDifficulty == HARD) {
      mFuel = mFuel * 7 / 8
      mGoalWidth = mGoalWidth * 3 / 4
      mGoalSpeed = mGoalSpeed * 7 / 8
      speedInit = speedInit * 4 / 3
    }
        
    mX = getWidth/2
    mY = getHeight - mLanderHeight/2
        
    // start with a little random motion
    mDY = Math.random * -speedInit
    mDX = Math.random * 2*speedInit - speedInit
        
    mHeading = 0
        
    // Figure initial spot for landing, not too near center
    do {
      mGoalX = (Math.random * (getWidth - mGoalWidth)).toInt
    } while (Math.abs(mGoalX - (mX - mLanderWidth/2)) <= getWidth/6)
        
    mLastTime = System.currentTimeMillis + 100
    mode = RUNNING
  }

  /**
   * Resumes from a pause.
   */
  def doResume() {
    // Move the real time clock up to now
    mLastTime = System.currentTimeMillis + 100
    mode = RUNNING
  }

  /**
   * Pauses from the running state.
   */
  def doPause() { if (mMode == RUNNING) mode = PAUSE }

    
  /**
   * Standard override to get key events.
   */
  override def onKeyDown(keyCode: Int, msg: KeyEvent): Boolean = {
    var handled = false

    val okStart = keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                  keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                  keyCode == KeyEvent.KEYCODE_S

    val center = keyCode == KeyEvent.KEYCODE_DPAD_UP

    // ready-to-start -> start
    if (okStart && (mMode == READY || mMode == LOSE || mMode == WIN)) {
      doStart()
      handled = true
    }
    // paused -> running
    else if (mMode == PAUSE && okStart) {
      doResume()
      handled = true
    } else if (mMode == RUNNING) {
      // center/space -> fire
      if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER  ||
          keyCode == KeyEvent.KEYCODE_SPACE) {
        isFiring = true
        handled = true
        // left/q -> left
      } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                 keyCode == KeyEvent.KEYCODE_Q) {
        mRotating = -1
        handled = true
        // right/w -> right
      } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                 keyCode == KeyEvent.KEYCODE_W) {
        mRotating = 1
        handled = true
        // up -> pause
      } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
        doPause()
        handled = true
      }
    }
    handled
  }

  /**
   * Standard override for key-up. We actually care about these,
   * so we can turn off the engine or stop rotating.
   */
  override def onKeyUp(keyCode: Int, msg: KeyEvent): Boolean = 
    if (mMode == RUNNING) {
      if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
          keyCode == KeyEvent.KEYCODE_SPACE) {
        isFiring = false
        true
      } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                 keyCode == KeyEvent.KEYCODE_Q || 
                 keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                 keyCode == KeyEvent.KEYCODE_W) {
        mRotating = 0
        true
      } else false
    } else false

  def difficulty_=(d: Int) { mDifficulty = d }
  def difficulty = mDifficulty
}
