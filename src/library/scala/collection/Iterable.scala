/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.collection

import generic._
import scala.util.control.Breaks._
import mutable.Builder

/** A base trait for iterable collections.
 *  $iterableInfo
 */
trait Iterable[+A] extends Traversable[A] 
                      with GenIterable[A]
                      with GenericTraversableTemplate[A, Iterable]
                      with IterableLike[A, Iterable[A]] {
  override def companion: GenericCompanion[Iterable] = Iterable
 
  override def seq = this
  
  /* The following methods are inherited from trait IterableLike
   *
  override def iterator: Iterator[A]
  override def takeRight(n: Int): Iterable[A]
  override def dropRight(n: Int): Iterable[A]
  override def sameElements[B >: A](that: GenIterable[B]): Boolean
  override def view
  override def view(from: Int, until: Int)
  */

}
                                         
/** $factoryInfo
 *  The current default implementation of a $Coll is a `Vector`.
 *  @define coll iterable collection
 *  @define Coll Iterable
 */
object Iterable extends TraversableFactory[Iterable] {
  
  /** $genericCanBuildFromInfo */
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, Iterable[A]] = new GenericCanBuildFrom[A]

  def newBuilder[A]: Builder[A, Iterable[A]] = immutable.Iterable.newBuilder[A]
  
  /** The minimum element of a non-empty sequence of ordered elements */
  @deprecated("use <seq>.min instead, where <seq> is the sequence for which you want to compute the minimum", "2.8.0")
  def min[A](seq: Iterable[A])(implicit ord: Ordering[A]): A = seq.min

  /** The maximum element of a non-empty sequence of ordered elements */
  @deprecated("use <seq>.max instead, where <seq> is the sequence for which you want to compute the maximum", "2.8.0")
  def max[A](seq: Iterable[A])(implicit ord: Ordering[A]): A = seq.max

  @deprecated("use View instead", "2.8.0")
  type Projection[A] = IterableView[A, Coll]
}
