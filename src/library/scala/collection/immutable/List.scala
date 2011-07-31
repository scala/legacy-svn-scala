/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.collection
package immutable

import generic._
import mutable.{Builder, ListBuffer}
import annotation.tailrec

/** A class for immutable linked lists representing ordered collections
 *  of elements of type. 
 *  
 *  This class comes with two implementing case classes `scala.Nil` 
 *  and `scala.::` that implement the abstract members `isEmpty`, 
 *  `head` and `tail`.
 *
 *  @author  Martin Odersky and others
 *  @version 2.8
 *  @since   1.0
 *
 *  @tparam  A    the type of the list's elements
 *
 *  @define Coll List
 *  @define coll list
 *  @define thatinfo the class of the returned collection. In the standard library configuration,
 *    `That` is always `List[B]` because an implicit of type `CanBuildFrom[List, B, That]`
 *    is defined in object `List`.
 *  @define bfinfo an implicit value of class `CanBuildFrom` which determines the
 *    result class `That` from the current representation type `Repr`
 *    and the new element type `B`. This is usually the `canBuildFrom` value
 *    defined in object `List`.
 *  @define orderDependent 
 *  @define orderDependentFold
 *  @define mayNotTerminateInf
 *  @define willNotTerminateInf
 */
sealed abstract class List[+A] extends LinearSeq[A] 
                                  with Product 
                                  with GenericTraversableTemplate[A, List]
                                  with LinearSeqOptimized[A, List[A]] {
  override def companion: GenericCompanion[List] = List

  import scala.collection.{Iterable, Traversable, Seq, IndexedSeq}

  def isEmpty: Boolean
  def head: A
  def tail: List[A]

  // New methods in List

  /** Adds an element at the beginning of this list.
   *  @param x the element to prepend.
   *  @return  a list which contains `x` as first element and
   *           which continues with this list.
   *  @example `1 :: List(2, 3) = List(2, 3).::(1) = List(1, 2, 3)`
   *  @usecase def ::(x: A): List[A]
   */
  def ::[B >: A] (x: B): List[B] =
    new scala.collection.immutable.::(x, this)

  /** Adds the elements of a given list in front of this list.
   *  @param prefix  The list elements to prepend.
   *  @return a list resulting from the concatenation of the given
   *    list `prefix` and this list. 
   *  @example `List(1, 2) ::: List(3, 4) = List(3, 4).:::(List(1, 2)) = List(1, 2, 3, 4)`
   *  @usecase def :::(prefix: List[A]): List[A]
   */
  def :::[B >: A](prefix: List[B]): List[B] =
    if (isEmpty) prefix
    else if (prefix.isEmpty) this
    else (new ListBuffer[B] ++= prefix).prependToList(this)

  /** Adds the elements of a given list in reverse order in front of this list.
   *  `xs reverse_::: ys` is equivalent to
   *  `xs.reverse ::: ys` but is more efficient.
   *
   *  @param prefix the prefix to reverse and then prepend
   *  @return       the concatenation of the reversed prefix and the current list.
   *  @usecase def reverse_:::(prefix: List[A]): List[A]
   */
  def reverse_:::[B >: A](prefix: List[B]): List[B] = {
    var these: List[B] = this
    var pres = prefix
    while (!pres.isEmpty) {
      these = pres.head :: these
      pres = pres.tail
    }
    these
  }

  /** Builds a new list by applying a function to all elements of this list.
   *  Like `xs map f`, but returns `xs` unchanged if function
   *  `f` maps all elements to themselves (wrt eq).
   * 
   *  @param f      the function to apply to each element.
   *  @tparam B     the element type of the returned collection.
   *  @return       a list resulting from applying the given function
   *                `f` to each element of this list and collecting the results.
   *  @usecase def mapConserve(f: A => A): List[A] 
   */
  def mapConserve[B >: A <: AnyRef](f: A => B): List[B] = {
    @tailrec
    def loop(mapped: ListBuffer[B], unchanged: List[A], pending: List[A]): List[B] =
      if (pending.isEmpty) {
        if (mapped eq null) unchanged
        else mapped.prependToList(unchanged)
      }
      else {
        val head0 = pending.head
        val head1 = f(head0)

        if (head1 eq head0.asInstanceOf[AnyRef])
          loop(mapped, unchanged, pending.tail)
        else {
          val b = if (mapped eq null) new ListBuffer[B] else mapped
          var xc = unchanged
          while (xc ne pending) {
            b += xc.head
            xc = xc.tail
          }
          b += head1
          val tail0 = pending.tail
          loop(b, tail0, tail0)
        }
      }
    loop(null, this, this)
  }

  // Overridden methods from IterableLike and SeqLike or overloaded variants of such methods
  
  override def ++[B >: A, That](that: GenTraversableOnce[B])(implicit bf: CanBuildFrom[List[A], B, That]): That = {
    val b = bf(this)
    if (b.isInstanceOf[ListBuffer[_]]) (this ::: that.seq.toList).asInstanceOf[That]
    else super.++(that)
  }

  override def +:[B >: A, That](elem: B)(implicit bf: CanBuildFrom[List[A], B, That]): That = bf match {
    case _: List.GenericCanBuildFrom[_] => (elem :: this).asInstanceOf[That]
    case _ => super.+:(elem)(bf)
  }

  override def toList: List[A] = this

  override def take(n: Int): List[A] = {
    val b = new ListBuffer[A]
    var i = 0
    var these = this
    while (!these.isEmpty && i < n) {
      i += 1
      b += these.head
      these = these.tail
    }
    if (these.isEmpty) this
    else b.toList
  }

  override def drop(n: Int): List[A] = {
    var these = this
    var count = n
    while (!these.isEmpty && count > 0) {
      these = these.tail
      count -= 1
    }
    these
  }
  
  override def slice(from: Int, until: Int): List[A] = {
    val lo = math.max(from, 0)
    if (until <= lo || isEmpty) Nil
    else this drop lo take (until - lo)
  }

  override def takeRight(n: Int): List[A] = {
    @tailrec
    def loop(lead: List[A], lag: List[A]): List[A] = lead match {
      case Nil => lag
      case _ :: tail => loop(tail, lag.tail)
    }
    loop(drop(n), this)
  }
  
  // dropRight is inherited from LinearSeq

  override def splitAt(n: Int): (List[A], List[A]) = {
    val b = new ListBuffer[A]
    var i = 0
    var these = this
    while (!these.isEmpty && i < n) {
      i += 1
      b += these.head
      these = these.tail
    }
    (b.toList, these)
  }

  override def takeWhile(p: A => Boolean): List[A] = {
    val b = new ListBuffer[A]
    var these = this
    while (!these.isEmpty && p(these.head)) {
      b += these.head
      these = these.tail
    }
    b.toList
  }

  override def dropWhile(p: A => Boolean): List[A] = {
    @tailrec
    def loop(xs: List[A]): List[A] =
      if (xs.isEmpty || !p(xs.head)) xs
      else loop(xs.tail)

    loop(this)
  }

  override def span(p: A => Boolean): (List[A], List[A]) = {
    val b = new ListBuffer[A]
    var these = this
    while (!these.isEmpty && p(these.head)) {
      b += these.head
      these = these.tail
    }
    (b.toList, these)
  }

  override def reverse: List[A] = {
    var result: List[A] = Nil
    var these = this
    while (!these.isEmpty) {
      result = these.head :: result
      these = these.tail
    }
    result
  }

  override def stringPrefix = "List"

  override def toStream : Stream[A] =
    if (isEmpty) Stream.Empty
    else new Stream.Cons(head, tail.toStream)
}

/** The empty list.
 *
 *  @author  Martin Odersky
 *  @version 1.0, 15/07/2003
 *  @since   2.8
 */
@SerialVersionUID(0 - 8256821097970055419L)
case object Nil extends List[Nothing] {
  override def isEmpty = true
  override def head: Nothing =
    throw new NoSuchElementException("head of empty list")
  override def tail: List[Nothing] =
    throw new UnsupportedOperationException("tail of empty list")
  // Removal of equals method here might lead to an infinite recursion similar to IntMap.equals.
  override def equals(that: Any) = that match {
    case that1: collection.GenSeq[_] => that1.isEmpty
    case _ => false
  }
}

/** A non empty list characterized by a head and a tail.
 *  @param hd   the first element of the list
 *  @param tl   the list containing the remaining elements of this list after the first one.
 *  @tparam B   the type of the list elements.
 *  @author  Martin Odersky
 *  @version 1.0, 15/07/2003
 *  @since   2.8
 */
@SerialVersionUID(0L - 8476791151983527571L)
final case class ::[B](private var hd: B, private[scala] var tl: List[B]) extends List[B] {
  override def head : B = hd
  override def tail : List[B] = tl
  override def isEmpty: Boolean = false

  import java.io._

  private def writeObject(out: ObjectOutputStream) {
    var xs: List[B] = this
    while (!xs.isEmpty) { out.writeObject(xs.head); xs = xs.tail }
    out.writeObject(ListSerializeEnd)
  }

  private def readObject(in: ObjectInputStream) {
    hd = in.readObject.asInstanceOf[B]
    assert(hd != ListSerializeEnd)
    var current: ::[B] = this
    while (true) in.readObject match {
      case ListSerializeEnd =>
        current.tl = Nil
        return
      case a : Any =>
        val list : ::[B] = new ::(a.asInstanceOf[B], Nil)
        current.tl = list
        current = list
    }
  }
}

/** $factoryInfo
 *  @define coll list
 *  @define Coll List
 */
object List extends SeqFactory[List] {

  /** $genericCanBuildFromInfo */
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, List[A]] =
    ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]

  def newBuilder[A]: Builder[A, List[A]] = new ListBuffer[A]

  override def empty[A]: List[A] = Nil

  override def apply[A](xs: A*): List[A] = xs.toList
}

/** Only used for list serialization */
@SerialVersionUID(0L - 8476791151975527571L)
private[scala] case object ListSerializeEnd
