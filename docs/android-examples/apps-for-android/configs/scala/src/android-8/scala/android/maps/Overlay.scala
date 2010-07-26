/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.android.maps

abstract class Overlay extends com.google.android.maps.Overlay {

  protected object Overlay {
    def drawAt(canvas: android.graphics.Canvas,
               drawable: android.graphics.drawable.Drawable,
               x: Int, y: Int, shadow: Boolean) {
      Overlay$.drawAt$(canvas, drawable, x, y, shadow)
    }
  }

}

