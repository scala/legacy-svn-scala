/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.collection
package mutable

import generic._
import scala.collection.parallel.mutable.ParHashMap

/** This class implements mutable maps using a hashtable. It provides all of the map operations available in [[scala.collection.Map]], along with mutating operations such as put and +=.
 *  It does not allow use of null in either the key or value.
 *
 *  This class makes no guarantees as to the order of the map; in particular, it does not guarantee that the order will remain constant over time.
 *  This implementation provides constant-time performance for the basic operations (get and put), assuming the hash function disperses the elements properly among the buckets.
 *  Iteration over collection views requires time proportional to the "capacity" of the `HashMap` instance (the number of buckets) plus its size (the number of key-value mappings).
 *
 *  == Performance Tuning ==
 *
 *  An instance of `HashMap` has two parameters that affect its performance: initial capacity and load factor. The load factor for a `HashMap` is set to 0.75 by default.
 *  The capacity is the number of buckets in the hash table, and the initial capacity is simply the capacity at the time the hash table is created, 16 if unspecified.
 *  As the hash table fills up, the load factor is used to decide whether to increase the capacity. When the number of entries in the hash table exceeds the product
 *  of the load factor and the current capacity, the hash table is rehashed (that is, internal data structures are rebuilt)
 *  so that the hash table has approximately twice the number of buckets.
 *
 *  As a general rule, the default load factor (0.75) offers a good tradeoff between time and space costs.
 *  Higher values decrease the space overhead but increase the lookup cost (reflected in most of the operations of the `HashMap` class, including get and put).
 *  The expected number of entries in the map and its load factor should be taken into account when setting its initial capacity, so as to minimize the number of rehash operations.
 *  If the initial capacity is greater than the maximum number of entries divided by the load factor, no rehash operations will ever occur.
 *
 *  If many mappings are to be stored in a `HashMap` instance, creating it with a sufficiently large capacity will allow the mappings to be stored more efficiently
 *  than letting it perform automatic rehashing as needed to grow the table.
 *
 *  To use a different load factor, define the values in your implementation of `HashMap`:
 *
 *  {{{
 *  class GreedyHashMap[A,B] extends HashMap[A,B] {
 *    _loadFactor = 500 // equivalent to 0.5
 *  }
 *  }}}
 *
 *  == Concurrency considerations ==
 *
 *  Note that this implementation is not synchronized. If multiple threads access a hash map concurrently, and at least one of the threads modifies the map structurally,
 *  it must be synchronized externally. (A structural modification is any operation that adds or deletes one or more mappings; merely changing the value
 *  associated with a key that an instance already contains is not a structural modification.) This is typically accomplished by synchronizing
 *  on some object that naturally encapsulates the map. If no such object exists, the map can be mixed in with [[scala.collection.mutable.SynchronizedMap]]:
 *
 *  {{{
 *  val map = new HashMap[String, Int]() with SynchronizedMap[String, Int]
 *  }}}
 *
 *  The iterators returned by all of this class's "collection view methods" are mutable, so if the map is structurally modified at any time after the iterator is created
 *  in any way except, the iterator will behave in a non-deterministic way.
 *  Note that, contrary to the behaviour of `java.util.HashMap`, `HashMap` does not throw `ConcurrentModificationException`.
 *
 *  This class shares a lot of characteristics with `java.util.HashMap`. For more information, please see [[http://download.oracle.com/javase/6/docs/api/java/util/HashMap.html java.util.HashMap]].
 *
 *  @example {{{
 *  import scala.collection.mutable.HashMap
 *
 *  val map = HashMap[Int, String]()
 *
 *  map += 1 -> "one"
 *  map += 2 -> "two"
 *
 *  println(map)                         // Map(1 -> one, 2 -> two)
 *  println(map(1))                      // one
 *  println(map.get(2))                  // Some(two)
 *  println(map.getOrElse(3, "unknown")) // unknown
 *
 *  map -= 1
 *
 *  println(map)                         // Map(2 -> two)
 *
 *  // You can also use the factory method to initialise the map:
 *
 *  val map2 = HashMap[Int, String](1 -> "one", 2 -> "two")
 *
 *  println(map2)                         // Map(1 -> one, 2 -> two)
 *  }}}
 *
 *  @since 1
 *  
 *  @tparam A    the type of the keys contained in this hash map.
 *  @tparam B    the type of the values assigned to keys in this hash map.
 *  
 *  @define Coll mutable.HashMap
 *  @define coll mutable hash map
 *  @define thatinfo the class of the returned collection. In the standard library configuration,
 *    `That` is always `HashMap[A, B]` if the elements contained in the resulting collection are 
 *    pairs of type `(A, B)`. This is because an implicit of type `CanBuildFrom[HashMap, (A, B), HashMap[A, B]]`
 *    is defined in object `HashMap`. Otherwise, `That` resolves to the most specific type that doesn't have
 *    to contain pairs of type `(A, B)`, which is `Iterable`.
 *  @define bfinfo an implicit value of class `CanBuildFrom` which determines the
 *    result class `That` from the current representation type `Repr`
 *    and the new element type `B`. This is usually the `canBuildFrom` value
 *    defined in object `HashMap`.
 *  @define mayNotTerminateInf
 *  @define willNotTerminateInf
 */
@SerialVersionUID(1L)
class HashMap[A, B] private[collection] (contents: HashTable.Contents[A, DefaultEntry[A, B]])
extends AbstractMap[A, B]
   with Map[A, B]
   with MapLike[A, B, HashMap[A, B]] 
   with HashTable[A, DefaultEntry[A, B]]
   with CustomParallelizable[(A, B), ParHashMap[A, B]]
   with Serializable
{
  initWithContents(contents)
  
  type Entry = DefaultEntry[A, B]

  override def empty: HashMap[A, B] = HashMap.empty[A, B]
  override def clear() = clearTable()
  override def size: Int = tableSize
  
  def this() = this(null)
  
  override def par = new ParHashMap[A, B](hashTableContents)
  
  // contains and apply overridden to avoid option allocations.
  override def contains(key: A) = findEntry(key) != null
  override def apply(key: A): B = {
    val result = findEntry(key)
    if (result == null) default(key)
    else result.value
  }

  def get(key: A): Option[B] = {
    val e = findEntry(key)
    if (e == null) None
    else Some(e.value)
  }

  override def put(key: A, value: B): Option[B] = {
    val e = findEntry(key)
    if (e == null) { addEntry(new Entry(key, value)); None }
    else { val v = e.value; e.value = value; Some(v) }
  }

  override def update(key: A, value: B): Unit = put(key, value)

  override def remove(key: A): Option[B] = {
    val e = removeEntry(key)
    if (e ne null) Some(e.value)
    else None
  }

  def += (kv: (A, B)): this.type = { 
    val e = findEntry(kv._1)
    if (e == null) addEntry(new Entry(kv._1, kv._2))
    else e.value = kv._2
    this
  }

  def -=(key: A): this.type = { removeEntry(key); this }

  def iterator = entriesIterator map {e => (e.key, e.value)}
  
  override def foreach[C](f: ((A, B)) => C): Unit = foreachEntry(e => f(e.key, e.value))
  
  /* Override to avoid tuple allocation in foreach */
  override def keySet: collection.Set[A] = new DefaultKeySet {
    override def foreach[C](f: A => C) = foreachEntry(e => f(e.key))
  }
  
  /* Override to avoid tuple allocation in foreach */
  override def values: collection.Iterable[B] = new DefaultValuesIterable {
    override def foreach[C](f: B => C) = foreachEntry(e => f(e.value))
  }
  
  /* Override to avoid tuple allocation */
  override def keysIterator: Iterator[A] = new AbstractIterator[A] {
    val iter    = entriesIterator
    def hasNext = iter.hasNext
    def next()  = iter.next.key
  }
  
  /* Override to avoid tuple allocation */
  override def valuesIterator: Iterator[B] = new AbstractIterator[B] {
    val iter    = entriesIterator
    def hasNext = iter.hasNext
    def next()  = iter.next.value
  }
  
  /** Toggles whether a size map is used to track hash map statistics.
   */
  def useSizeMap(t: Boolean) = if (t) {
    if (!isSizeMapDefined) sizeMapInitAndRebuild
  } else sizeMapDisable
  
  private def writeObject(out: java.io.ObjectOutputStream) {
    serializeTo(out, _.value)
  }
  
  private def readObject(in: java.io.ObjectInputStream) {
    init[B](in, new Entry(_, _))
  }
  
}

/** $factoryInfo
 *  @define Coll mutable.HashMap
 *  @define coll mutable hash map
 */
object HashMap extends MutableMapFactory[HashMap] {
  implicit def canBuildFrom[A, B]: CanBuildFrom[Coll, (A, B), HashMap[A, B]] = new MapCanBuildFrom[A, B]
  def empty[A, B]: HashMap[A, B] = new HashMap[A, B]
}
