/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.util.parsing.input

/** An object encapsulating basic character constants 
 *
 * @author Martin Odersky, Adriaan Moors
 */
object CharArrayReader {
  final val EofCh = '\032'
}

/** A character array reader reads a stream of characters (keeping track of their positions) 
 * from an array.
 *
 * @param source an array of characters
 * @param index  starting offset into the array; the first element returned will be `source(index)`
 * @param line   the line number of the first element (counting from index `0` of `source`)
 * @param column the column number of the first element (counting from index `0` of `source`)
 *
 * @author Martin Odersky, Adriaan Moors 
 */
class CharArrayReader(chars: Array[Char], index: Int) 
extends CharSequenceReader(chars, index) {

  def this(chars: Array[Char]) = this(chars, 0)

}
