/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// DO NOT EDIT, CHANGES WILL BE LOST.

package scala

import java.{ lang => jl }


final class Float extends AnyVal {
  def toByte: Byte = sys.error("stub")
  def toShort: Short = sys.error("stub")
  def toChar: Char = sys.error("stub")
  def toInt: Int = sys.error("stub")
  def toLong: Long = sys.error("stub")
  def toFloat: Float = sys.error("stub")
  def toDouble: Double = sys.error("stub")

  def unary_+ : Float = sys.error("stub")
  def unary_- : Float = sys.error("stub")

  def +(x: String): String = sys.error("stub")

  def ==(x: Byte): Boolean = sys.error("stub")
  def ==(x: Short): Boolean = sys.error("stub")
  def ==(x: Char): Boolean = sys.error("stub")
  def ==(x: Int): Boolean = sys.error("stub")
  def ==(x: Long): Boolean = sys.error("stub")
  def ==(x: Float): Boolean = sys.error("stub")
  def ==(x: Double): Boolean = sys.error("stub")

  def !=(x: Byte): Boolean = sys.error("stub")
  def !=(x: Short): Boolean = sys.error("stub")
  def !=(x: Char): Boolean = sys.error("stub")
  def !=(x: Int): Boolean = sys.error("stub")
  def !=(x: Long): Boolean = sys.error("stub")
  def !=(x: Float): Boolean = sys.error("stub")
  def !=(x: Double): Boolean = sys.error("stub")

  def <(x: Byte): Boolean = sys.error("stub")
  def <(x: Short): Boolean = sys.error("stub")
  def <(x: Char): Boolean = sys.error("stub")
  def <(x: Int): Boolean = sys.error("stub")
  def <(x: Long): Boolean = sys.error("stub")
  def <(x: Float): Boolean = sys.error("stub")
  def <(x: Double): Boolean = sys.error("stub")

  def <=(x: Byte): Boolean = sys.error("stub")
  def <=(x: Short): Boolean = sys.error("stub")
  def <=(x: Char): Boolean = sys.error("stub")
  def <=(x: Int): Boolean = sys.error("stub")
  def <=(x: Long): Boolean = sys.error("stub")
  def <=(x: Float): Boolean = sys.error("stub")
  def <=(x: Double): Boolean = sys.error("stub")

  def >(x: Byte): Boolean = sys.error("stub")
  def >(x: Short): Boolean = sys.error("stub")
  def >(x: Char): Boolean = sys.error("stub")
  def >(x: Int): Boolean = sys.error("stub")
  def >(x: Long): Boolean = sys.error("stub")
  def >(x: Float): Boolean = sys.error("stub")
  def >(x: Double): Boolean = sys.error("stub")

  def >=(x: Byte): Boolean = sys.error("stub")
  def >=(x: Short): Boolean = sys.error("stub")
  def >=(x: Char): Boolean = sys.error("stub")
  def >=(x: Int): Boolean = sys.error("stub")
  def >=(x: Long): Boolean = sys.error("stub")
  def >=(x: Float): Boolean = sys.error("stub")
  def >=(x: Double): Boolean = sys.error("stub")

  def +(x: Byte): Float = sys.error("stub")
  def +(x: Short): Float = sys.error("stub")
  def +(x: Char): Float = sys.error("stub")
  def +(x: Int): Float = sys.error("stub")
  def +(x: Long): Float = sys.error("stub")
  def +(x: Float): Float = sys.error("stub")
  def +(x: Double): Double = sys.error("stub")

  def -(x: Byte): Float = sys.error("stub")
  def -(x: Short): Float = sys.error("stub")
  def -(x: Char): Float = sys.error("stub")
  def -(x: Int): Float = sys.error("stub")
  def -(x: Long): Float = sys.error("stub")
  def -(x: Float): Float = sys.error("stub")
  def -(x: Double): Double = sys.error("stub")

  def *(x: Byte): Float = sys.error("stub")
  def *(x: Short): Float = sys.error("stub")
  def *(x: Char): Float = sys.error("stub")
  def *(x: Int): Float = sys.error("stub")
  def *(x: Long): Float = sys.error("stub")
  def *(x: Float): Float = sys.error("stub")
  def *(x: Double): Double = sys.error("stub")

  def /(x: Byte): Float = sys.error("stub")
  def /(x: Short): Float = sys.error("stub")
  def /(x: Char): Float = sys.error("stub")
  def /(x: Int): Float = sys.error("stub")
  def /(x: Long): Float = sys.error("stub")
  def /(x: Float): Float = sys.error("stub")
  def /(x: Double): Double = sys.error("stub")

  def %(x: Byte): Float = sys.error("stub")
  def %(x: Short): Float = sys.error("stub")
  def %(x: Char): Float = sys.error("stub")
  def %(x: Int): Float = sys.error("stub")
  def %(x: Long): Float = sys.error("stub")
  def %(x: Float): Float = sys.error("stub")
  def %(x: Double): Double = sys.error("stub")

}


object Float extends AnyValCompanion {
  /** The smallest positive value greater than 0.0f.*/
  final val MinPositiveValue = jl.Float.MIN_VALUE
  final val NaN              = jl.Float.NaN
  final val PositiveInfinity = jl.Float.POSITIVE_INFINITY
  final val NegativeInfinity = jl.Float.NEGATIVE_INFINITY

  @deprecated("use Float.MinPositiveValue instead")
  final val Epsilon  = MinPositiveValue

  /** The negative number with the greatest (finite) absolute value which is representable
   *  by a Float.  Note that it differs from [[java.lang.Float.MIN_VALUE]], which
   *  is the smallest positive value representable by a Float.  In Scala that number
   *  is called Float.MinPositiveValue.
   */
  final val MinValue = -jl.Float.MAX_VALUE

  /** The largest finite positive number representable as a Float. */
  final val MaxValue = jl.Float.MAX_VALUE

  def box(x: Float): jl.Float = jl.Float.valueOf(x)
  def unbox(x: jl.Object): Float = x.asInstanceOf[jl.Float].floatValue()
  override def toString = "object scala.Float"
}
