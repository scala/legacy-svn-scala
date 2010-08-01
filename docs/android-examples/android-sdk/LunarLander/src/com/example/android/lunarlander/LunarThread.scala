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

package com.example.android.lunarlander

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory, Canvas, Paint, RectF}
import android.graphics.drawable.Drawable
import android.os.{Bundle, Handler, Message}
import android.view.{KeyEvent, SurfaceHolder, View}


private[lunarlander] object LunarThread {

  /** Difficulty setting constants */
  object Difficulty extends Enumeration {
    val EASY, HARD, MEDIUM = Value
  }
  type Difficulty = Difficulty.Value

  /** Physics constants */
  val PHYS_DOWN_ACCEL_SEC = 35
  val PHYS_FIRE_ACCEL_SEC = 80
  val PHYS_FUEL_INIT = 60
  val PHYS_FUEL_MAX = 100
  val PHYS_FUEL_SEC = 10
  val PHYS_SLEW_SEC = 120 // degrees/second rotate
  val PHYS_SPEED_HYPERSPACE = 180
  val PHYS_SPEED_INIT = 30
  val PHYS_SPEED_MAX = 120

  /** State-tracking constants */
  object State extends Enumeration {
    val LOSE, PAUSE, READY, RUNNING, WIN = Value
  }
  type State = State.Value

  /** Goal condition constants*/
  val TARGET_ANGLE = 18 // > this angle means crash
  val TARGET_BOTTOM_PADDING = 17 // px below gear
  val TARGET_PAD_HEIGHT = 8 // how high above ground
  val TARGET_SPEED = 28 // > this speed means crash
  val TARGET_WIDTH = 1.6 // width of target

  /** UI constants (i.e. the speed & fuel bars) */
  val UI_BAR = 100 // width of the bar(s)
  val UI_BAR_HEIGHT = 10 // height of the bar(s)

  private val KEY_DIFFICULTY = "mDifficulty"
  private val KEY_DX = "mDX"

  private val KEY_DY = "mDY"
  private val KEY_FUEL = "mFuel"
  private val KEY_GOAL_ANGLE = "mGoalAngle"
  private val KEY_GOAL_SPEED = "mGoalSpeed"
  private val KEY_GOAL_WIDTH = "mGoalWidth"

  private val KEY_GOAL_X = "mGoalX"
  private val KEY_HEADING = "mHeading"
  private val KEY_LANDER_HEIGHT = "mLanderHeight"
  private val KEY_LANDER_WIDTH = "mLanderWidth"
  private val KEY_WINS = "mWinsInARow"

  private val KEY_X = "mX"
  private val KEY_Y = "mY"
}

private[lunarlander] class LunarThread(mSurfaceHolder: SurfaceHolder,
                                     mContext: Context,
                                     mHandler: Handler) extends Thread {
  import LunarThread._  // companion object

  /** Member (state) fields
   */
  /** The drawable to use as the background of the animation canvas */
  private var mBackgroundImage: Bitmap = null

  /** Current height/width of the surface/canvas.
   * 
   * @see #setSurfaceSize
   */
  private var mCanvasHeight = 1
  private var mCanvasWidth = 1

  /** What to draw for the Lander when it has crashed */
  private var mCrashedImage: Drawable = null

  /** Current difficulty -- amount of fuel, allowed angle, etc.
   *  Default is MEDIUM.
   */
  private var mDifficulty = Difficulty.MEDIUM

  /** Velocity dx/dy. */
  private var mDX: Double = 0.0
  private var mDY: Double = 0.0

  /** Is the engine burning? */
  private var mEngineFiring = true

  /** What to draw for the Lander when the engine is firing */
  private var mFiringImage: Drawable = null

  /** Fuel remaining */
  private var mFuel: Double = PHYS_FUEL_INIT

  /** Allowed angle. */
  private var mGoalAngle: Int = 0

  /** Allowed speed. */
  private var mGoalSpeed = 0

  /** Width of the landing pad. */
  private var mGoalWidth = 0

  /** X of the landing pad. */
  private var mGoalX = 0

  /** Lander heading in degrees, with 0 up, 90 right. Kept in the range
   * 0..360.
   */
  private var mHeading: Double = 0.0

  /** Pixel height of lander image. */
  private var mLanderHeight = 0

  /** What to draw for the Lander in its normal state */
  private var mLanderImage: Drawable = null

  /** Pixel width of lander image. */
  private var mLanderWidth = 0

  /** Used to figure out elapsed time between frames */
  private var mLastTime = 0L

  /** Paint to draw the lines on screen. */
  private var mLinePaint: Paint = null

  /** "Bad" speed-too-high variant of the line color. */
  private var mLinePaintBad: Paint = null

  /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
  private var mMode = State.READY

  /** Currently rotating, -1 left, 0 none, 1 right. */
  private var mRotating = 0

  /** Indicate whether the surface has been created & is ready to draw */
  private var mRun = false

  /** Scratch rect object. */
  private var mScratchRect = new RectF(0, 0, 0, 0)

  /** Number of wins in a row. */
  private var mWinsInARow = 0

  /** X/Y of lander center. */
  private var mX = 0.0
  private var mY = 0.0

  initLunarThread()

  private def initLunarThread() {
    val res = mContext.getResources()
    // cache handles to our key sprites & other drawables
    mLanderImage = res getDrawable R.drawable.lander_plain
    mFiringImage = res getDrawable R.drawable.lander_firing
    mCrashedImage = res getDrawable R.drawable.lander_crashed

    // load background image as a Bitmap instead of a Drawable b/c
    // we don't need to transform it and it's faster to draw this way
    mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.earthrise)

    // Use the regular lander image as the model size for all sprites
    mLanderWidth = mLanderImage.getIntrinsicWidth()
    mLanderHeight = mLanderImage.getIntrinsicHeight()

    // Initialize paints for speedometer
    mLinePaint = new Paint()
    mLinePaint setAntiAlias true
    mLinePaint.setARGB(255, 0, 255, 0)

    mLinePaintBad = new Paint()
    mLinePaintBad setAntiAlias true
    mLinePaintBad.setARGB(255, 120, 180, 0)

    // initial show-up of lander (not yet playing)
    mX = mLanderWidth
    mY = mLanderHeight * 2
    mFuel = PHYS_FUEL_INIT
    mDX = 0
    mDY = 0
    mHeading = 0
  }

  /** Starts the game, setting parameters for the current difficulty.
   */
  def doStart() {
    mSurfaceHolder synchronized {
      // First set the game for Medium difficulty
      mFuel = PHYS_FUEL_INIT
      mEngineFiring = false
      mGoalWidth = (mLanderWidth * TARGET_WIDTH).toInt
      mGoalSpeed = TARGET_SPEED
      mGoalAngle = TARGET_ANGLE
      var speedInit: Double = PHYS_SPEED_INIT

      // Adjust difficulty params for EASY/HARD
      if (mDifficulty == Difficulty.EASY) {
        mFuel = mFuel * 3 / 2
        mGoalWidth = mGoalWidth * 4 / 3
        mGoalSpeed = mGoalSpeed * 3 / 2
        mGoalAngle = mGoalAngle * 4 / 3
        speedInit = speedInit * 3 / 4
      } else if (mDifficulty == Difficulty.HARD) {
        mFuel = mFuel * 7 / 8
        mGoalWidth = mGoalWidth * 3 / 4
        mGoalSpeed = mGoalSpeed * 7 / 8
        speedInit = speedInit * 4 / 3
      }

      // pick a convenient initial location for the lander sprite
      mX = mCanvasWidth / 2
      mY = mCanvasHeight - mLanderHeight / 2

      // start with a little random motion
      mDY = math.random * -speedInit
      mDX = math.random * 2 * speedInit - speedInit
      mHeading = 0

      // Figure initial spot for landing, not too near center
      do {
        mGoalX = (math.random * (mCanvasWidth - mGoalWidth)).toInt
      }
      while (math.abs(mGoalX - (mX - mLanderWidth / 2)) <= mCanvasHeight / 6)

      mLastTime = System.currentTimeMillis + 100
      setState(State.RUNNING)
    }
  }

  /** Pauses the physics update & animation.
   */
  def pause() {
    mSurfaceHolder synchronized {
      if (mMode == State.RUNNING) setState(State.PAUSE)
    }
  }

  /** Restores game state from the indicated Bundle. Typically called when
   *  the Activity is being restored after having been previously
   *  destroyed.
   * 
   *  @param savedState Bundle containing the game state
   */
  def restoreState(savedState: Bundle) {
    mSurfaceHolder synchronized {
      setState(State.PAUSE)
      mRotating = 0
      mEngineFiring = false

      mDifficulty = Difficulty(savedState getInt KEY_DIFFICULTY)
      mX = savedState getDouble KEY_X
      mY = savedState getDouble KEY_Y
      mDX = savedState getDouble KEY_DX
      mDY = savedState getDouble KEY_DY
      mHeading = savedState getDouble KEY_HEADING

      mLanderWidth = savedState getInt KEY_LANDER_WIDTH
      mLanderHeight = savedState getInt KEY_LANDER_HEIGHT
      mGoalX = savedState getInt KEY_GOAL_X
      mGoalSpeed = savedState getInt KEY_GOAL_SPEED
      mGoalAngle = savedState getInt KEY_GOAL_ANGLE
      mGoalWidth = savedState getInt KEY_GOAL_WIDTH
      mWinsInARow = savedState getInt KEY_WINS
      mFuel = savedState getDouble KEY_FUEL
    }
  }

  override def run() {
    while (mRun) {
      var c: Canvas = null
      try {
        c = mSurfaceHolder lockCanvas null
        mSurfaceHolder synchronized {
          if (mMode == State.RUNNING) updatePhysics()
          doDraw(c)
        }
      } finally {
        // do this in a finally so that if an exception is thrown
        // during the above, we don't leave the Surface in an
        // inconsistent state
        if (c != null) {
          mSurfaceHolder unlockCanvasAndPost c
        }
      }
    }
  }

  /** Dump game state to the provided Bundle. Typically called when the
   *  Activity is being suspended.
   * 
   *  @return Bundle with this view's state
   */
  def saveState(map: Bundle): Bundle = {
    mSurfaceHolder synchronized {
      if (map != null) {
        map.putInt(KEY_DIFFICULTY, mDifficulty.id)
        map.putDouble(KEY_X, mX)
        map.putDouble(KEY_Y, mY)
        map.putDouble(KEY_DX, mDX)
        map.putDouble(KEY_DY, mDY)
        map.putDouble(KEY_HEADING, mHeading)
        map.putInt(KEY_LANDER_WIDTH, mLanderWidth)
        map.putInt(KEY_LANDER_HEIGHT, mLanderHeight)
        map.putInt(KEY_GOAL_X, mGoalX)
        map.putInt(KEY_GOAL_SPEED, mGoalSpeed)
        map.putInt(KEY_GOAL_ANGLE, mGoalAngle)
        map.putInt(KEY_GOAL_WIDTH, mGoalWidth)
        map.putInt(KEY_WINS, mWinsInARow)
        map.putDouble(KEY_FUEL, mFuel)
      }
    }
    map
  }

  /** Sets the current difficulty.
   * 
   * @param difficulty
   */
  def setDifficulty(difficulty: Difficulty) {
    mSurfaceHolder synchronized  {
       mDifficulty = difficulty
    }
  }

  /** Sets if the engine is currently firing.
   */
  def setFiring(firing: Boolean) {
    mSurfaceHolder synchronized {
      mEngineFiring = firing
    }
  }

  /** Used to signal the thread whether it should be running or not.
   *  Passing true allows the thread to run; passing false will shut it
   *  down if it's already running. Calling start() after this was most
   *  recently called with false will result in an immediate shutdown.
   * 
   *  @param b true to run, false to shut down
   */
  def setRunning(b: Boolean) {
    mRun = b
  }

  /** Sets the game mode. That is, whether we are running, paused, in the
   *  failure state, in the victory state, etc.
   * 
   *  @see #setState(State, CharSequence)
   *  @param mode one of the State.* constants
   */
  def setState(mode: State) {
    mSurfaceHolder synchronized {
      setState(mode, null)
    }
  }

  /** Sets the game mode. That is, whether we are running, paused, in the
   *  failure state, in the victory state, etc.
   * 
   *  @param mode one of the State.* constants
   *  @param message string to add to screen or null
   */
  def setState(mode: State, message: CharSequence) {
    /** This method optionally can cause a text message to be displayed
     *  to the user when the mode changes. Since the View that actually
     *  renders that text is part of the main View hierarchy and not
     *  owned by this thread, we can't touch the state of that View.
     *  Instead we use a Message + Handler to relay commands to the main
     *  thread, which updates the user-text View.
     */
    mSurfaceHolder synchronized {
      mMode = mode

      if (mMode == State.RUNNING) {
        val msg = mHandler.obtainMessage()
        val b = new Bundle()
        b.putString("text", "")
        b.putInt("viz", View.INVISIBLE)
        msg setData b
        mHandler sendMessage msg
      } else {
        mRotating = 0
        mEngineFiring = false
        val res = mContext.getResources()
        val str: CharSequence = mMode match {
          case State.READY => res getText R.string.mode_ready
          case State.PAUSE => res getText R.string.mode_pause
          case State.LOSE  => res getText R.string.mode_lose
          case State.WIN   => (res getString R.string.mode_win_prefix) +
                              mWinsInARow + " " +
                              (res getString R.string.mode_win_suffix)
          case _ => ""
        }
        val text =
          (if (message != null) message + "\n" + str
          else str).toString

        if (mMode == State.LOSE) mWinsInARow = 0

        val msg = mHandler.obtainMessage()
        val b = new Bundle()
        b.putString("text", text)
        b.putInt("viz", View.VISIBLE)
        msg setData b
        mHandler sendMessage msg
      }
    }
  }

  /** Callback invoked when the surface dimensions change. */
  def setSurfaceSize(width: Int, height: Int) {
    // synchronized to make sure these all change atomically
    mSurfaceHolder synchronized {
      mCanvasWidth = width
      mCanvasHeight = height

      // don't forget to resize the background image
      mBackgroundImage =
        Bitmap.createScaledBitmap(mBackgroundImage, width, height, true)
    }
  }

  /** Resumes from a pause. */
  def unpause() {
    // Move the real time clock up to now
    mSurfaceHolder synchronized {
      mLastTime = System.currentTimeMillis + 100
    }
    setState(State.RUNNING)
  }

  /** Handles a key-down event.
   * 
   *  @param keyCode the key that was pressed
   *  @param msg the original event object
   *  @return true
   */
  def doKeyDown(keyCode: Int, msg: KeyEvent): Boolean = {
    mSurfaceHolder synchronized {
      lazy val okStart =
        keyCode == KeyEvent.KEYCODE_DPAD_UP ||
        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
        keyCode == KeyEvent.KEYCODE_S

      if ((mMode == State.READY || mMode == State.LOSE || mMode == State.WIN) &&
          okStart) {
        // ready-to-start -> start
        doStart()
        true
      } else if (mMode == State.PAUSE && okStart) {
        // paused -> running
        unpause()
        true
      } else if (mMode == State.RUNNING) keyCode match {
        // center/space -> fire
        case KeyEvent.KEYCODE_DPAD_CENTER | KeyEvent.KEYCODE_SPACE =>
          setFiring(true)
          true
        // left/q -> left
        case KeyEvent.KEYCODE_DPAD_LEFT | KeyEvent.KEYCODE_Q =>
          mRotating = -1
          true
        // right/w -> right
        case KeyEvent.KEYCODE_DPAD_RIGHT| KeyEvent.KEYCODE_W =>
          mRotating = 1
          true
        // up -> pause
        case KeyEvent.KEYCODE_DPAD_UP =>
          pause()
          true
        case _ =>
          false
      } else
        false
    }
  }

  /** Handles a key-up event.
   * 
   *  @param keyCode the key that was pressed
   *  @param msg the original event object
   *  @return true if the key was handled and consumed, or else false
   */
  def doKeyUp(keyCode: Int, msg: KeyEvent): Boolean = {
    var handled = false

    mSurfaceHolder synchronized {
      if (mMode == State.RUNNING) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
         || keyCode == KeyEvent.KEYCODE_SPACE) {
          setFiring(false)
          handled = true
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_Q
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || keyCode == KeyEvent.KEYCODE_W) {
          mRotating = 0
          handled = true
        }
      }
    }
    handled
  }

  /** Draws the ship, fuel/speed bars, and background to the provided
   * Canvas.
   */
  private def doDraw(canvas: Canvas) {
    // Draw the background image. Operations on the Canvas accumulate
    // so this is like clearing the screen.
    canvas.drawBitmap(mBackgroundImage, 0, 0, null)

    val yTop = mCanvasHeight - (mY + mLanderHeight / 2).toInt
    val xLeft = (mX - mLanderWidth / 2).toInt

    // Draw the fuel gauge
    val fuelWidth = (UI_BAR * mFuel / PHYS_FUEL_MAX).toInt
    mScratchRect.set(4, 4, 4 + fuelWidth, 4 + UI_BAR_HEIGHT)
    canvas.drawRect(mScratchRect, mLinePaint)

    // Draw the speed gauge, with a two-tone effect
    val speed = math.sqrt(mDX * mDX + mDY * mDY)
    val speedWidth = (UI_BAR * speed / PHYS_SPEED_MAX).toInt

    if (speed <= mGoalSpeed) {
      mScratchRect.set(4 + UI_BAR + 4, 4,
                       4 + UI_BAR + 4 + speedWidth, 4 + UI_BAR_HEIGHT)
      canvas.drawRect(mScratchRect, mLinePaint)
    } else {
      // Draw the bad color in back, with the good color in front of
      // it
      mScratchRect.set(4 + UI_BAR + 4, 4,
                       4 + UI_BAR + 4 + speedWidth, 4 + UI_BAR_HEIGHT)
      canvas.drawRect(mScratchRect, mLinePaintBad);
      val goalWidth = (UI_BAR * mGoalSpeed / PHYS_SPEED_MAX)
      mScratchRect.set(4 + UI_BAR + 4, 4, 4 + UI_BAR + 4 + goalWidth,
                       4 + UI_BAR_HEIGHT);
      canvas.drawRect(mScratchRect, mLinePaint)
    }

    // Draw the landing pad
    canvas.drawLine(mGoalX, 1 + mCanvasHeight - TARGET_PAD_HEIGHT,
                    mGoalX + mGoalWidth, 1 + mCanvasHeight - TARGET_PAD_HEIGHT,
                    mLinePaint)

    // Draw the ship with its current rotation
    canvas.save()
    canvas.rotate(mHeading.toFloat, mX.toFloat, mCanvasHeight - mY.toFloat)
    if (mMode == State.LOSE) {
       mCrashedImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                    + mLanderHeight)
       mCrashedImage.draw(canvas)
    } else if (mEngineFiring) {
       mFiringImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                      + mLanderHeight)
       mFiringImage.draw(canvas)
    } else {
       mLanderImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                      + mLanderHeight)
       mLanderImage.draw(canvas)
    }
    canvas.restore()
  }

  /** Figures the lander state (x, y, fuel, ...) based on the passage of
   *  realtime. Does not invalidate(). Called at the start of draw().
   *  Detects the end-of-game and sets the UI to the next state.
   */
  private def updatePhysics() {
    val now = System.currentTimeMillis

    // Do nothing if mLastTime is in the future.
    // This allows the game-start to delay the start of the physics
    // by 100ms or whatever.
    if (mLastTime > now) return

    val elapsed = (now - mLastTime) / 1000.0

    // mRotating -- update heading
    if (mRotating != 0) {
      mHeading += mRotating * (PHYS_SLEW_SEC * elapsed)

      // Bring things back into the range 0..360
      if (mHeading < 0) mHeading += 360
      else if (mHeading >= 360) mHeading -= 360
    }

    // Base accelerations -- 0 for x, gravity for y
    var ddx = 0.0
    var ddy = -PHYS_DOWN_ACCEL_SEC * elapsed

    if (mEngineFiring) {
      // taking 0 as up, 90 as to the right
      // cos(deg) is ddy component, sin(deg) is ddx component
      var elapsedFiring = elapsed
      var fuelUsed = elapsedFiring * PHYS_FUEL_SEC

      // tricky case where we run out of fuel partway through the
      // elapsed
      if (fuelUsed > mFuel) {
        elapsedFiring = mFuel / fuelUsed * elapsed
        fuelUsed = mFuel

        // Oddball case where we adjust the "control" from here
        mEngineFiring = false
      }

      mFuel -= fuelUsed

      // have this much acceleration from the engine
      val accel = PHYS_FIRE_ACCEL_SEC * elapsedFiring

      val radians = 2 * math.Pi * mHeading / 360
      ddx = math.sin(radians) * accel
      ddy += math.cos(radians) * accel
    }

    val dxOld = mDX
    val dyOld = mDY

    // figure speeds for the end of the period
    mDX += ddx
    mDY += ddy

    // figure position based on average speed during the period
    mX += elapsed * (mDX + dxOld) / 2
    mY += elapsed * (mDY + dyOld) / 2

    mLastTime = now

    // Evaluate if we have landed ... stop the game
    val yLowerBound =
      TARGET_PAD_HEIGHT + mLanderHeight / 2 - TARGET_BOTTOM_PADDING
    if (mY <= yLowerBound) {
      mY = yLowerBound

      var result = State.LOSE
      var message: CharSequence = ""
      val res = mContext.getResources();
      val speed = math.sqrt(mDX * mDX + mDY * mDY);
      val onGoal = (mGoalX <= mX - mLanderWidth / 2 && mX
                      + mLanderWidth / 2 <= mGoalX + mGoalWidth)

      // "Hyperspace" win -- upside down, going fast,
      // puts you back at the top.
      if (onGoal && math.abs(mHeading - 180) < mGoalAngle
                    && speed > PHYS_SPEED_HYPERSPACE) {
        result = State.WIN
        mWinsInARow += 1
        doStart()

        return
        // Oddball case: this case does a return, all other cases
        // fall through to setMode() below.
      } else if (!onGoal) {
        message = res getText R.string.message_off_pad
      } else if (!(mHeading <= mGoalAngle || mHeading >= 360 - mGoalAngle)) {
        message = res getText R.string.message_bad_angle
      } else if (speed > mGoalSpeed) {
        message = res getText R.string.message_too_fast
      } else {
        result = State.WIN
        mWinsInARow += 1
      }
      setState(result, message)
    }
  }
}
