/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.android.maps;

abstract class Overlay$ extends com.google.android.maps.Overlay {

    static void drawAt$(android.graphics.Canvas canvas,
                       android.graphics.drawable.Drawable drawable,
                       int x, int y, boolean shadow) {
        drawAt(canvas, drawable, x, y, shadow);
    }

}
