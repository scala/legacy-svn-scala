/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.graphics

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R

import android.app.{Activity, Application}
import android.content.Context
import android.os.Bundle
import android.graphics.{BitmapFactory, Bitmap}
import android.graphics.Canvas
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.widget.{LinearLayout, TextView, ScrollView}
import android.view.{LayoutInflater, View, ViewGroup}
import android.content.Context
import android.content.pm.{ApplicationInfo, PackageManager}
import android.util.{DisplayMetrics, Log}

/**
 * This activity demonstrates various ways density can cause the scaling of
 * bitmaps and drawables.
 */
class DensityActivity extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val li =
      getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    this.setTitle(R.string.density_title)
    val root = new LinearLayout(this)
    root setOrientation LinearLayout.VERTICAL

    var layout = new LinearLayout(this)
    addBitmapDrawable(layout, R.drawable.logo120dpi, true)
    addBitmapDrawable(layout, R.drawable.logo160dpi, true)
    addBitmapDrawable(layout, R.drawable.logo240dpi, true)
    addLabelToRoot(root, "Prescaled bitmap in drawable")
    addChildToRoot(root, layout)

    layout = new LinearLayout(this)
    addBitmapDrawable(layout, R.drawable.logo120dpi, false)
    addBitmapDrawable(layout, R.drawable.logo160dpi, false)
    addBitmapDrawable(layout, R.drawable.logo240dpi, false)
    addLabelToRoot(root, "Autoscaled bitmap in drawable")
    addChildToRoot(root, layout)

    layout = new LinearLayout(this)
    addResourceDrawable(layout, R.drawable.logo120dpi)
    addResourceDrawable(layout, R.drawable.logo160dpi)
    addResourceDrawable(layout, R.drawable.logo240dpi)
    addLabelToRoot(root, "Prescaled resource drawable")
    addChildToRoot(root, layout)

    layout = li.inflate(R.layout.density_image_views, null).asInstanceOf[LinearLayout]
    addLabelToRoot(root, "Inflated layout")
    addChildToRoot(root, layout)

    layout = li.inflate(R.layout.density_styled_image_views, null).asInstanceOf[LinearLayout]
    addLabelToRoot(root, "Inflated styled layout")
    addChildToRoot(root, layout)

    layout = new LinearLayout(this)
    addCanvasBitmap(layout, R.drawable.logo120dpi, true)
    addCanvasBitmap(layout, R.drawable.logo160dpi, true)
    addCanvasBitmap(layout, R.drawable.logo240dpi, true)
    addLabelToRoot(root, "Prescaled bitmap")
    addChildToRoot(root, layout)

    layout = new LinearLayout(this)
    addCanvasBitmap(layout, R.drawable.logo120dpi, false)
    addCanvasBitmap(layout, R.drawable.logo160dpi, false)
    addCanvasBitmap(layout, R.drawable.logo240dpi, false)
    addLabelToRoot(root, "Autoscaled bitmap")
    addChildToRoot(root, layout)

    layout = new LinearLayout(this)
    addResourceDrawable(layout, R.drawable.logonodpi120)
    addResourceDrawable(layout, R.drawable.logonodpi160)
    addResourceDrawable(layout, R.drawable.logonodpi240)
    addLabelToRoot(root, "No-dpi resource drawable")
    addChildToRoot(root, layout)

    layout = new LinearLayout(this)
    addNinePatchResourceDrawable(layout, R.drawable.smlnpatch120dpi)
    addNinePatchResourceDrawable(layout, R.drawable.smlnpatch160dpi)
    addNinePatchResourceDrawable(layout, R.drawable.smlnpatch240dpi)
    addLabelToRoot(root, "Prescaled 9-patch resource drawable")
    addChildToRoot(root, layout)

    setContentView(scrollWrap(root))
  }

  private def scrollWrap(view: View): View = {
    val scroller = new ScrollView(this)
    scroller.addView(view,
      new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                 ViewGroup.LayoutParams.FILL_PARENT))
    scroller
  }

  private def addLabelToRoot(root: LinearLayout, text: String) {
    val label = new TextView(this)
    label setText text
    root.addView(label,
      new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT))
    }

  private def addChildToRoot(root: LinearLayout, layout: LinearLayout) {
    root.addView(layout,
      new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT))
    }

  private def addBitmapDrawable(layout: LinearLayout, resource: Int, scale: Boolean) {
    val bitmap = loadAndPrintDpi(resource, scale)
    val view = new View(this)

    val d = new BitmapDrawable(getResources, bitmap)
    if (!scale) d.setTargetDensity(getResources.getDisplayMetrics)
    view setBackgroundDrawable d

    view.setLayoutParams(new LinearLayout.LayoutParams(d.getIntrinsicWidth,
                d.getIntrinsicHeight))
    layout addView view 
  }

  private def addResourceDrawable(layout: LinearLayout, resource: Int) {
    val view = new View(this)

    val d = getResources.getDrawable(resource)
    view setBackgroundDrawable d

    view setLayoutParams new ViewGroup.LayoutParams(d.getIntrinsicWidth,
                d.getIntrinsicHeight)
    layout addView view
  }

  private def addCanvasBitmap(layout: LinearLayout, resource: Int, scale: Boolean) {
    val bitmap = loadAndPrintDpi(resource, scale)
    val view = new ScaledBitmapView(this, bitmap)

    view setLayoutParams new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    layout addView view
  }

  private def addNinePatchResourceDrawable(layout: LinearLayout, resource: Int) {
    val view = new View(this)

    val d = getResources().getDrawable(resource)
    view setBackgroundDrawable d

    Log.i("foo", "9-patch #" + Integer.toHexString(resource)
          + " w=" + d.getIntrinsicWidth + " h=" + d.getIntrinsicHeight)
    view setLayoutParams new LinearLayout.LayoutParams(
                d.getIntrinsicWidth*2, d.getIntrinsicHeight*2)
    layout addView view
  }

  private def loadAndPrintDpi(id: Int, scale: Boolean): Bitmap =
    if (scale) {
      BitmapFactory.decodeResource(getResources, id)
    } else {
      val opts = new BitmapFactory.Options()
      opts.inScaled = false
      BitmapFactory.decodeResource(getResources, id, opts)
    }

  private class ScaledBitmapView(context: Context, bitmap: Bitmap)
      extends View(context) {

    override protected def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
      val metrics = getResources.getDisplayMetrics
      setMeasuredDimension(
        bitmap getScaledWidth metrics,
        bitmap getScaledHeight metrics)
    }

    override protected def onDraw(canvas: Canvas) {
      super.onDraw(canvas)

      canvas.drawBitmap(bitmap, 0.0f, 0.0f, null)
    }
  }
}
