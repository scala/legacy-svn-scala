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
import android.os.{Bundle, IBinder}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, Toast}

/**
 * <p>Example of binding and unbinding to the {@link LocalService}.
 * This demonstrates the implementation of a service which the client will
 * bind to, receiving an object through which it can communicate with the service.</p>
 */
class LocalServiceBinding extends Activity {
    private var mIsBound: Boolean = _
    private var mBoundService: LocalService = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.local_service_binding)

    // Watch for button clicks.
    var button = findViewById(R.id.bind).asInstanceOf[Button]
    button setOnClickListener mBindListener
    button = findViewById(R.id.unbind).asInstanceOf[Button]
    button setOnClickListener mUnbindListener
  }

  private val mConnection = new ServiceConnection() {
    def onServiceConnected(className: ComponentName, service: IBinder) {
      // This is called when the connection with the service has been
      // established, giving us the service object we can use to
      // interact with the service.  Because we have bound to a explicit
      // service that we know is running in our own process, we can
      // cast its IBinder to a concrete class and directly access it.
      mBoundService = service.asInstanceOf[LocalService#LocalBinder].getService

      // Tell the user about this for our demo.
      Toast.makeText(LocalServiceBinding.this, R.string.local_service_connected,
                     Toast.LENGTH_SHORT).show()
    }

    def onServiceDisconnected(className: ComponentName) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      // Because it is running in our same process, we should never
      // see this happen.
      mBoundService = null
      Toast.makeText(LocalServiceBinding.this, R.string.local_service_disconnected,
                     Toast.LENGTH_SHORT).show()
    }
  }

  private val mBindListener = new OnClickListener() {
    def onClick(v: View) {
      // Establish a connection with the service.  We use an explicit
      // class name because we want a specific service implementation that
      // we know will be running in our own process (and thus won't be
      // supporting component replacement by other applications).
      bindService(
        new Intent(LocalServiceBinding.this, classOf[LocalService]),
        mConnection, Context.BIND_AUTO_CREATE)
      mIsBound = true
    }
  }

  private val mUnbindListener = new OnClickListener() {
    def onClick(v: View) {
      if (mIsBound) {
        // Detach our existing connection.
        unbindService(mConnection)
        mIsBound = false
      }
    }
  }
}

