package com.example.helloactivity

import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.widget.TextView

class HelloActivity extends Activity {

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val tv = new TextView(this)
    tv setText "Scala on Android"
    setContentView(tv)
  }

}
