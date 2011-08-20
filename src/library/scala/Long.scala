/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// DO NOT EDIT, CHANGES WILL BE LOST.

package scala

/** `Long`, a 64-bit signed integer (equivalent to Java's long primitive type) is a member 
 *  of the value classes, those whose instances are not represented as objects by the 
 *  underlying host system.
 *
 *  There is an implicit conversion from [[scala.Long]] => [[scala.runtime.RichLong]]
 *  which provides useful non-primitive operations.
 */
final class Long extends AnyVal {
  def toByte: Byte = sys.error("stub")
  def toShort: Short = sys.error("stub")
  def toChar: Char = sys.error("stub")
  def toInt: Int = sys.error("stub")
  def toLong: Long = sys.error("stub")
  def toFloat: Float = sys.error("stub")
  def toDouble: Double = sys.error("stub")

  /**
 * @return the bitwise negation of this value
 * @example {{{
 * ~5 == -6
 * // in binary: ~00000101 == 
 * //             11111010
 * }}}
 */
  def unary_~ : Long = sys.error("stub")
  /**
 * @return this value, unmodified
 */
  def unary_+ : Long = sys.error("stub")
  /**
 * @return the negation of this value
 */
  def unary_- : Long = sys.error("stub")

  def +(x: String): String = sys.error("stub")

  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the new right bits with zeroes.
  * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
  */
  def <<(x: Int): Long = sys.error("stub")
  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the new right bits with zeroes.
  * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
  */
  def <<(x: Long): Long = sys.error("stub")
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
  def >>>(x: Int): Long = sys.error("stub")
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
  def >>>(x: Long): Long = sys.error("stub")
  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the right bits with the same value as the left-most bit of this.
  *         The effect of this is to retain the sign of the value.
  * @example {{{ -21 >>> 3 == -3 
  * // in binary: 11111111 11111111 11111111 11101011 >>> 3 == 
  * //            11111111 11111111 11111111 11111101
  * }}}
  */
  def >>(x: Int): Long = sys.error("stub")
  /**
  * @return this value bit-shifted left by the specified number of bits,
  *         filling in the right bits with the same value as the left-most bit of this.
  *         The effect of this is to retain the sign of the value.
  * @example {{{ -21 >>> 3 == -3 
  * // in binary: 11111111 11111111 11111111 11101011 >>> 3 == 
  * //            11111111 11111111 11111111 11111101
  * }}}
  */
  def >>(x: Long): Long = sys.error("stub")

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
  * @example {{{
  * (0xf0 | 0xaa) == 0xfa
  * // in binary: ( 11110000 
  * //            | 10101010) == 
  * //              11111010
  * }}}
  */
  def |(x: Byte): Long = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  * @example {{{
  * (0xf0 | 0xaa) == 0xfa
  * // in binary: ( 11110000 
  * //            | 10101010) == 
  * //              11111010
  * }}}
  */
  def |(x: Short): Long = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  * @example {{{
  * (0xf0 | 0xaa) == 0xfa
  * // in binary: ( 11110000 
  * //            | 10101010) == 
  * //              11111010
  * }}}
  */
  def |(x: Char): Long = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  * @example {{{
  * (0xf0 | 0xaa) == 0xfa
  * // in binary: ( 11110000 
  * //            | 10101010) == 
  * //              11111010
  * }}}
  */
  def |(x: Int): Long = sys.error("stub")
  /**
  * @return the bitwise OR of this value with the provided value
  * @example {{{
  * (0xf0 | 0xaa) == 0xfa
  * // in binary: ( 11110000 
  * //            | 10101010) == 
  * //              11111010
  * }}}
  */
  def |(x: Long): Long = sys.error("stub")

  /**
  * @return the bitwise AND of this value with the provided value
  * @example {{{
  * (0xf0 & 0xaa) == 0xa0
  * // in binary: ( 11110000 
  * //            & 10101010) == 
  * //              10100000
  * }}}
  */
  def &(x: Byte): Long = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  * @example {{{
  * (0xf0 & 0xaa) == 0xa0
  * // in binary: ( 11110000 
  * //            & 10101010) == 
  * //              10100000
  * }}}
  */
  def &(x: Short): Long = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  * @example {{{
  * (0xf0 & 0xaa) == 0xa0
  * // in binary: ( 11110000 
  * //            & 10101010) == 
  * //              10100000
  * }}}
  */
  def &(x: Char): Long = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  * @example {{{
  * (0xf0 & 0xaa) == 0xa0
  * // in binary: ( 11110000 
  * //            & 10101010) == 
  * //              10100000
  * }}}
  */
  def &(x: Int): Long = sys.error("stub")
  /**
  * @return the bitwise AND of this value with the provided value
  * @example {{{
  * (0xf0 & 0xaa) == 0xa0
  * // in binary: ( 11110000 
  * //            & 10101010) == 
  * //              10100000
  * }}}
  */
  def &(x: Long): Long = sys.error("stub")

  /**
  * @return the bitwise XOR of this value with the provided value
  * @example {{{
  * (0xf0 ^ 0xaa) == 0x5a
  * // in binary: ( 11110000 
  * //            ^ 10101010) == 
  * //              01011010
  * }}}
  */
  def ^(x: Byte): Long = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  * @example {{{
  * (0xf0 ^ 0xaa) == 0x5a
  * // in binary: ( 11110000 
  * //            ^ 10101010) == 
  * //              01011010
  * }}}
  */
  def ^(x: Short): Long = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  * @example {{{
  * (0xf0 ^ 0xaa) == 0x5a
  * // in binary: ( 11110000 
  * //            ^ 10101010) == 
  * //              01011010
  * }}}
  */
  def ^(x: Char): Long = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  * @example {{{
  * (0xf0 ^ 0xaa) == 0x5a
  * // in binary: ( 11110000 
  * //            ^ 10101010) == 
  * //              01011010
  * }}}
  */
  def ^(x: Int): Long = sys.error("stub")
  /**
  * @return the bitwise XOR of this value with the provided value
  * @example {{{
  * (0xf0 ^ 0xaa) == 0x5a
  * // in binary: ( 11110000 
  * //            ^ 10101010) == 
  * //              01011010
  * }}}
  */
  def ^(x: Long): Long = sys.error("stub")

  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Byte): Long = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Short): Long = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Char): Long = sys.error("stub")
  /**
  * @return the sum of this value with the provided value
  */
  def +(x: Int): Long = sys.error("stub")
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
  def -(x: Byte): Long = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Short): Long = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Char): Long = sys.error("stub")
  /**
  * @return the difference of this value with the provided value
  */
  def -(x: Int): Long = sys.error("stub")
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
  def *(x: Byte): Long = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Short): Long = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Char): Long = sys.error("stub")
  /**
  * @return the product of this value and the provided value
  */
  def *(x: Int): Long = sys.error("stub")
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
  def /(x: Byte): Long = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Short): Long = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Char): Long = sys.error("stub")
  /**
  * @return the quotient of this value and the provided value
  */
  def /(x: Int): Long = sys.error("stub")
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
  def %(x: Byte): Long = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Short): Long = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Char): Long = sys.error("stub")
  /**
  * @return the remainder of whole number division of this value by the provided value
  */
  def %(x: Int): Long = sys.error("stub")
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

  def getClass(): Class[Long] = sys.error("stub")
}

object Long extends AnyValCompanion {
  /** The smallest value representable as a Long.
   */
  final val MinValue = java.lang.Long.MIN_VALUE

  /** The largest value representable as a Long.
   */
  final val MaxValue = java.lang.Long.MAX_VALUE

  /** Transform a value type into a boxed reference type.
   *
   *  @param  x   the Long to be boxed
   *  @return     a java.lang.Long offering `x` as its underlying value.
   */
  def box(x: Long): java.lang.Long = java.lang.Long.valueOf(x)

  /** Transform a boxed type into a value type.  Note that this
   *  method is not typesafe: it accepts any Object, but will throw
   *  an exception if the argument is not a java.lang.Long.
   *
   *  @param  x   the java.lang.Long to be unboxed.
   *  @throws     ClassCastException  if the argument is not a java.lang.Long
   *  @return     the Long resulting from calling longValue() on `x`
   */
  def unbox(x: java.lang.Object): Long = x.asInstanceOf[java.lang.Long].longValue()

  /** The String representation of the scala.Long companion object.
   */
  override def toString = "object scala.Long"
}

