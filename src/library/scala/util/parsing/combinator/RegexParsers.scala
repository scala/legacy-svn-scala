/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.util.parsing.combinator

import java.util.regex.Pattern
import scala.util.matching.Regex
import scala.util.parsing.input._
import scala.collection.immutable.PagedSeq

trait AvoidingLength {
  self : java.lang.CharSequence =>
  /**Generate a new CharSequence that views this one from the offset to the end.
   * This returns the same characters as subSequence(offset, length) but should be implemented to avoid the call to
   * length.
   *
   * @param offset  the first character to include in the returned chars
   * @return  the remainder of this char seq from the offset
   */
  def subSequenceFrom(offset: Int): CharSequence with AvoidingLength

  /**Decide if this charsequence is long enough to have a char at an index.
   * This potentially allows a call to length to be avoided, if we already know that the length exceeds some value.
   * It should be implemented so as to at most force into memory i elements of the charsequence.
   *
   * @param l  the index we are checking against
   * @return   true if our length exceeds i, false if our length is equal to or less than i
   */
  def isDefinedAt(i: Int): Boolean

  @deprecated("Please avoid calling length, as this may force the entire charsequence into memory")
  override def length(): Int
}

object AvoidingLength {
  implicit def wrapCharSequence(cs: CharSequence): CharSequence with AvoidingLength = cs match {
    case ps : PagedSeq[Char] => // special-case handling for PagedSeq to avoid forcing the stream
      new NestedCSAL(ps, 0) {
        override def isDefinedAt(i: Int): Boolean = ps.isDefinedAt(i + offset)
      }
    case _ => // general case for normal CharSeqence
      new NestedCSAL(cs, 0)
  }

  private class NestedCSAL(protected val cs: CharSequence, protected val offset: Int) extends CharSequence with AvoidingLength {
    def subSequenceFrom(offset: Int): CharSequence with AvoidingLength = new NestedCSAL(cs, this.offset + offset)

    def isDefinedAt(i: Int): Boolean = cs.length() > (i + offset) // fixme: this forces length

    @deprecated("Please avoid calling length, as this may force the entire charsequence into memory")
    override def length(): Int = cs.length - offset

    def charAt(index: Int): Char = cs.charAt(index + offset)

    def subSequence(start: Int, end: Int): CharSequence = cs.subSequence(start + offset, end + offset)
  }
}

import AvoidingLength._

trait RegexParsers extends Parsers {

  type Elem = Char

  protected val whiteSpace = """\s+""".r

  def skipWhitespace = whiteSpace.toString.length > 0

  protected def handleWhiteSpace(source: java.lang.CharSequence with AvoidingLength, offset: Int): Int =
    if (skipWhitespace)
      (whiteSpace findPrefixMatchOf (source.subSequenceFrom(offset))) match {
        case Some(matched) => offset + matched.end
        case None => offset
      }
    else 
      offset

  /** A parser that matches a literal string */
  implicit def literal(s: String): Parser[String] = new Parser[String] {
    def apply(in: Input) = {
      val source = in.source : java.lang.CharSequence with AvoidingLength
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      var i = 0
      var j = start
      while (i < s.length && source.isDefinedAt(j) && s.charAt(i) == source.charAt(j)) {
        i += 1
        j += 1
      }
      if (i == s.length)
        Success(source.subSequence(start, j).toString, in.drop(j - offset))
      else  {
        val found = if (!source.isDefinedAt(start)) "end of source" else "`"+source.charAt(start)+"'"
        Failure("`"+s+"' expected but "+found+" found", in.drop(start - offset))
      }
    }
  }

  /** A parser that matches a regex string */
  implicit def regex(r: Regex): Parser[String] = new Parser[String] {
    def apply(in: Input) = {
      val source = in.source : java.lang.CharSequence with AvoidingLength
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      (r findPrefixMatchOf (source.subSequenceFrom(start))) match {
        case Some(matched) =>
          Success(source.subSequence(start, start + matched.end).toString, 
                  in.drop(start + matched.end - offset))
        case None =>
          val found = if (!source.isDefinedAt(start)) "end of source" else "`"+source.charAt(start)+"'"
          Failure("string matching regex `"+r+"' expected but "+found+" found", in.drop(start - offset))
      }
    }
  }
  
  /** `positioned` decorates a parser's result with the start position of the input it consumed.
   * If whitespace is being skipped, then it is skipped before the start position is recorded.
   * 
   * @param p a `Parser` whose result conforms to `Positional`.
   * @return A parser that has the same behaviour as `p`, but which marks its result with the
   *         start position of the input it consumed after whitespace has been skipped, if it
   *         didn't already have a position.
   */
  override def positioned[T <: Positional](p: => Parser[T]): Parser[T] = {
    val pp = super.positioned(p)
    new Parser[T] {
      def apply(in: Input) = {
        val source = in.source : java.lang.CharSequence with AvoidingLength
        val offset = in.offset
        val start = handleWhiteSpace(source, offset)
        pp(in.drop (start - offset))
      }
    }
  }

  override def phrase[T](p: Parser[T]): Parser[T] =
    super.phrase(p <~ opt("""\z""".r))

  /** Parse some prefix of reader `in` with parser `p`. */
  def parse[T](p: Parser[T], in: Reader[Char]): ParseResult[T] = 
    p(in)

  /** Parse some prefix of character sequence `in` with parser `p`. */
  def parse[T](p: Parser[T], in: java.lang.CharSequence): ParseResult[T] = 
    p(new CharSequenceReader(in))
  
  /** Parse some prefix of reader `in` with parser `p`. */
  def parse[T](p: Parser[T], in: java.io.Reader): ParseResult[T] =
    p(new PagedSeqReader(PagedSeq.fromReader(in)))

  /** Parse all of reader `in` with parser `p`. */
  def parseAll[T](p: Parser[T], in: Reader[Char]): ParseResult[T] =
    parse(phrase(p), in)

  /** Parse all of reader `in` with parser `p`. */
  def parseAll[T](p: Parser[T], in: java.io.Reader): ParseResult[T] =
    parse(phrase(p), in)

  /** Parse all of character sequence `in` with parser `p`. */
  def parseAll[T](p: Parser[T], in: java.lang.CharSequence): ParseResult[T] = 
    parse(phrase(p), in)
}
