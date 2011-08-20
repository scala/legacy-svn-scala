/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// DO NOT EDIT, CHANGES WILL BE LOST.

package scala

/** `Char` is a member of the value classes, those whose instances are
 *  not represented as objects by the underlying host system.
 *
 *  There is an implicit conversion from [[scala.Char]] => [[scala.runtime.RichChar]]
 *  which provides useful non-primitive operations.
 */
final class Char extends AnyVal {
  def toByte: Byte = sys.error("stub")
  def toShort: Short = sys.error("stub")
  def toChar: Char = sys.error("stub")
  def toInt: Int = sys.error("stub")
  def toLong: Long = sys.error("stub")
  def toFloat: Float = sys.error("stub")
  def toDouble: Double = sys.error("stub")

  def unary_+ : Int = sys.error("stub")
  def unary_- : Int = sys.error("stub")
  def unary_~ : Int = sys.error("stub")

  def +(x: String): String = sys.error("stub")

  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the new right bits with zeroes.
  * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
  */
  def <<(x: Int): Int = sys.error("stub")
  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the new right bits with zeroes.
  * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
  */
  def <<(x: Long): Int = sys.error("stub")
  /**
  * @return this value bit-shifted right by the specified number of bits,
  *         filling the new left bits with zeroes. 
  * @example {{{ 21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010 }}}
  * @example {{{
  * -21 >>> 3 == 536870909 
  * // in binary: 11111111 11111111 11111111 11101011 >>> 3 == 
  * //            00011111 11111111 11111111 11111101
  * }}}
  */
  def >>>(x: Int): Int = sys.error("stub")
  /**
  * @return this value bit-shifted right by the specified number of bits,
  *         filling the new left bits with zeroes. 
  * @example {{{ 21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010 }}}
  * @example {{{
  * -21 >>> 3 == 536870909 
  * // in binary: 11111111 11111111 11111111 11101011 >>> 3 == 
  * //            00011111 11111111 11111111 11111101
  * }}}
  */
  def >>>(x: Long): Int = sys.error("stub")
  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the right bits with the same value as the left-most bit of this.
  *         The effect of this is to retain the sign of the value.
  * @example {{{ -21 >>> 3 == -3 
  * // in binary: 11111111 11111111 11111111 11101011 >>> 3 == 
  * //            11111111 11111111 11111111 11111101
  * }}}
  */
  def >>(x: Int): Int = sys.error("stub")
  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the right bits with the same value as the left-most bit of this.
  *         The effect of this is to retain the sign of the value.
  * @example {{{ -21 >>> 3 == -3 
  * // in binary: 11111111 11111111 11111111 11101011 >>> 3 == 
  * //            11111111 11111111 11111111 11111101
  * }}}
  */
  def >>(x: Long): Int = sys.error("stub")

  /**
  * @return true if this value is equal to the provided value, false otherwise
  */
  def ==(x: Byte): Boolean = sys.error("stub")
  /**
  * @return true if this value is equal to the provided value, false otherwise
  */
  def ==(x: Short): Boolean = sys.error("stub")
  /**
  * @return true if this value is equal to the provided value, false otherwise
  */
  def ==(x: Char): Boolean = sys.error("stub")
  /**
  * @return true if this value is equal to the provided value, false otherwise
  */
  def ==(x: Int): Boolean = sys.error("stub")
  /**
  * @return true if this value is equal to the provided value, false otherwise
  */
  def ==(x: Long): Boolean = sys.error("stub")
  /**
  * @return true if this value is equal to the provided value, false otherwise
  */
  def ==(x: Float): Boolean = sys.error("stub")
  /**
  * @return true if this value is equal to the provided value, false otherwise
  */
  def ==(x: Double): Boolean = sys.error("stub")

  /**
  * @return true if this value is not equal to the provided value, false otherwise
  */
  def !=(x: Byte): Boolean = sys.error("stub")
  /**
  * @return true if this value is not equal to the provided value, false otherwise
  */
  def !=(x: Short): Boolean = sys.error("stub")
  /**
  * @return true if this value is not equal to the provided value, false otherwise
  */
  def !=(x: Char): Boolean = sys.error("stub")
  /**
  * @return true if this value is not equal to the provided value, false otherwise
  */
  def !=(x: Int): Boolean = sys.error("stub")
  /**
  * @return true if this value is not equal to the provided value, false otherwise
  */
  def !=(x: Long): Boolean = sys.error("stub")
  /**
  * @return true if this value is not equal to the provided value, false otherwise
  */
  def !=(x: Float): Boolean = sys.error("stub")
  /**
  * @return true if this value is not equal to the provided value, false otherwise
  */
  def !=(x: Double): Boolean = sys.error("stub")

  /**
  * @return true if this value is less than the provided value, false otherwise
  */
  def <(x: Byte): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than the provided value, false otherwise
  */
  def <(x: Short): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than the provided value, false otherwise
  */
  def <(x: Char): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than the provided value, false otherwise
  */
  def <(x: Int): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than the provided value, false otherwise
  */
  def <(x: Long): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than the provided value, false otherwise
  */
  def <(x: Float): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than the provided value, false otherwise
  */
  def <(x: Double): Boolean = sys.error("stub")

  /**
  * @return true if this value is less than or equal to the provide value, false otherwise
  */
  def <=(x: Byte): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than or equal to the provide value, false otherwise
  */
  def <=(x: Short): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than or equal to the provide value, false otherwise
  */
  def <=(x: Char): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than or equal to the provide value, false otherwise
  */
  def <=(x: Int): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than or equal to the provide value, false otherwise
  */
  def <=(x: Long): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than or equal to the provide value, false otherwise
  */
  def <=(x: Float): Boolean = sys.error("stub")
  /**
  * @return true if this value is less than or equal to the provide value, false otherwise
  */
  def <=(x: Double): Boolean = sys.error("stub")

  /**
  * @return true if this value is greater than the provided value, false otherwise
  */
  def >(x: Byte): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than the provided value, false otherwise
  */
  def >(x: Short): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than the provided value, false otherwise
  */
  def >(x: Char): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than the provided value, false otherwise
  */
  def >(x: Int): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than the provided value, false otherwise
  */
  def >(x: Long): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than the provided value, false otherwise
  */
  def >(x: Float): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than the provided value, false otherwise
  */
  def >(x: Double): Boolean = sys.error("stub")

  /**
  * @return true if this value is greater than or equal to the provided value, false otherwise
  */
  def >=(x: Byte): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than or equal to the provided value, false otherwise
  */
  def >=(x: Short): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than or equal to the provided value, false otherwise
  */
  def >=(x: Char): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than or equal to the provided value, false otherwise
  */
  def >=(x: Int): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than or equal to the provided value, false otherwise
  */
  def >=(x: Long): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than or equal to the provided value, false otherwise
  */
  def >=(x: Float): Boolean = sys.error("stub")
  /**
  * @return true if this value is greater than or equal to the provided value, false otherwise
  */
  def >=(x: Double): Boolean = sys.error("stub")

  /**
  * @return the bitwise OR of this value with the provided value
  */
  def |(x: Byte): Int = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  */
  def |(x: Short): Int = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  */
  def |(x: Char): Int = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  */
  def |(x: Int): Int = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  */
  def |(x: Long): Long = sys.error("stub")

  /**
  * @return the bitwise AND of this value with the provided value
  */
  def &(x: Byte): Int = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  */
  def &(x: Short): Int = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  */
  def &(x: Char): Int = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  */
  def &(x: Int): Int = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  */
  def &(x: Long): Long = sys.error("stub")

  /**
  * @return the bitwise XOR of this value with the provided value
  */
  def ^(x: Byte): Int = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  */
  def ^(x: Short): Int = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  */
  def ^(x: Char): Int = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  */
  def ^(x: Int): Int = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  */
  def ^(x: Long): Long = sys.error("stub")

  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Byte): Int = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Short): Int = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Char): Int = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Int): Int = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Long): Long = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Float): Float = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Double): Double = sys.error("stub")

  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Byte): Int = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Short): Int = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Char): Int = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Int): Int = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Long): Long = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Float): Float = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Double): Double = sys.error("stub")

  /**
  * @return the product of this value and the provided value
  */
  def *(x: Byte): Int = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Short): Int = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Char): Int = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Int): Int = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Long): Long = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Float): Float = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Double): Double = sys.error("stub")

  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Byte): Int = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Short): Int = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Char): Int = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Int): Int = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Long): Long = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Float): Float = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Double): Double = sys.error("stub")

  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Byte): Int = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Short): Int = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Char): Int = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Int): Int = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Long): Long = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Float): Float = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Double): Double = sys.error("stub")

  def getClass(): Class[Char] = sys.error("stub")
}

object Char extends AnyValCompanion {
  /** The smallest value representable as a Char.
   */
  final val MinValue = java.lang.Character.MIN_VALUE

  /** The largest value representable as a Char.
   */
  final val MaxValue = java.lang.Character.MAX_VALUE

  /** Transform a value type into a boxed reference type.
   *
   *  @param  x   the Char to be boxed
   *  @return     a java.lang.Character offering `x` as its underlying value.
   */
  def box(x: Char): java.lang.Character = java.lang.Character.valueOf(x)

  /** Transform a boxed type into a value type.  Note that this
   *  method is not typesafe: it accepts any Object, but will throw
   *  an exception if the argument is not a java.lang.Character.
   *
   *  @param  x   the java.lang.Character to be unboxed.
   *  @throws     ClassCastException  if the argument is not a java.lang.Character
   *  @return     the Char resulting from calling charValue() on `x`
   */
  def unbox(x: java.lang.Object): Char = x.asInstanceOf[java.lang.Character].charValue()

  /** The String representation of the scala.Char companion object.
   */
  override def toString = "object scala.Char"
}

