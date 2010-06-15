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

package com.msi.manning.binder

import android.app.Activity
import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.os.{Bundle, DeadObjectException, IBinder, RemoteException}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView, Toast}

class ActivityExample extends Activity {

  private var service: ISimpleMathService = _
  private var isBound: Boolean = _
    
  private var inputa: EditText = _
  private var inputb: EditText = _
  private var output: TextView = _
  private var addButton: Button = _
  private var subtractButton: Button = _
  private var echoButton: Button = _
    
  private val connection = new ServiceConnection() {
    def onServiceConnected(className: ComponentName, iservice: IBinder) {
      service = ISimpleMathService.Stub.asInterface(iservice)
      Toast.makeText(ActivityExample.this, "connected to Service",
                     Toast.LENGTH_SHORT).show()
      isBound = true
    }
    def onServiceDisconnected(className: ComponentName) {
      service = null
      Toast.makeText(ActivityExample.this, "disconnected from Service",
                     Toast.LENGTH_SHORT).show()
      isBound = false
    }
  }

  private def findView[V <: View](id: Int) = findViewById(id).asInstanceOf[V]

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.activity_example)

    inputa = findView(R.id.inputa)
    inputb = findView(R.id.inputb)
    output = findView(R.id.output)
    addButton = findView(R.id.add_button)
    subtractButton = findView(R.id.subtract_button)
    echoButton = findView(R.id.echo_button)

    addButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        try {
          val a = Integer.parseInt(inputa.getText.toString)
          val b = Integer.parseInt(inputb.getText.toString)
          val result = service.add(a, b)
          output setText String.valueOf(result)
        } catch {
          case e: DeadObjectException =>
            Log.e("ActivityExample", "error", e)
          case e: RemoteException =>
            Log.e("ActivityExample", "error", e)
        }
      }
    }

    subtractButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        try {
          val a = Integer.parseInt(inputa.getText.toString)
          val b = Integer.parseInt(inputb.getText.toString)
          val result = service.subtract(a, b)
          output setText String.valueOf(result)
        } catch {
          case e: DeadObjectException =>
            Log.e("ActivityExample", "error", e)
          case e: RemoteException =>
            Log.e("ActivityExample", "error", e)
        }
      }
    }

    echoButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        try {
          val result = service.echo(inputa.getText.toString +
                                    inputb.getText.toString)
          output setText result
        } catch {
          case e: DeadObjectException =>
            Log.e("ActivityExample", "error", e)
          case e: RemoteException =>
            Log.e("ActivityExample", "error", e)
        }
      }
    }
  }

  override def onStart() {
    super.onStart()

     if (!this.isBound) {
        bindService(new Intent(ActivityExample.this,
                               classOf[SimpleMathService]),
                    this.connection, Context.BIND_AUTO_CREATE)
     }
  }

  override def onPause() {
    super.onPause()
    if (isBound) {
      isBound = false
      unbindService(connection)
    }
  }
}
