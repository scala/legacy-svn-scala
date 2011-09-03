/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala

/** [[http://en.wikipedia.org/wiki/Multiton_pattern Multiton pattern]] for
  * factory objects. Uses a [[scala.collection.mutable.WeakHashMap]] (caching)
  * and [[http://en.wikipedia.org/wiki/Double-checked_locking double-checked
  * locking]] (synchronisation on the mutable `Map` for thread safety)
  * internally.
  *
  * ==Usage==
  *
  * {{{
  * object MyMultitonFactory extends Multiton[Int,String] {
  *   override protected val create = (i: Int) => {
  *     i.toString
  *   }
  * }
  * }}}
  *
  * One may also want to use more than one argument:
  *
  * {{{
  * object MyMultitonFactory extends Multiton[(Int,Int),Int] with Function2[Int,Int,Int] {
  *   override protected val create = (p: (Int,Int)) => {
  *     p._1 + p._2
  *   }
  *
  *   override def apply(a: Int, b: Int) = apply((a,b))
  * }
  * }}}
  *
  * You will have to make all constructors `private` if you want to force that
  * every instance creation must go through `Multiton`:
  *
  * {{{
  * case class Foo private (number: String)
  *
  * object Foo extends Multiton[Int,Foo] {
  *   override protected val create = (i: Int) => {
  *     new Foo(i.toString)
  *   }
  * }
  * }}}
  *
  * @tparam A key/argument-type, instances should be immutable
  * @tparam B value/return-type
  *
  * @define key   key
  * @define value value
  *
  * @author Christian Krause
  */
trait Multiton[A,B] {
  /** Returns the cache. */
  private val instances = collection.mutable.WeakHashMap[A,B]()

  /** Returns the $value associated to the given $key. */
  final def apply(key: A): B = instances get key getOrElse {
    instances.synchronized {
      instances get key getOrElse {
        val value = create(key)
        instances += (key -> value)
        value
      }
    }
  }

  /** Creates and returns a new $value. */
  protected val create: A => B
}
