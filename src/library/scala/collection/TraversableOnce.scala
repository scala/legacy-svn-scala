/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.collection

import mutable.{ Buffer, ListBuffer, ArrayBuffer }
import annotation.unchecked.{ uncheckedVariance => uV }

/** A template trait for collections which can be traversed either once only
 *  or one or more times.
 *  $traversableonceinfo
 * 
 *  @tparam A    the element type of the collection
 *
 *  @define traversableonceinfo
 *  This trait exists primarily to eliminate code duplication between
 *  `Iterator` and `Traversable`, and thus implements some of the common
 *  methods that can be implemented solely in terms of foreach without
 *  access to a `Builder`. It also includes a number of abstract methods
 *  whose implementations are provided by `Iterator`, `Traversable`, etc.
 *  It contains implementations common to `Iterators` and
 *  `Traversables`, such as folds, conversions, and other operations which
 *  traverse some or all of the elements and return a derived value.
 *  Directly subclassing `TraversableOnce` is not recommended - instead,
 *  consider declaring an `Iterator` with a `next` and `hasNext` method,
 *  creating an `Iterator` with one of the methods on the `Iterator` object,
 *  or declaring a subclass of `Traversable`.
 *
 *  @author Martin Odersky
 *  @author Paul Phillips
 *  @version 2.8
 *  @since   2.8
 *
 *  @define coll traversable or iterator
 *  @define orderDependentFold
 * 
 *    Note: might return different results for different runs, unless the
 *    underlying collection type is ordered or the operator is associative
 *    and commutative.
 *  @define willNotTerminateInf
 *
 *    Note: will not terminate for infinite-sized collections.
 */
trait TraversableOnce[+A] {
  self =>

  /** Self-documenting abstract methods. */
  def foreach[U](f: A => U): Unit
  def isEmpty: Boolean
  def hasDefiniteSize: Boolean
  
  /** Tests whether this $coll can be repeatedly traversed.  Always
   *  true for Traversables and false for Iterators unless overridden.
   *
   *  @return   `true` if it is repeatedly traversable, `false` otherwise.
   */
  def isTraversableAgain: Boolean
  
  /** Returns an Iterator over the elements in this $coll.  Will return
   *  the same Iterator if this instance is already an Iterator.
   *  $willNotTerminateInf
   *  @return an Iterator containing all elements of this $coll.
   */	
  def toIterator: Iterator[A]
  
  /** Converts this $coll to an unspecified Traversable.  Will return
   *  the same collection if this instance is already Traversable.
   *  $willNotTerminateInf
   *  @return a Traversable containing all elements of this $coll.
   */	
  def toTraversable: Traversable[A]

  /** Converts this $coll to a stream.
   *  $willNotTerminateInf
   *  @return a stream containing all elements of this $coll.
   */	
  def toStream: Stream[A]
    
  // Note: We could redefine this in TraversableLike to always return `repr`
  // of type `Repr`, only if `Repr` had type bounds, which it doesn't, because
  // not all `Repr` are a subtype `TraversableOnce[A]`.
  // The alternative is redefining it for maps, sets and seqs. For concrete implementations
  // we don't have to do this anyway, since they are leaves in the inheritance hierarchy.
  /** A version of this collection with all
   *  of the operations implemented sequentially (i.e. in a single-threaded manner).
   *
   *  This method returns a reference to this collection. In parallel collections,
   *  it is redefined to return a sequential implementation of this collection. In
   *  both cases, it has O(1) complexity.
   *  
   *  @return a sequential view of the collection.
   */
  def seq: TraversableOnce[A] = this
  
  /** Presently these are abstract because the Traversable versions use
   *  breakable/break, and I wasn't sure enough of how that's supposed to
   *  function to consolidate them with the Iterator versions.
   */
  def forall(p: A => Boolean): Boolean
  def exists(p: A => Boolean): Boolean
  def find(p: A => Boolean): Option[A]
  def copyToArray[B >: A](xs: Array[B], start: Int, len: Int): Unit
  
  // for internal use
  protected[this] def reversed = {
    var elems: List[A] = Nil
    self.seq foreach (elems ::= _)
    elems
  }
    
  /** The size of this $coll.
   *
   *  $willNotTerminateInf
   *
   *  @return    the number of elements in this $coll.
   */
  def size: Int = {
    var result = 0	
    for (x <- self) result += 1
    result
  }
  
  /** Tests whether the $coll is not empty.
   * 
   *  @return    `true` if the $coll contains at least one element, `false` otherwise.
   */
  def nonEmpty: Boolean = !isEmpty
  
  /** Counts the number of elements in the $coll which satisfy a predicate.
   *
   *  @param p     the predicate  used to test elements.
   *  @return      the number of elements satisfying the predicate `p`.
   */
  def count(p: A => Boolean): Int = {
    var cnt = 0
    for (x <- this)
      if (p(x)) cnt += 1

    cnt
  }
  
  /** Finds the first element of the $coll for which the given partial
   *  function is defined, and applies the partial function to it.
   *
   *  $mayNotTerminateInf
   *  $orderDependent
   *
   *  @param pf   the partial function
   *  @return     an option value containing pf applied to the first
   *              value for which it is defined, or `None` if none exists.
   *  @example   `Seq("a", 1, 5L).collectFirst({ case x: Int => x*10 }) = Some(10)`
   */
  def collectFirst[B](pf: PartialFunction[A, B]): Option[B] = {
    for (x <- self.toIterator) { // make sure to use an iterator or `seq`
      if (pf isDefinedAt x)
        return Some(pf(x))
    }
    None
  }
  
  /** Applies a binary operator to a start value and all elements of this $coll,
   *  going left to right.
   *
   *  Note: `/:` is alternate syntax for `foldLeft`; `z /: xs` is the same as
   *  `xs foldLeft z`.
   *  $willNotTerminateInf
   *  $orderDependentFold
   *
   *  @param   z    the start value.
   *  @param   op   the binary operator.
   *  @tparam  B    the result type of the binary operator.
   *  @return  the result of inserting `op` between consecutive elements of this $coll,
   *           going left to right with the start value `z` on the left:
   *           {{{
   *             op(...op(op(z, x,,1,,), x,,2,,), ..., x,,n,,)
   *           }}}
   *           where `x,,1,,, ..., x,,n,,` are the elements of this $coll.
   */
  def /:[B](z: B)(op: (B, A) => B): B = foldLeft(z)(op)
  
  /** Applies a binary operator to all elements of this $coll and a start value,
   *  going right to left.
   *
   *  Note: `:\` is alternate syntax for `foldRight`; `xs :\ z` is the same as
   *  `xs foldRight z`.
   *  $willNotTerminateInf
   *  $orderDependentFold
   * 
   *  @param   z    the start value
   *  @param   op   the binary operator
   *  @tparam  B    the result type of the binary operator.
   *  @return  the result of inserting `op` between consecutive elements of this $coll,
   *           going right to left with the start value `z` on the right:
   *           {{{
   *             op(x,,1,,, op(x,,2,,, ... op(x,,n,,, z)...))
   *           }}}
   *           where `x,,1,,, ..., x,,n,,` are the elements of this $coll.
   */
  def :\[B](z: B)(op: (A, B) => B): B = foldRight(z)(op)
  
  /** Applies a binary operator to a start value and all elements of this $coll,
   *  going left to right.
   * 
   *  $willNotTerminateInf
   *  $orderDependentFold
   *
   *  @param   z    the start value.
   *  @param   op   the binary operator.
   *  @tparam  B    the result type of the binary operator.
   *  @return  the result of inserting `op` between consecutive elements of this $coll,
   *           going left to right with the start value `z` on the left:
   *           {{{
   *             op(...op(z, x,,1,,), x,,2,,, ..., x,,n,,)
   *           }}}
   *           where `x,,1,,, ..., x,,n,,` are the elements of this $coll.
   */
  def foldLeft[B](z: B)(op: (B, A) => B): B = {
    var result = z
    this.seq foreach (x => result = op(result, x))
    result
  }
  
  /** Applies a binary operator to all elements of this $coll and a start value,
   *  going right to left.
   * 
   *  $willNotTerminateInf
   *  $orderDependentFold
   *  @param   z    the start value.
   *  @param   op   the binary operator.
   *  @tparam  B    the result type of the binary operator.
   *  @return  the result of inserting `op` between consecutive elements of this $coll,
   *           going right to left with the start value `z` on the right:
   *           {{{
   *             op(x,,1,,, op(x,,2,,, ... op(x,,n,,, z)...))
   *           }}}
   *           where `x,,1,,, ..., x,,n,,` are the elements of this $coll.
   */
  def foldRight[B](z: B)(op: (A, B) => B): B =
    reversed.foldLeft(z)((x, y) => op(y, x))
  
  /** Applies a binary operator to all elements of this $coll, going left to right.
   *  $willNotTerminateInf
   *  $orderDependentFold
   *
   *  @param  op    the binary operator.
   *  @tparam  B    the result type of the binary operator.
   *  @return  the result of inserting `op` between consecutive elements of this $coll,
   *           going left to right:
   *           {{{
   *             op(...(op(x,,1,,, x,,2,,), ... ) , x,,n,,)
   *           }}}
   *           where `x,,1,,, ..., x,,n,,` are the elements of this $coll.
   *  @throws `UnsupportedOperationException` if this $coll is empty.
   */
  def reduceLeft[B >: A](op: (B, A) => B): B = {
    if (isEmpty)
      throw new UnsupportedOperationException("empty.reduceLeft")
    
    var first = true
    var acc: B = 0.asInstanceOf[B]

    for (x <- self) {
      if (first) {
        acc = x
        first = false
      }
      else acc = op(acc, x)
    }
    acc
  }

  /** Applies a binary operator to all elements of this $coll, going right to left.
   *  $willNotTerminateInf
   *  $orderDependentFold
   * 
   *  @param  op    the binary operator.
   *  @tparam  B    the result type of the binary operator.
   *  @return  the result of inserting `op` between consecutive elements of this $coll,
   *           going right to left:
   *           {{{
   *             op(x,,1,,, op(x,,2,,, ..., op(x,,n-1,,, x,,n,,)...))
   *           }}}
   *           where `x,,1,,, ..., x,,n,,` are the elements of this $coll.
   *  @throws `UnsupportedOperationException` if this $coll is empty.
   */
  def reduceRight[B >: A](op: (A, B) => B): B = {
    if (isEmpty)
      throw new UnsupportedOperationException("empty.reduceRight")
    
    reversed.reduceLeft[B]((x, y) => op(y, x))
  }
  
  /** Optionally applies a binary operator to all elements of this $coll, going left to right.
   *  $willNotTerminateInf
   *  $orderDependentFold
   *  
   *  @param  op    the binary operator.
   *  @tparam  B    the result type of the binary operator.
   *  @return  an option value containing the result of `reduceLeft(op)` is this $coll is nonempty,
   *           `None` otherwise.
   */
  def reduceLeftOption[B >: A](op: (B, A) => B): Option[B] =
    if (isEmpty) None else Some(reduceLeft(op))

  /** Optionally applies a binary operator to all elements of this $coll, going
   *  right to left.
   *  $willNotTerminateInf
   *  $orderDependentFold
   * 
   *  @param  op    the binary operator.
   *  @tparam  B    the result type of the binary operator.
   *  @return  an option value containing the result of `reduceRight(op)` is this $coll is nonempty,
   *           `None` otherwise.
   */
  def reduceRightOption[B >: A](op: (A, B) => B): Option[B] =
    if (isEmpty) None else Some(reduceRight(op))
  
  /** Sums up the elements of this collection.
   * 
   *   @param   num  an implicit parameter defining a set of numeric operations
   *                 which includes the `+` operator to be used in forming the sum.
   *   @tparam  B    the result type of the `+` operator.  
   *   @return       the sum of all elements of this $coll with respect to the `+` operator in `num`.
   *
   *   @usecase def sum: A
   * 
   *   @return       the sum of all elements in this $coll of numbers of type `Int`.
   *   Instead of `Int`, any other type `T` with an implicit `Numeric[T]` implementation 
   *   can be used as element type of the $coll and as result type of `sum`. 
   *   Examples of such types are: `Long`, `Float`, `Double`, `BigInt`.
   * 
   */
  def sum[B >: A](implicit num: Numeric[B]): B = foldLeft(num.zero)(num.plus)
  
  /** Multiplies up the elements of this collection.
   * 
   *   @param   num  an implicit parameter defining a set of numeric operations
   *                 which includes the `*` operator to be used in forming the product.
   *   @tparam  B    the result type of the `*` operator.  
   *   @return       the product of all elements of this $coll with respect to the `*` operator in `num`.
   *
   *   @usecase def product: A
   * 
   *   @return       the product of all elements in this $coll of numbers of type `Int`.
   *   Instead of `Int`, any other type `T` with an implicit `Numeric[T]` implementation 
   *   can be used as element type of the $coll and as result type of `product`. 
   *   Examples of such types are: `Long`, `Float`, `Double`, `BigInt`.
   */
  def product[B >: A](implicit num: Numeric[B]): B = foldLeft(num.one)(num.times)

  /** Finds the smallest element.
   *
   *  @param    cmp   An ordering to be used for comparing elements.
   *  @tparam   B     The type over which the ordering is defined.   
   *  @return   the smallest element of this $coll with respect to the ordering `cmp`.
   *
   *  @usecase def min: A
   *  @return   the smallest element of this $coll  
   */
  def min[B >: A](implicit cmp: Ordering[B]): A = {
    if (isEmpty)
      throw new UnsupportedOperationException("empty.min")
    
    reduceLeft((x, y) => if (cmp.lteq(x, y)) x else y)
  }

  /** Finds the largest element.
   *
   *  @param    cmp   An ordering to be used for comparing elements.
   *  @tparam   B     The type over which the ordering is defined.   
   *  @return   the largest element of this $coll with respect to the ordering `cmp`.
   *
   *  @usecase def max: A
   *  @return   the largest element of this $coll. 
   */
  def max[B >: A](implicit cmp: Ordering[B]): A = {
    if (isEmpty)
      throw new UnsupportedOperationException("empty.max")
    
    reduceLeft((x, y) => if (cmp.gteq(x, y)) x else y)
  }

  def maxBy[B](f: A => B)(implicit cmp: Ordering[B]): A = {
    if (isEmpty)
      throw new UnsupportedOperationException("empty.maxBy")
    
    reduceLeft((x, y) => if (cmp.gteq(f(x), f(y))) x else y)
  }
  def minBy[B](f: A => B)(implicit cmp: Ordering[B]): A = {
    if (isEmpty)
      throw new UnsupportedOperationException("empty.maxBy")
    
    reduceLeft((x, y) => if (cmp.lteq(f(x), f(y))) x else y)
  }

  /** Copies all elements of this $coll to a buffer.
   *  $willNotTerminateInf
   *  @param  dest   The buffer to which elements are copied.
   */
  def copyToBuffer[B >: A](dest: Buffer[B]): Unit = dest ++= self
  
  /** Copies values of this $coll to an array.
   *  Fills the given array `xs` with values of this $coll, after skipping `start` values.
   *  Copying will stop once either the end of the current $coll is reached,
   *  or the end of the array is reached.
   *
   *  $willNotTerminateInf
   * 
   *  @param  xs     the array to fill.
   *  @param  start  the starting index.
   *  @tparam B      the type of the elements of the array. 
   *
   *  @usecase def copyToArray(xs: Array[A], start: Int): Unit
   */
  def copyToArray[B >: A](xs: Array[B], start: Int): Unit = 
    copyToArray(xs, start, xs.length - start)

  /** Copies values of this $coll to an array.
   *  Fills the given array `xs` with values of this $coll.
   *  Copying will stop once either the end of the current $coll is reached,
   *  or the end of the array is reached.
   *
   *  $willNotTerminateInf
   * 
   *  @param  xs     the array to fill.
   *  @tparam B      the type of the elements of the array. 
   * 
   *  @usecase def copyToArray(xs: Array[A]): Unit
   */
  def copyToArray[B >: A](xs: Array[B]): Unit =
    copyToArray(xs, 0, xs.length)
  
  /** Converts this $coll to an array.
   *  $willNotTerminateInf
   * 
   *  @tparam B the type of the elements of the array. A `ClassManifest` for
   *            this type must be available.
   *  @return   an array containing all elements of this $coll.
   *
   *  @usecase def toArray: Array[A]
   *  @return  an array containing all elements of this $coll.
   *           A `ClassManifest` must be available for the element type of this $coll.
   */
  def toArray[B >: A : ClassManifest]: Array[B] = {
    if (isTraversableAgain) {
      val result = new Array[B](size)
      copyToArray(result, 0)
      result
    }
    else toBuffer.toArray
  }
  
  /** Converts this $coll to a list.
   *  $willNotTerminateInf
   *  @return a list containing all elements of this $coll.
   */
  def toList: List[A] = new ListBuffer[A] ++= self toList

  /** Converts this $coll to an iterable collection.  Note that
   *  the choice of target `Iterable` is lazy in this default implementation
   *  as this `TraversableOnce` may be lazy and unevaluated (i.e. it may
   *  be an iterator which is only traversable once).
   *
   *  $willNotTerminateInf 
   *  @return an `Iterable` containing all elements of this $coll.
   */	
  def toIterable: Iterable[A] = toStream
 
  /** Converts this $coll to a sequence. As with `toIterable`, it's lazy
   *  in this default implementation, as this `TraversableOnce` may be
   *  lazy and unevaluated.
   *  
   *  $willNotTerminateInf
   *  @return a sequence containing all elements of this $coll.
   */	
  def toSeq: Seq[A] = toStream
 
  /** Converts this $coll to an indexed sequence.
   *  $willNotTerminateInf
   *  @return an indexed sequence containing all elements of this $coll.
   */	
  def toIndexedSeq[B >: A]: immutable.IndexedSeq[B] = immutable.IndexedSeq() ++ self
  
  /** Converts this $coll to a mutable buffer.
   *  $willNotTerminateInf
   *  @return a buffer containing all elements of this $coll.
   */	
  def toBuffer[B >: A]: mutable.Buffer[B] = new ArrayBuffer[B] ++= self
  
  /** Converts this $coll to a set.
   *  $willNotTerminateInf
   *  @return      a set containing all elements of this $coll.
   */
  def toSet[B >: A]: immutable.Set[B] = immutable.Set() ++ self

  /** Converts this $coll to a map.  This method is unavailable unless
   *  the elements are members of Tuple2, each ((T, U)) becoming a key-value
   *  pair in the map.  Duplicate keys will be overwritten by later keys:
   *  if this is an unordered collection, which key is in the resulting map
   *  is undefined.
   *  $willNotTerminateInf
   *  @return    a map containing all elements of this $coll.
   *  @usecase   def toMap[T, U]: Map[T, U]
   *  @return    a map of type `immutable.Map[T, U]`
   *             containing all key/value pairs of type `(T, U)` of this $coll.
   */
  def toMap[T, U](implicit ev: A <:< (T, U)): immutable.Map[T, U] = {
    val b = immutable.Map.newBuilder[T, U]
    for (x <- self)
      b += x
      
    b.result
  }
  
  /** Displays all elements of this $coll in a string using start, end, and
   *  separator strings.
   *
   *  @param start the starting string.
   *  @param sep   the separator string.
   *  @param end   the ending string.
   *  @return      a string representation of this $coll. The resulting string
   *               begins with the string `start` and ends with the string
   *               `end`. Inside, the string representations (w.r.t. the method
   *               `toString`) of all elements of this $coll are separated by
   *               the string `sep`.
   *
   *  @example  `List(1, 2, 3).mkString("(", "; ", ")") = "(1; 2; 3)"`
   */
  def mkString(start: String, sep: String, end: String): String =
    addString(new StringBuilder(), start, sep, end).toString

  /** Displays all elements of this $coll in a string using a separator string.
   *
   *  @param sep   the separator string.
   *  @return      a string representation of this $coll. In the resulting string
   *               the string representations (w.r.t. the method `toString`)
   *               of all elements of this $coll are separated by the string `sep`.
   *
   *  @example  `List(1, 2, 3).mkString("|") = "1|2|3"`
   */
  def mkString(sep: String): String = mkString("", sep, "")

  /** Displays all elements of this $coll in a string.
   *
   *  @return a string representation of this $coll. In the resulting string
   *          the string representations (w.r.t. the method `toString`)
   *          of all elements of this $coll follow each other without any
   *          separator string.
   */
  def mkString: String = mkString("")

  /** Appends all elements of this $coll to a string builder using start, end,
   *  and separator strings.
   *  The written text begins with the string `start` and ends with the string
   *  `end`. Inside, the string representations (w.r.t. the method `toString`)
   *  of all elements of this $coll are separated by the string `sep`.
   *
   *  @param  b    the string builder to which elements are appended.
   *  @param start the starting string.
   *  @param sep   the separator string.
   *  @param end   the ending string.
   *  @return      the string builder `b` to which elements were appended.
   */
  def addString(b: StringBuilder, start: String, sep: String, end: String): StringBuilder = {
    var first = true

    b append start    
    for (x <- self) {
      if (first) {
        b append x
        first = false
      }
      else {
        b append sep
        b append x
      }
    }
    b append end

    b
  }

  /** Appends all elements of this $coll to a string builder using a separator
   *  string. The written text consists of the string representations (w.r.t.
   *  the method `toString`) of all elements of this $coll, separated by the
   *  string `sep`.
   *
   *  @param  b    the string builder to which elements are appended.
   *  @param sep   the separator string.
   *  @return      the string builder `b` to which elements were appended.
   */
  def addString(b: StringBuilder, sep: String): StringBuilder = addString(b, "", sep, "")

  /** Appends all elements of this $coll to a string builder.
   *  The written text consists of the string representations (w.r.t. the method
   * `toString`) of all elements of this $coll without any separator string.
   *
   *  @param  b    the string builder to which elements are appended.
   *  @return      the string builder `b` to which elements were appended.
   */
  def addString(b: StringBuilder): StringBuilder = addString(b, "")
}

object TraversableOnce {
  implicit def traversableOnceCanBuildFrom[T] = new OnceCanBuildFrom[T]  
  implicit def wrapTraversableOnce[A](trav: TraversableOnce[A]) = new MonadOps(trav)
  implicit def flattenTraversableOnce[A, CC[_]](travs: TraversableOnce[CC[A]])(implicit ev: CC[A] => TraversableOnce[A]) =
    new FlattenOps[A](travs map ev)
  
  /** With the advent of TraversableOnce, it can be useful to have a builder which
   *  operates on Iterators so they can be treated uniformly along with the collections.
   *  See scala.util.Random.shuffle for an example.
   */
  class OnceCanBuildFrom[A] extends generic.CanBuildFrom[TraversableOnce[A], A, TraversableOnce[A]] {
    def newIterator = new ArrayBuffer[A] mapResult (_.iterator)
    
    /** Creates a new builder on request of a collection.
     *  @param from  the collection requesting the builder to be created.
     *  @return the result of invoking the `genericBuilder` method on `from`.
     */
    def apply(from: TraversableOnce[A]) = newIterator
    
    /** Creates a new builder from scratch
     *  @return the result of invoking the `newBuilder` method of this factory.
     */
    def apply() = newIterator
  }
  
  class FlattenOps[A](travs: TraversableOnce[TraversableOnce[A]]) {
    def flatten: Iterator[A] = travs.foldLeft(Iterator.empty: Iterator[A])(_ ++ _)
  }

  class MonadOps[+A](trav: TraversableOnce[A]) {    
    def map[B](f: A => B): TraversableOnce[B] = trav.toIterator map f
    def flatMap[B](f: A => TraversableOnce[B]): TraversableOnce[B] = trav.toIterator flatMap f
    def withFilter(p: A => Boolean) = trav.toIterator filter p
    def filter(p: A => Boolean): TraversableOnce[A] = withFilter(p)
  }
}

