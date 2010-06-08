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

import android.app.Activity
import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.os.{Bundle, RemoteException, Handler, IBinder, Message, Process}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView, Toast}


object RemoteServiceBinding {
    private final val BUMP_MSG = 1
}

class RemoteServiceBinding extends Activity {
  import RemoteServiceBinding._  // companion object

  /** The primary interface we will be calling on the service. */
  var mService: IRemoteService = null
  /** Another interface we use on the service. */
  var mSecondaryService: ISecondary = null
    
  private var mKillButton: Button = _
  private var mCallbackText: TextView = _

  private var mIsBound: Boolean = _

  /**
   * Standard initialization of this activity.  Set up the UI, then wait
   * for the user to poke it before doing anything.
   */
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.remote_service_binding)

    // Watch for button clicks.
    var button = findViewById(R.id.bind).asInstanceOf[Button]
    button setOnClickListener mBindListener
    button = findViewById(R.id.unbind).asInstanceOf[Button]
    button setOnClickListener mUnbindListener
    mKillButton = findViewById(R.id.kill).asInstanceOf[Button]
    mKillButton setOnClickListener mKillListener
    mKillButton setEnabled false
        
    mCallbackText = findViewById(R.id.callback).asInstanceOf[TextView]
    mCallbackText setText "Not attached."
  }

  /**
   * Class for interacting with the main interface of the service.
   */
  private val mConnection = new ServiceConnection {
    def onServiceConnected(className: ComponentName, service: IBinder) {
      // This is called when the connection with the service has been
      // established, giving us the service object we can use to
      // interact with the service.  We are communicating with our
      // service through an IDL interface, so get a client-side
      // representation of that from the raw service object.
      mService = IRemoteService.Stub.asInterface(service)
      mKillButton setEnabled true
      mCallbackText setText "Attached."

      // We want to monitor the service for as long as we are
      // connected to it.
      try {
        mService registerCallback mCallback
      } catch {
        case e: RemoteException =>
          // In this case the service has crashed before we could even
          // do anything with it; we can count on soon being
          // disconnected (and then reconnected if it can be restarted)
          // so there is no need to do anything here.
      }
            
      // As part of the sample, tell the user what happened.
      Toast.makeText(RemoteServiceBinding.this, R.string.remote_service_connected,
                     Toast.LENGTH_SHORT).show();
    }

    def onServiceDisconnected(className: ComponentName) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      mService = null
      mKillButton setEnabled false
      mCallbackText setText "Disconnected."

      // As part of the sample, tell the user what happened.
      Toast.makeText(RemoteServiceBinding.this, R.string.remote_service_disconnected,
                    Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * Class for interacting with the secondary interface of the service.
   */
  private val mSecondaryConnection = new ServiceConnection {
    def onServiceConnected(className: ComponentName, service: IBinder) {
      // Connecting to a secondary interface is the same as any
      // other interface.
      mSecondaryService = ISecondary.Stub.asInterface(service)
      mKillButton setEnabled true
    }

    def onServiceDisconnected(className: ComponentName) {
      mSecondaryService = null
      mKillButton setEnabled false
    }
  }

  private val mBindListener = new OnClickListener {
    def onClick(v: View) {
      // Establish a couple connections with the service, binding
      // by interface names.  This allows other applications to be
      // installed that replace the remote service by implementing
      // the same interface.
      bindService(new Intent(classOf[IRemoteService].getName),
                  mConnection, Context.BIND_AUTO_CREATE)
      bindService(new Intent(classOf[ISecondary].getName),
                  mSecondaryConnection, Context.BIND_AUTO_CREATE)
      mIsBound = true
      mCallbackText setText "Binding."
    }
  }

  private val mUnbindListener = new OnClickListener {
    def onClick(v: View) {
      if (mIsBound) {
        // If we have received the service, and hence registered with
        // it, then now is the time to unregister.
        if (mService != null) {
          try {
            mService unregisterCallback mCallback
          } catch {
            case e: RemoteException =>
              // There is nothing special we need to do if the service
              // has crashed.
          }
        }
                
        // Detach our existing connection.
        unbindService(mConnection)
        unbindService(mSecondaryConnection)
        mKillButton setEnabled false
        mIsBound = false
        mCallbackText setText "Unbinding."
      }
    }
  }

  private val mKillListener = new OnClickListener {
    def onClick(v: View) {
      // To kill the process hosting our service, we need to know its
      // PID.  Conveniently our service has a call that will return
      // to us that information.
      if (mSecondaryService != null) {
        try {
          val pid = mSecondaryService.getPid()
          // Note that, though this API allows us to request to
          // kill any process based on its PID, the kernel will
          // still impose standard restrictions on which PIDs you
          // are actually able to kill.  Typically this means only
          // the process running your application and any additional
          // processes created by that app as shown here; packages
          // sharing a common UID will also be able to kill each
          // other's processes.
          Process.killProcess(pid)
          mCallbackText setText "Killed service process."
        } catch {
          case ex: RemoteException =>
            // Recover gracefully from the process hosting the
            // server dying.
            // Just for purposes of the sample, put up a notification.
            Toast.makeText(RemoteServiceBinding.this,
                            R.string.remote_call_failed,
                            Toast.LENGTH_SHORT).show()
        }
      }
    }
  }
    
  // ----------------------------------------------------------------------
  // Code showing how to deal with callbacks.
  // ----------------------------------------------------------------------
    
  /**
   * This implementation is used to receive callbacks from the remote
   * service.
   */
  private val mCallback = new IRemoteServiceCallback.Stub {
    /**
     * This is called by the remote service regularly to tell us about
     * new values.  Note that IPC calls are dispatched through a thread
     * pool running in each process, so the code executing here will
     * NOT be running in our main thread like most other things -- so,
     * to update the UI, we need to use a Handler to hop over there.
     */
    def valueChanged(value: Int) {
      mHandler sendMessage mHandler.obtainMessage(BUMP_MSG, value, 0)
    }
  }
    
  private val mHandler = new Handler {
    override def handleMessage(msg: Message) {
      msg.what match {
        case BUMP_MSG =>
          mCallbackText.setText("Received from service: " + msg.arg1)
        case _ =>
          super.handleMessage(msg)
      }
    }
        
  }
}
