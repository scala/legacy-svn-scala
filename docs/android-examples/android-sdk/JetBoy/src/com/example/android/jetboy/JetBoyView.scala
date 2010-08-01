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
 * 
 */

// Android JET demonstration code:
// All inline comments related to the use of the JetPlayer class are preceded by "JET info:"

package com.example.android.jetboy

import android.content.Context
import android.content.res.Resources
import android.graphics.{Bitmap, BitmapFactory, Canvas}
import android.media.JetPlayer
import android.media.JetPlayer.OnJetEventListener
import android.os.{Bundle, Handler, Message}
import android.util.{AttributeSet, Log}
import android.view.{KeyEvent, SurfaceHolder, SurfaceView, View}
import android.widget.{Button, TextView}

import java.util.{Timer, TimerTask, Vector}
import java.util.concurrent.ConcurrentLinkedQueue

import scala.util.Random

object JetBoyView {
  // the number of asteroids that must be destroyed
  final val mSuccessThreshold = 50

  final val TAG = "JetBoy"
}

class JetBoyView(context: Context, attrs: AttributeSet)
extends SurfaceView(context, attrs) with SurfaceHolder.Callback {
  import JetBoyView._  // companion object

  // register our interest in hearing about changes to our surface
  val holder = getHolder
  holder addCallback this
        
  // create thread only; it's started in surfaceCreated()
  // except if used in the layout editor.
  if (!isInEditMode) {
    thread = new JetBoyThread(holder, context, new Handler() {
    
      override def handleMessage(m: Message) {
        mTimerView setText m.getData.getString("text")
        if (m.getData.getString("STATE_LOSE") != null) {
          //mButtonRestart setVisibility View.VISIBLE
          mButtonRetry setVisibility View.VISIBLE
          mTimerView setVisibility View.INVISIBLE
          mTextView setVisibility View.VISIBLE
    
          Log.d(TAG, "the total was " + mHitTotal)
    
          if (mHitTotal >= mSuccessThreshold)
            mTextView setText R.string.winText
          else
            mTextView.setText("Sorry, You Lose! You got " + mHitTotal
                            + ". You need 50 to win.")
    
          mTimerView setText "1:12"
          mTextView setHeight 20
    
        }
      }//end handle msg
    })
  }

  setFocusable(true) // make sure we get key events

  Log.d(TAG, "@@@ done creating view!")

  // used to calculate level for mutes and trigger clip
  private var mHitStreak = 0

  // total number asteroids you need to hit.
  private var mHitTotal = 0

  // which music bed is currently playing?
  private var mCurrentBed = 0

  private var mTitleBG2: Bitmap = _

  /**
   * Base class for any external event passed to the JetBoyThread. This can
   * include user input, system events, network input, etc.
   */
  class GameEvent {
    val eventTime = System.currentTimeMillis
  }

  /**
   * A GameEvent subclass for key based user input. Values are those used by
   * the standard onKey
   *
   * Simple constructor to make populating this event easier.
   */
  class KeyGameEvent(var keyCode: Int, var up: Boolean,
                     var msg: KeyEvent) extends GameEvent {
  }

  /**
   * A GameEvent subclass for events from the JetPlayer.
   *
   * Simple constructor to make populating this event easier.
   */
  class JetGameEvent(var player: JetPlayer, var segment: Short,
                     var track: Byte, var channel: Byte,
                     var controller: Byte, var value: Byte) extends GameEvent {
  }

  object JetBoyThread {
    /**
     * State-tracking constants.
     */
    final val STATE_START = -1
    final val STATE_PLAY = 0
    final val STATE_LOSE = 1
    final val STATE_PAUSE = 2
    final val STATE_RUNNING = 3
        
    // how many frames per beat? The basic animation can be changed for
    // instance to 3/4 by changing this to 3.
    // untested is the impact on other parts of game logic for non 4/4 time.
    private final val ANIMATION_FRAMES_PER_BEAT = 4
  }

  // JET info: the JetBoyThread receives all the events from the JET player
  // JET info: through the OnJetEventListener interface.
  class JetBoyThread(surfaceHolder: SurfaceHolder, 
                     context: Context,
                     handler: Handler) extends Thread with OnJetEventListener {
    import JetBoyThread._  // companion object

    /** Handle to the surface manager object we interact with */
    private val mSurfaceHolder = surfaceHolder

    /** Message handler used by thread to interact with TextView */
    private val mHandler = handler

    /** Handle to the application context, used to e.g. fetch Drawables. */
    private val mContext = context
    
    private val mRes = context.getResources

    // array to store the mute masks that are applied during game play
    // to respond to the player's hit streaks
    private val muteMask = new Array[Array[Boolean]](9)//(32)

    // JET info: this are the mute arrays associated with the music beds in the
    // JET info: JET file
    for (ii <- 0 until 8) {
      muteMask(ii) = Array.fill(32)(true)
    }
    muteMask(0)(2) = false
    muteMask(0)(3) = false
    muteMask(0)(4) = false
    muteMask(0)(5) = false

    muteMask(1)(2) = false
    muteMask(1)(3) = false
    muteMask(1)(4) = false
    muteMask(1)(5) = false
    muteMask(1)(8) = false
    muteMask(1)(9) = false

    muteMask(2)(2) = false
    muteMask(2)(3) = false
    muteMask(2)(6) = false
    muteMask(2)(7) = false
    muteMask(2)(8) = false
    muteMask(2)(9) = false

    muteMask(3)(2) = false
    muteMask(3)(3) = false
    muteMask(3)(6) = false
    muteMask(3)(11) = false
    muteMask(3)(12) = false

    muteMask(4)(2) = false
    muteMask(4)(3) = false
    muteMask(4)(10) = false
    muteMask(4)(11) = false
    muteMask(4)(12) = false
    muteMask(4)(13) = false

    muteMask(5)(2) = false
    muteMask(5)(3) = false
    muteMask(5)(10) = false
    muteMask(5)(12) = false
    muteMask(5)(15) = false
    muteMask(5)(17) = false

    muteMask(6)(2) = false
    muteMask(6)(3) = false
    muteMask(6)(14) = false
    muteMask(6)(15) = false
    muteMask(6)(16) = false
    muteMask(6)(17) = false

    muteMask(7)(2) = false
    muteMask(7)(3) = false
    muteMask(7)(6) = false
    muteMask(7)(14) = false
    muteMask(7)(15) = false
    muteMask(7)(16) = false
    muteMask(7)(17) = false
    muteMask(7)(18) = false

    // set all tracks to play
    muteMask(8) = Array.fill(32)(false)

    // always set state to start, ensure we come in from front door if
    // app gets tucked into background
    mState = STATE_START

    setInitialGameState()

    // a lazy graphic fudge for the initial title splash
    private var mTitleBG: Bitmap =
      BitmapFactory.decodeResource(mRes, R.drawable.title_hori)

    // load background image as a Bitmap instead of a Drawable b/c
    // we don't need to transform it and it's faster to draw this
    // way...thanks lunar lander :)

    // two background since we want them moving at different speeds

    /** The drawable to use as the far background of the animation canvas */
    private var mBackgroundImageFar: Bitmap =
      BitmapFactory.decodeResource(mRes, R.drawable.background_a)

    private val mLaserShot: Bitmap =
      BitmapFactory.decodeResource(mRes, R.drawable.laser)

    /** The drawable to use as the close background of the animation canvas */
    private var mBackgroundImageNear: Bitmap =
      BitmapFactory.decodeResource(mRes, R.drawable.background_b)

    // our intrepid space boy
    private val mShipFlying = Array( //Bitmap
      BitmapFactory.decodeResource(mRes, R.drawable.ship2_1),
      BitmapFactory.decodeResource(mRes, R.drawable.ship2_2),
      BitmapFactory.decodeResource(mRes, R.drawable.ship2_3),
      BitmapFactory.decodeResource(mRes, R.drawable.ship2_4))

    // the twinkly bit
    private val mBeam = Array( //Bitmap
      BitmapFactory.decodeResource(mRes, R.drawable.intbeam_1),
      BitmapFactory.decodeResource(mRes, R.drawable.intbeam_2),
      BitmapFactory.decodeResource(mRes, R.drawable.intbeam_3),
      BitmapFactory.decodeResource(mRes, R.drawable.intbeam_4))

    private var mTimerShell =
      BitmapFactory.decodeResource(mRes, R.drawable.int_timer)

    // the things you are trying to hit
    private val mAsteroids = Array( //Bitmap
      // I wanted them to rotate in a certain way
      // so I loaded them backwards from the way created.
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid12),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid11),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid10),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid09),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid08),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid07),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid06),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid05),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid04),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid03),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid02),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid01))

    // hit animation
    private val mExplosions = Array( //Bitmap
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode1),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode2),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode3),
      BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode4))

    var mInitialized = false

    /** Queue for GameEvents */
    protected val mEventQueue = new ConcurrentLinkedQueue[GameEvent]

    /** Context for processKey to maintain state accross frames * */
    protected var mKeyContext: Object = null

    // the timer display in seconds
    var mTimerLimit: Int = _

    // used for internal timing logic.
    final val TIMER_LIMIT = 72

    // string value for timer display
    private var mTimerValue = "1:12"

    // start, play, running, lose are the states we use
    var mState: Int = _

    // has laser been fired and for how long?
    // user for fx logic on laser fire
    var mLaserOn = false

    var mLaserFireTime = 0L

    // JET info: event IDs within the JET file.
    // JET info: in this game 80 is used for sending asteroid across the screen
    // JET info: 82 is used as game time for 1/4 note beat.
    private final val NEW_ASTEROID_EVENT: Byte = 80
    private final val TIMER_EVENT: Byte = 82

    // used to track beat for synch of mute/unmute actions
    private var mBeatCount = 1

    // used to save the beat event system time.
    private var mLastBeatTime: Long = _

    private var mPassedTime: Long = _

    // how much do we move the asteroids per beat?
    private var mPixelMoveX = 25

    // the asteroid send events are generated from the Jet File.
    // but which land they start in is random.
    private val mRandom = new Random

    // JET info: the star of our show, a reference to the JetPlayer object.
    private var mJet: JetPlayer = _

    private var mJetPlaying = false

    /** Indicate whether the surface has been created & is ready to draw */
    private var mRun = false

    // updates the screen clock. Also used for tempo timing.
    private var mTimer: Timer = _

    private var mTimerTask: TimerTask = _

    // one second - used to update timer
    private var mTaskIntervalInMillis = 1000

    /**
     * Current height of the surface/canvas.
     * 
     * @see #setSurfaceSize
     */
    private var mCanvasHeight = 1

    /**
     * Current width of the surface/canvas.
     * 
     * @see #setSurfaceSize
     */
    private var mCanvasWidth = 1

    // used to track the picture to draw for ship animation
    private var mShipIndex = 0

    // stores all of the asteroid objects in order
    private var mDangerWillRobinson: Vector[Asteroid] = _

    private var mExplosion: Vector[Explosion] = _

    // right to left scroll tracker for near and far BG
    private var mBGFarMoveX = 0
    private var mBGNearMoveX = 0

    // how far up (close to top) jet boy can fly
    private var mJetBoyYMin = 40
    private var mJetBoyX = 0
    private var mJetBoyY = 0

    // this is the pixel position of the laser beam guide.
    private var mAsteroidMoveLimitX = 110

    // how far up asteroid can be painted
    private var mAsteroidMinY = 40

    /**
     * Does the grunt work of setting up initial jet requirements
     */
    private def initializeJetPlayer() {

      // JET info: let's create our JetPlayer instance using the factory.
      // JET info: if we already had one, the same singleton is returned.
      mJet = JetPlayer.getJetPlayer

      mJetPlaying = false

      // JET info: make sure we flush the queue,
      // JET info: otherwise left over events from previous gameplay can hang around.
      // JET info: ok, here we don't really need that but if you ever reuse a
      // JET info: JetPlayer instance, clear the queue before reusing it, this
      // JET info: will also clear any trigger clips that have been triggered
      // JET info: but not played yet.
      mJet.clearQueue()

      // JET info: we are going to receive in this example all the JET callbacks
      // JET info: inthis animation thread object. 
      mJet setEventListener this

      Log.d(TAG, "opening jet file")

      // JET info: load the actual JET content the game will be playing,
      // JET info: it's stored as a raw resource in our APK, and is labeled "level1"
      mJet loadJetFile mContext.getResources.openRawResourceFd(R.raw.level1)
      // JET info: if our JET file was stored on the sdcard for instance, we would have used
      // JET info: mJet.loadJetFile("/sdcard/level1.jet")

      Log.d(TAG, "opening jet file DONE")

      mCurrentBed = 0
      var sSegmentID: Byte = 0

      Log.d(TAG, " start queuing jet file")
            
      // JET info: now we're all set to prepare queuing the JET audio segments for the game.
      // JET info: in this example, the game uses segment 0 for the duration of the game play,
      // JET info: and plays segment 1 several times as the "outro" music, so we're going to
      // JET info: queue everything upfront, but with more complex JET compositions, we could
      // JET info: also queue the segments during the game play.

      // JET info: this is the main game play music
      // JET info: it is located at segment 0
      // JET info: it uses the first DLS lib in the .jet resource, which is at index 0
      // JET info: index -1 means no DLS
      mJet.queueJetSegment(0, 0, 0, 0, 0, sSegmentID)

      // JET info: end game music, loop 4 times normal pitch
      mJet.queueJetSegment(1, 0, 4, 0, 0, sSegmentID)

      // JET info: end game music loop 4 times up an octave
      mJet.queueJetSegment(1, 0, 4, 1, 0, sSegmentID)

      // JET info: set the mute mask as designed for the beginning of the game, when the
      // JET info: the player hasn't scored yet.
      mJet.setMuteArray(muteMask(0), true)

      Log.d(TAG, " start queuing jet file DONE")

    }
        
    private def doDraw(canvas: Canvas) {
      if (mState == STATE_RUNNING) {
        doDrawRunning(canvas)
      } else if (mState == STATE_START) {
        doDrawReady(canvas)
      } else if (mState == STATE_PLAY || mState == STATE_LOSE) {
        if (mTitleBG2 == null) {
            mTitleBG2 = BitmapFactory.decodeResource(mRes, R.drawable.title_bg_hori)
        }
        doDrawPlay(canvas)
      }// end state play block
    }

    /**
     * Draws current state of the game Canvas.
     */
    private def doDrawRunning(canvas: Canvas) {
      // decrement the far background
      mBGFarMoveX = mBGFarMoveX - 1

      // decrement the near background
      mBGNearMoveX = mBGNearMoveX - 4

      // calculate the wrap factor for matching image draw
      val newFarX = mBackgroundImageFar.getWidth - (-mBGFarMoveX)

      // if we have scrolled all the way, reset to start
      if (newFarX <= 0) {
        mBGFarMoveX = 0
        // only need one draw
        canvas.drawBitmap(mBackgroundImageFar, mBGFarMoveX, 0, null)

      } else {
        // need to draw original and wrap
        canvas.drawBitmap(mBackgroundImageFar, mBGFarMoveX, 0, null)
        canvas.drawBitmap(mBackgroundImageFar, newFarX, 0, null)
      }

      // same story different image...
      // TODO possible method call
      val newNearX = mBackgroundImageNear.getWidth() - (-mBGNearMoveX)

      if (newNearX <= 0) {
        mBGNearMoveX = 0
        canvas.drawBitmap(mBackgroundImageNear, mBGNearMoveX, 0, null)

      } else {
        canvas.drawBitmap(mBackgroundImageNear, mBGNearMoveX, 0, null)
        canvas.drawBitmap(mBackgroundImageNear, newNearX, 0, null)
      }

      doAsteroidAnimation(canvas)

      canvas.drawBitmap(mBeam(mShipIndex), 51 + 20, 0, null)

      mShipIndex += 1

      if (mShipIndex == 4)
        mShipIndex = 0

      // draw the space ship in the same lane as the next asteroid
      canvas.drawBitmap(mShipFlying(mShipIndex), mJetBoyX, mJetBoyY, null)

      if (mLaserOn) {
        canvas.drawBitmap(mLaserShot,
                          mJetBoyX + mShipFlying(0).getWidth,
                          mJetBoyY + (mShipFlying(0).getHeight / 2), null)
      }

      // tick tock
      canvas.drawBitmap(mTimerShell, mCanvasWidth - mTimerShell.getWidth, 0, null)

    }

    private def setInitialGameState() {
      mTimerLimit = TIMER_LIMIT

      mJetBoyY = mJetBoyYMin

      // set up jet stuff
      initializeJetPlayer()

      mTimer = new Timer

      mDangerWillRobinson = new Vector[Asteroid]

      mExplosion = new Vector[Explosion]

      mInitialized = true

      mHitStreak = 0
      mHitTotal = 0
    }

    private def doAsteroidAnimation(canvas: Canvas) {
      if ((mDangerWillRobinson == null | mDangerWillRobinson.size == 0)
               && (mExplosion != null && mExplosion.size == 0))
        return

      // Compute what percentage through a beat we are and adjust
      // animation and position based on that. This assumes 140bpm(428ms/beat).
      // This is just inter-beat interpolation, no game state is updated
      val frameDelta = System.currentTimeMillis - mLastBeatTime

      val animOffset = (ANIMATION_FRAMES_PER_BEAT * frameDelta / 428).toInt

      for (i <- (mDangerWillRobinson.size() - 1) until 0 by -1) {
        val asteroid: Asteroid = mDangerWillRobinson elementAt i

        if (!asteroid.mMissed)
          mJetBoyY = asteroid.mDrawY

        // Log.d(TAG, " drawing asteroid " + ii + " at " +
        // asteroid.mDrawX )

        canvas.drawBitmap(
          mAsteroids((asteroid.mAniIndex + animOffset) % mAsteroids.length),
          asteroid.mDrawX, asteroid.mDrawY, null);
      }

      for (i <- (mExplosion.size() - 1) to 0 by -1) {
        val ex: Explosion = mExplosion elementAt i
        val ex1 = mExplosions((ex.mAniIndex + animOffset) % mExplosions.length)
        canvas.drawBitmap(ex1, ex.mDrawX, ex.mDrawY, null)
      }
    }

    private def doDrawReady(canvas: Canvas) {
      canvas.drawBitmap(mTitleBG, 0, 0, null)
    }

    private def doDrawPlay(canvas: Canvas) {
      canvas.drawBitmap(mTitleBG2, 0, 0, null)
    }

    /**
     * the heart of the worker bee
     */
    override def run() {
      // while running do stuff in this loop...bzzz!
      while (mRun) {
        var c: Canvas = null

        if (mState == STATE_RUNNING) {
          // Process any input and apply it to the game state
          updateGameState()

          if (!mJetPlaying) {

            mInitialized = false
            Log.d(TAG, "------> STARTING JET PLAY")
            mJet.play()

            mJetPlaying = true

          }

          mPassedTime = System.currentTimeMillis

          // kick off the timer task for counter update if not already
          // initialized
          if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
              def run() {
                doCountDown()
              }
            }

            mTimer.schedule(mTimerTask, mTaskIntervalInMillis);

          }// end of TimerTask init block

        }// end of STATE_RUNNING block
        else if (mState == STATE_PLAY && !mInitialized) {
          setInitialGameState()
        } else if (mState == STATE_LOSE) {
          mInitialized = false
        }

        try {
          c = mSurfaceHolder.lockCanvas(null)
          // synchronized (mSurfaceHolder) {
          doDraw(c)
          // }
        } finally {
          // do this in a finally so that if an exception is thrown
          // during the above, we don't leave the Surface in an
          // inconsistent state
          if (c != null) {
            mSurfaceHolder unlockCanvasAndPost c
          }
        }// end finally block
      }// end while mrun block
    }

    /**
     * This method handles updating the model of the game state. No
     * rendering is done here only processing of inputs and update of state.
     * This includes positons of all game objects (asteroids, player,
     * explosions), their state (animation frame, hit), creation of new
     * objects, etc.
     */
    protected def updateGameState() {
      // Process any game events and apply them
      var event: GameEvent = mEventQueue.poll()
      while (event != null) {
        // Log.d(TAG,"*** EVENT = " + event);

        // Process keys tracking the input context to pass in to later
        // calls
        event match {
          case keyEvent: KeyGameEvent =>
            // Process the key for affects other then asteroid hits
            mKeyContext = processKeyEvent(keyEvent, mKeyContext)

            // Update laser state. Having this here allows the laser to
            // be triggered right when the key is
            // pressed. If we comment this out the laser will only be
            // turned on when updateLaser is called
            // when processing a timer event below.
            updateLaser(mKeyContext)

          // JET events trigger a state update
          case jetEvent: JetGameEvent =>

            // Only update state on a timer event
            if (jetEvent.value == TIMER_EVENT) {
              // Note the time of the last beat
              mLastBeatTime = System.currentTimeMillis

              // Update laser state, turning it on if a key has been
              // pressed or off if it has been
              // on for too long.
              updateLaser(mKeyContext)

              // Update explosions before we update asteroids because
              // updateAsteroids may add
              // new explosions that we do not want updated until next
              // frame
              updateExplosions(mKeyContext)

              // Update asteroid positions, hit status and animations
              updateAsteroids(mKeyContext)
            }

            processJetEvent(jetEvent.player, jetEvent.segment, jetEvent.track,
                            jetEvent.channel, jetEvent.controller, jetEvent.value)
          case _ =>
            // do nothing
        } //match
        event = mEventQueue.poll()
      } //while
    }

    /**
     * This method handles the state updates that can be caused by key press
     * events. Key events may mean different things depending on what has
     * come before, to support this concept this method takes an opaque
     * context object as a parameter and returns an updated version. This
     * context should be set to null for the first event then should be set
     * to the last value returned for subsequent events.
     */
    protected def processKeyEvent(event: KeyGameEvent, context: Object): Object = {
      // Log.d(TAG, "key code is " + event.keyCode + " " + (event.up ?
      // "up":"down"));

      // If it is a key up on the fire key make sure we mute the
      // associated sound
      if (event.up) {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
          return null
        }
      }
      // If it is a key down on the fire key start playing the sound and
      // update the context
      // to indicate that a key has been pressed and to ignore further
      // presses
      else {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER && (context == null)) {
          return event
        }
      }

      // Return the context unchanged
      context
    }

    /**
     * This method updates the laser status based on user input and shot
     * duration
     */
    protected def updateLaser(inputContext: Object) {
      // Lookup the time of the fire event if there is one
      val keyTime =
        if (inputContext == null) 0L
        else inputContext.asInstanceOf[GameEvent].eventTime

      // Log.d(TAG,"keyTime delta = " +
      // (System.currentTimeMillis()-keyTime) + ": obj = " +
      // inputContext);

      // If the laser has been on too long shut it down
      if (mLaserOn && System.currentTimeMillis - mLaserFireTime > 400) {
        mLaserOn = false
      }

      // trying to tune the laser hit timing
      else if (System.currentTimeMillis - mLaserFireTime > 300) {
        // JET info: the laser sound is on track 23, we mute it (true) right away (false)
        mJet.setMuteFlag(23, true, false)

      }

      // Now check to see if we should turn the laser on. We do this after
      // the above shutdown
      // logic so it can be turned back on in the same frame it was turned
      // off in. If we want
      // to add a cooldown period this may change.
      if (!mLaserOn && System.currentTimeMillis - keyTime <= 400) {

        mLaserOn = true
        mLaserFireTime = keyTime

        // JET info: unmute the laser track (false) right away (false)
        mJet.setMuteFlag(23, false, false)
      }
    }

    /**
     * Update asteroid state including position and laser hit status.
     */
    protected def updateAsteroids(inputContext: Object) {
      if (mDangerWillRobinson == null | mDangerWillRobinson.size() == 0)
        return

      for (i <- (mDangerWillRobinson.size() - 1) to 0 by -1) {
        val asteroid: Asteroid = mDangerWillRobinson.elementAt(i)
        var continue1 = false

        // If the asteroid is within laser range but not already missed
        // check if the key was pressed close enough to the beat to make a hit
        if (asteroid.mDrawX <= mAsteroidMoveLimitX + 20 && !asteroid.mMissed) {
          // If the laser was fired on the beat destroy the asteroid
          if (mLaserOn) {
            // Track hit streak for adjusting music
            mHitStreak += 1
            mHitTotal += 1

            // replace the asteroid with an explosion
            val ex = new Explosion
            ex.mAniIndex = 0
            ex.mDrawX = asteroid.mDrawX
            ex.mDrawY = asteroid.mDrawY
            mExplosion add ex

            mJet.setMuteFlag(24, false, false)

            mDangerWillRobinson removeElementAt i

            // This asteroid has been removed process the next one
            continue1 = true // continue (Java)
          } else {
            // Sorry, timing was not good enough, mark the asteroid
            // as missed so on next frame it cannot be hit even if it is still
            // within range
            asteroid.mMissed = true

            mHitStreak -= 1

            if (mHitStreak < 0)
              mHitStreak = 0

          }
        }

        if (!continue1) {
          // Update the asteroids position, even missed ones keep moving
          asteroid.mDrawX -= mPixelMoveX

          // Update asteroid animation frame
          asteroid.mAniIndex =
            (asteroid.mAniIndex + ANIMATION_FRAMES_PER_BEAT) % mAsteroids.length

          // if we have scrolled off the screen
          if (asteroid.mDrawX < 0) {
            mDangerWillRobinson removeElementAt i
          }
        }
      }
    }

    /**
     * This method updates explosion animation and removes them once they
     * have completed.
     */
    protected def updateExplosions(inputContext: Object) {
      if (mExplosion == null | mExplosion.size == 0)
        return

      for (i <- (mExplosion.size() - 1) to 0 by -1) {
        val ex: Explosion = mExplosion elementAt i

        ex.mAniIndex += ANIMATION_FRAMES_PER_BEAT

        // When the animation completes remove the explosion
        if (ex.mAniIndex > 3) {
          mJet.setMuteFlag(24, true, false)
          mJet.setMuteFlag(23, true, false)

          mExplosion removeElementAt i
        }
      }
    }

    /**
     * This method handles the state updates that can be caused by JET
     * events.
     */
    protected def processJetEvent(player: JetPlayer, segment: Short,
                                  track: Byte, channel: Byte,
                                  controller: Byte, value: Byte) {

      //Log.d(TAG, "onJetEvent(): seg=" + segment + " track=" + track + " chan=" + channel
      //        + " cntrlr=" + controller + " val=" + value);

      // Check for an event that triggers a new asteroid
      if (value == NEW_ASTEROID_EVENT) {
        doAsteroidCreation()
      }

      mBeatCount += 1

      if (mBeatCount > 4) {
        mBeatCount = 1
      }

      // Scale the music based on progress

      // it was a game requirement to change the mute array on 1st beat of
      // the next measure when needed
      // and so we track beat count, after that we track hitStreak to
      // determine the music "intensity"
      // if the intensity has go gone up, call a corresponding trigger clip,
      // otherwise just execute the rest of the music bed change logic.
      if (mBeatCount == 1) {

        // do it back wards so you fall into the correct one
        if (mHitStreak > 28) {

          // did the bed change?
          if (mCurrentBed != 7) {
            // did it go up?
            if (mCurrentBed < 7) {
              mJet.triggerClip(7)
            }
            mCurrentBed = 7
            // JET info: change the mute mask to update the way the music plays
            // JET info: based on the player's skills.
            mJet.setMuteArray(muteMask(7), false)
          }
        } else if (mHitStreak > 24) {
          if (mCurrentBed != 6) {
            if (mCurrentBed < 6) {
              // JET info: quite a few asteroids hit, trigger the clip with
              // JET info: the guy's voice that encourages the player.
              mJet.triggerClip(6)
            }
            mCurrentBed = 6
            mJet.setMuteArray(muteMask(6), false)
          }
        } else if (mHitStreak > 20) {
          if (mCurrentBed != 5) {
            if (mCurrentBed < 5) {
              mJet.triggerClip(5)
            }
            mCurrentBed = 5
            mJet.setMuteArray(muteMask(5), false)
          }
        } else if (mHitStreak > 16) {
          if (mCurrentBed != 4) {
            if (mCurrentBed < 4) {
              mJet.triggerClip(4)
            }
            mCurrentBed = 4
            mJet.setMuteArray(muteMask(4), false)
          }
        } else if (mHitStreak > 12) {
          if (mCurrentBed != 3) {
            if (mCurrentBed < 3) {
              mJet.triggerClip(3)
            }
            mCurrentBed = 3
            mJet.setMuteArray(muteMask(3), false)
          }
        } else if (mHitStreak > 8) {
          if (mCurrentBed != 2) {
            if (mCurrentBed < 2) {
              mJet.triggerClip(2)
            }
            mCurrentBed = 2
            mJet.setMuteArray(muteMask(2), false)
          }
        } else if (mHitStreak > 4) {
          if (mCurrentBed != 1) {
            if (mCurrentBed < 1) {
              mJet.triggerClip(1)
            }
            mCurrentBed = 1
            mJet.setMuteArray(muteMask(1), false)
          }
        }
      }
    }

    private def doAsteroidCreation() {
      // Log.d(TAG, "asteroid created")

      val _as = new Asteroid

      val drawIndex = mRandom nextInt 4

      // TODO Remove hard coded value
      _as.mDrawY = mAsteroidMinY + (drawIndex * 63)

      _as.mDrawX = mCanvasWidth - mAsteroids(0).getWidth

      _as.mStartTime = System.currentTimeMillis

      mDangerWillRobinson add _as
    }

    /**
     * Used to signal the thread whether it should be running or not.
     * Passing true allows the thread to run; passing false will shut it
     * down if it's already running. Calling start() after this was most
     * recently called with false will result in an immediate shutdown.
     * 
     * @param b true to run, false to shut down
     */
    def setRunning(b: Boolean) {
      mRun = b

      if (!mRun) {
        if (mTimerTask != null)
          mTimerTask.cancel()
      }
    }

    /**
     * returns the current int value of game state as defined by state
     * tracking constants
     * 
     * @return
     */
    def getGameState: Int = {
      mSurfaceHolder synchronized {
        mState
      }
    }

    /**
     * Sets the game mode. That is, whether we are running, paused, in the
     * failure state, in the victory state, etc.
     * 
     * @see #setState(int, CharSequence)
     * @param mode one of the STATE_* constants
     */
    def setGameState(mode: Int) {
      mSurfaceHolder synchronized {
        setGameState(mode, null)
      }
    }

    /**
     * Sets state based on input, optionally also passing in a text message.
     * 
     * @param state
     * @param message
     */
    def setGameState(state: Int, message: CharSequence) {

      mSurfaceHolder synchronized {

        // change state if needed
        if (mState != state) {
          mState = state
        }

        if (mState == STATE_PLAY) {
          val res = mContext.getResources
          mBackgroundImageFar =
            BitmapFactory.decodeResource(res, R.drawable.background_a)

          // don't forget to resize the background image
          mBackgroundImageFar =
            Bitmap.createScaledBitmap(mBackgroundImageFar,
                            mCanvasWidth * 2, mCanvasHeight, true)

          mBackgroundImageNear =
            BitmapFactory.decodeResource(res, R.drawable.background_b)

          // don't forget to resize the background image
          mBackgroundImageNear =
            Bitmap.createScaledBitmap(mBackgroundImageNear,
                            mCanvasWidth * 2, mCanvasHeight, true)

        } else if (mState == STATE_RUNNING) {
          // When we enter the running state we should clear any old
          // events in the queue
          mEventQueue.clear()

          // And reset the key state so we don't think a button is pressed
          // when it isn't
          mKeyContext = null
        }

      }
    }

    /**
     * Add key press input to the GameEvent queue
     */
    def doKeyDown(keyCode: Int, msg: KeyEvent): Boolean = {
      mEventQueue.add(new KeyGameEvent(keyCode, false, msg))
      true
    }

    /**
     * Add key press input to the GameEvent queue
     */
    def doKeyUp(keyCode: Int, msg: KeyEvent): Boolean = {
      mEventQueue.add(new KeyGameEvent(keyCode, true, msg))
      true
    }

    /* Callback invoked when the surface dimensions change. */
    def setSurfaceSize(width: Int, height: Int) {
      // synchronized to make sure these all change atomically
      mSurfaceHolder synchronized {
        mCanvasWidth = width
        mCanvasHeight = height

        // don't forget to resize the background image
        mBackgroundImageFar =
          Bitmap.createScaledBitmap(mBackgroundImageFar, width * 2,
                        height, true)

        // don't forget to resize the background image
        mBackgroundImageNear =
          Bitmap.createScaledBitmap(mBackgroundImageNear, width * 2,
                        height, true)
      }
    }

    /**
     * Pauses the physics update & animation.
     */
    def pause() {
      mSurfaceHolder synchronized {
        if (mState == STATE_RUNNING)
          setGameState(STATE_PAUSE)
        if (mTimerTask != null)
          mTimerTask.cancel()
        if (mJet != null)
          mJet.pause()
      }
    }

    /**
     * Does the work of updating timer
     * 
     */
    private def doCountDown() {
      //Log.d(TAG,"Time left is " + mTimerLimit);

      mTimerLimit -= 1
      try {
        //subtract one minute and see what the result is.
        val moreThanMinute = mTimerLimit - 60

        mTimerValue =
          if (moreThanMinute >= 0)
            (if (moreThanMinute > 9) "1:" else "1:0") + moreThanMinute
          else
            (if (mTimerLimit > 9) "0:" else "0:0") + mTimerLimit
      } catch {
        case e1: Exception =>
          Log.e(TAG, "doCountDown threw " + e1.toString)
      }

      val msg = mHandler.obtainMessage()

      val b = new Bundle
      b.putString("text", mTimerValue)

      //time's up
      if (mTimerLimit == 0) {
        b.putString("STATE_LOSE", "" + STATE_LOSE)
        mTimerTask = null

        mState = STATE_LOSE

      } else {
        mTimerTask = new TimerTask() {
          def run() {
            doCountDown()
          }
        }

        mTimer.schedule(mTimerTask, mTaskIntervalInMillis)
      }

      //this is how we send data back up to the main JetBoyView thread.
      //if you look in constructor of JetBoyView you will see code for
      //Handling of messages. This is borrowed directly from lunar lander.
      //Thanks again!
      msg setData b
      mHandler sendMessage msg
    }

    // JET info: JET event listener interface implementation:
    /**
     * required OnJetEventListener method. Notifications for queue updates
     * 
     * @param player
     * @param nbSegments
     */
    def onJetNumQueuedSegmentUpdate(player: JetPlayer, nbSegments: Int) {
      //Log.i(TAG, "onJetNumQueuedUpdate(): nbSegs =" + nbSegments)
    }

    // JET info: JET event listener interface implementation:
    /**
     * The method which receives notification from event listener.
     * This is where we queue up events 80 and 82.
     * 
     * Most of this data passed is unneeded for JetBoy logic but shown 
     * for code sample completeness.
     * 
     * @param player
     * @param segment
     * @param track
     * @param channel
     * @param controller
     * @param value
     */
    def onJetEvent(player: JetPlayer, segment: Short,
                   track: Byte, channel: Byte,
                   controller: Byte, value: Byte) {

      //Log.d(TAG, "jet got event " + value)

      //events fire outside the animation thread. This can cause timing issues.
      //put in queue for processing by animation thread.
      mEventQueue.add(new JetGameEvent(player, segment, track, channel, controller, value))
    }

    // JET info: JET event listener interface implementation:
    def onJetPauseUpdate(player: JetPlayer, paused: Int) {
      //Log.i(TAG, "onJetPauseUpdate(): paused =" + paused)
    }

    // JET info: JET event listener interface implementation:
    def onJetUserIdUpdate(player: JetPlayer, userId: Int, repeatCount: Int) {
      //Log.i(TAG, "onJetUserIdUpdate(): userId =" + userId + " repeatCount=" + repeatCount)
    }

  }//end thread class

  /** The thread that actually draws the animation */
  private var thread: JetBoyThread = _

  private var mTimerView: TextView = _

  private var mButtonRetry: Button = _

  // private var mButtonRestart: Button = _
  private var mTextView: TextView = _

  /**
   * Pass in a reference to the timer view widget so we can update it from here.
   * 
   * @param tv
   */
  def setTimerView(tv: TextView) {
    mTimerView = tv
  }

  /**
   * Standard window-focus override. Notice focus lost so we can pause on
   * focus lost. e.g. user switches to take a call.
   */
  override def onWindowFocusChanged(hasWindowFocus: Boolean) {
    if (!hasWindowFocus) {
      if (thread != null)
        thread.pause()
    }
  }

  /**
   * Fetches the animation thread corresponding to this LunarView.
   * 
   * @return the animation thread
   */
  def getThread: JetBoyThread =
    thread

  /* Callback invoked when the surface dimensions change. */
  def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    thread.setSurfaceSize(width, height)
  }

  def surfaceCreated(arg0: SurfaceHolder) {
    // start the thread here so that we don't busy-wait in run()
    // waiting for the surface to be created
    thread setRunning true
    thread.start()
  }

  def surfaceDestroyed(arg0: SurfaceHolder) {
    var retry = true
    thread setRunning false
    while (retry) {
      try {
        thread.join()
        retry = false
      } catch {
        case e: InterruptedException =>
      }
    }
  }

  /**
   * A reference to the button to start game over.
   * 
   * @param _buttonRetry
   * 
   */
  def setButtonView(_buttonRetry: Button) {
    mButtonRetry = _buttonRetry
    // mButtonRestart = _buttonRestart
  }

  //we reuse the help screen from the end game screen.
  def setTextView(textView: TextView) {
    mTextView = textView
  }
}
