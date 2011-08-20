/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.collection
package immutable

import scala.annotation.unchecked.uncheckedVariance
import compat.Platform
import scala.collection.generic._
import scala.collection.mutable.Builder
import scala.collection.parallel.immutable.ParVector

/** Companion object to the Vector class
 */
object Vector extends SeqFactory[Vector] {
  @inline implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, Vector[A]] =
    ReusableCBF.asInstanceOf[CanBuildFrom[Coll, A, Vector[A]]]
  def newBuilder[A]: Builder[A, Vector[A]] = new VectorBuilder[A]
  private[immutable] val NIL = new Vector[Nothing](0, 0, 0)
  @inline override def empty[A]: Vector[A] = NIL
}

// in principle, most members should be private. however, access privileges must
// be carefully chosen to not prevent method inlining

/** Vector is a general-purpose, immutable data structure.  It provides random access and updates 
 *  in effectively constant time, as well as very fast append and prepend.  Because vectors strike
 *  a good balance between fast random selections and fast random functional updates, they are 
 *  currently the default implementation of immutable indexed sequences.  It is backed by a little
 *  endian bit-mapped vector trie with a branching factor of 32.  Locality is very good, but not 
 *  contiguous, which is good for very large sequences.
 *
 *  `Vector` is not thread-safe, despite being immutable. It stores some internal variables to cache lookups. To ensure
 *  that this state does not become corrupted, any acces to a `Vector` from multiple threads must be synchronized.
 *
 *  @see List
 *  @See Queue
 *
 *  @tparam A the element type
 *
 *  @define Coll Vector
 *  @define coll vector
 *  @define thatinfo the class of the returned collection. In the standard library configuration,
 *    `That` is always `Vector[B]` because an implicit of type `CanBuildFrom[Vector, B, That]`
 *    is defined in object `Vector`.
 *  @define bfinfo an implicit value of class `CanBuildFrom` which determines the
 *    result class `That` from the current representation type `Repr`
 *    and the new element type `B`. This is usually the `canBuildFrom` value
 *    defined in object `Vector`.
 *  @define orderDependent
 *  @define orderDependentFold
 *  @define mayNotTerminateInf
 *  @define willNotTerminateInf
 */
final class Vector[+A](private[collection] val startIndex: Int, private[collection] val endIndex: Int, focus: Int)
extends IndexedSeq[A]
   with GenericTraversableTemplate[A, Vector]
   with IndexedSeqLike[A, Vector[A]]
   with VectorPointer[A @uncheckedVariance]
   with Serializable
   with CustomParallelizable[A, ParVector[A]]
{ self =>

override def companion: GenericCompanion[Vector] = Vector

  //assert(startIndex >= 0, startIndex+"<0")
  //assert(startIndex <= endIndex, startIndex+">"+endIndex)
  //assert(focus >= 0, focus+"<0")
  //assert(focus <= endIndex, focus+">"+endIndex)

  private[immutable] var dirty = false

  def length = endIndex - startIndex
  
  override def par = new ParVector(this)

  /**
   * `O,,c,,(1)`, `O,,m,,(0)`
   */
  override def lengthCompare(len: Int): Int = length - len

  /**
   * `O,,c,,(1)`, `O,,m,,(0)`
   */
  private[collection] final def initIterator[B >: A](s: VectorIterator[B]) {
    s.initFrom(this)
    if (dirty) s.stabilize(focus)
    if (s.depth > 1) s.gotoPos(startIndex, startIndex ^ focus)
  }
  
  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  @inline override def iterator: VectorIterator[A] = {
    val s = new VectorIterator[A](startIndex, endIndex)
    initIterator(s)
    s
  }


  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  // can still be improved
  override /*SeqLike*/
  def reverseIterator: Iterator[A] = new Iterator[A] {
    private var i = self.length
    def hasNext: Boolean = 0 < i
    def next: A = 
      if (0 < i) {
        i -= 1
        self(i)
      } else Iterator.empty.next
  }

  // TODO: reverse

  // TODO: check performance of foreach/map etc. should override or not?
  // Ideally, clients will inline calls to map all the way down, including the iterator/builder methods.
  // In principle, escape analysis could even remove the iterator/builder allocations and do it
  // with local variables exclusively. But we're not quite there yet ...

  @deprecated("this method is experimental and will be removed in a future release", "2.8.0")
  @inline def foreachFast[U](f: A => U): Unit = iterator.foreachFast(f)
  @deprecated("this method is experimental and will be removed in a future release", "2.8.0")
  @inline def mapFast[B, That](f: A => B)(implicit bf: CanBuildFrom[Vector[A], B, That]): That = {
    val b = bf(repr)
    foreachFast(x => b += f(x))
    b.result
  }


  /**
   * `O,,c,,(1)`, `O,,m,,(0)`
   */
  def apply(index: Int): A = {
    val idx = checkRangeConvert(index)
    //println("get elem: "+index + "/"+idx + "(focus:" +focus+" xor:"+(idx^focus)+" depth:"+depth+")")
    getElem(idx, idx ^ focus)
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(0)`
   */
  private def checkRangeConvert(index: Int) = {
    val idx = index + startIndex
    if (0 <= index && idx < endIndex)
      idx
    else
      throw new IndexOutOfBoundsException(index.toString)
  }


  // SeqLike api

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  @inline override def updated[B >: A, That](index: Int, elem: B)(implicit bf: CanBuildFrom[Vector[A], B, That]): That = {
    // just ignore bf
    updateAt(index, elem).asInstanceOf[That]
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  @inline override def +:[B >: A, That](elem: B)(implicit bf: CanBuildFrom[Vector[A], B, That]): That = {
    // just ignore bf
    appendFront(elem).asInstanceOf[That]
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  @inline override def :+[B >: A, That](elem: B)(implicit bf: CanBuildFrom[Vector[A], B, That]): That = {
    // just ignore bf
    appendBack(elem).asInstanceOf[That]
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override def take(n: Int): Vector[A] = {
    if (n <= 0)
      Vector.empty
    else if (startIndex + n < endIndex)
      dropBack0(startIndex + n)
    else
      this
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override def drop(n: Int): Vector[A] = {
    if (n <= 0)
      this
    else if (startIndex + n < endIndex)
      dropFront0(startIndex + n)
    else
      Vector.empty
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override def takeRight(n: Int): Vector[A] = {
    if (n <= 0)
      Vector.empty
    else if (endIndex - n > startIndex)
      dropFront0(endIndex - n)
    else
      this
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override def dropRight(n: Int): Vector[A] = {
    if (n <= 0)
      this
    else if (endIndex - n > startIndex)
      dropBack0(endIndex - n)
    else
      Vector.empty
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(0)`
   */
  override /*IterableLike*/ def head: A = {
    if (isEmpty) throw new UnsupportedOperationException("empty.head")
    apply(0)
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override /*TraversableLike*/ def tail: Vector[A] = {
    if (isEmpty) throw new UnsupportedOperationException("empty.tail")
    drop(1)
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(0)`
   */
  override /*TraversableLike*/ def last: A = {
    if (isEmpty) throw new UnsupportedOperationException("empty.last")
    apply(length-1)
  }
  
  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override /*TraversableLike*/ def init: Vector[A] = {
    if (isEmpty) throw new UnsupportedOperationException("empty.init")
    dropRight(1)
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override /*IterableLike*/ def slice(from: Int, until: Int): Vector[A] =
    take(until).drop(from)

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  override /*IterableLike*/ def splitAt(n: Int): (Vector[A], Vector[A]) = (take(n), drop(n))
  
    
  // concat (stub)
  
  override def ++[B >: A, That](that: GenTraversableOnce[B])(implicit bf: CanBuildFrom[Vector[A], B, That]): That = {
    super.++(that.seq)
  }

    
  
  // semi-private api

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  private[immutable] def updateAt[B >: A](index: Int, elem: B): Vector[B] = {
    val idx = checkRangeConvert(index)
    val s = new Vector[B](startIndex, endIndex, idx)
    s.initFrom(this)
    s.dirty = dirty
    s.gotoPosWritable(focus, idx, focus ^ idx)  // if dirty commit changes; go to new pos and prepare for writing
    s.display0(idx & 0x1f) = elem.asInstanceOf[AnyRef]
    s
  }
  
  
  private def gotoPosWritable(oldIndex: Int, newIndex: Int, xor: Int) = if (dirty) {
    gotoPosWritable1(oldIndex, newIndex, xor)
  } else {
    gotoPosWritable0(newIndex, xor)
    dirty = true
  }
  
  private def gotoFreshPosWritable(oldIndex: Int, newIndex: Int, xor: Int) = if (dirty) {
    gotoFreshPosWritable1(oldIndex, newIndex, xor)
  } else {
    gotoFreshPosWritable0(oldIndex, newIndex, xor)
    dirty = true
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  private[immutable] def appendFront[B>:A](value: B): Vector[B] = {
    if (endIndex != startIndex) {
      var blockIndex = (startIndex - 1) & ~31
      var lo = (startIndex - 1) & 31

      if (startIndex != blockIndex + 32) {
        val s = new Vector[B](startIndex - 1, endIndex, blockIndex)
        s.initFrom(this)
        s.dirty = dirty
        s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex)
        s.display0(lo) = value.asInstanceOf[AnyRef]
        s
      } else {

        val freeSpace = ((1<<5*(depth)) - endIndex) // free space at the right given the current tree-structure depth
        val shift = freeSpace & ~((1<<5*(depth-1))-1) // number of elements by which we'll shift right (only move at top level)
        val shiftBlocks = freeSpace >>> 5*(depth-1) // number of top-level blocks

        //println("----- appendFront " + value + " at " + (startIndex - 1) + " reached block start")
        if (shift != 0) {
          // case A: we can shift right on the top level
          debug
          //println("shifting right by " + shiftBlocks + " at level " + (depth-1) + " (had "+freeSpace+" free space)")

          if (depth > 1) {
            val newBlockIndex = blockIndex + shift
            val newFocus = focus + shift
            val s = new Vector[B](startIndex - 1 + shift, endIndex + shift, newBlockIndex)
            s.initFrom(this)
            s.dirty = dirty
            s.shiftTopLevel(0, shiftBlocks) // shift right by n blocks 
            s.debug
            s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex) // maybe create pos; prepare for writing
            s.display0(lo) = value.asInstanceOf[AnyRef]
            //assert(depth == s.depth)
            s
          } else {
            val newBlockIndex = blockIndex + 32
            val newFocus = focus

            //assert(newBlockIndex == 0)
            //assert(newFocus == 0)

            val s = new Vector[B](startIndex - 1 + shift, endIndex + shift, newBlockIndex)
            s.initFrom(this)
            s.dirty = dirty
            s.shiftTopLevel(0, shiftBlocks) // shift right by n elements
            s.gotoPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex) // prepare for writing
            s.display0(shift-1) = value.asInstanceOf[AnyRef]
            s.debug
            s
          }
        } else if (blockIndex < 0) {
          // case B: we need to move the whole structure
          val move = (1 << 5*(depth+1)) - (1 << 5*(depth))
          //println("moving right by " + move + " at level " + (depth-1) + " (had "+freeSpace+" free space)")

          val newBlockIndex = blockIndex + move
          val newFocus = focus + move


          val s = new Vector[B](startIndex - 1 + move, endIndex + move, newBlockIndex)
          s.initFrom(this)
          s.dirty = dirty
          s.debug
          s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex) // could optimize: we know it will create a whole branch
          s.display0(lo) = value.asInstanceOf[AnyRef]
          s.debug
          //assert(s.depth == depth+1)
          s
        } else {
          val newBlockIndex = blockIndex
          val newFocus = focus

          val s = new Vector[B](startIndex - 1, endIndex, newBlockIndex)
          s.initFrom(this)
          s.dirty = dirty
          s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex)
          s.display0(lo) = value.asInstanceOf[AnyRef]
          //assert(s.depth == depth)
          s
        }

      }
    } else {
      // empty vector, just insert single element at the back
      val elems = new Array[AnyRef](32)
      elems(31) = value.asInstanceOf[AnyRef]
      val s = new Vector[B](31,32,0)
      s.depth = 1
      s.display0 = elems
      s
    }
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  private[immutable] def appendBack[B>:A](value: B): Vector[B] = {
//    //println("------- append " + value)
//    debug()
    if (endIndex != startIndex) {
      var blockIndex = endIndex & ~31
      var lo = endIndex & 31
      
      if (endIndex != blockIndex) {
        //println("will make writable block (from "+focus+") at: " + blockIndex)
        val s = new Vector[B](startIndex, endIndex + 1, blockIndex)
        s.initFrom(this)
        s.dirty = dirty
        s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex)
        s.display0(lo) = value.asInstanceOf[AnyRef]
        s
      } else {
        val shift = startIndex & ~((1<<5*(depth-1))-1)
        val shiftBlocks = startIndex >>> 5*(depth-1)

        //println("----- appendBack " + value + " at " + endIndex + " reached block end")
        
        if (shift != 0) {
          debug
          //println("shifting left by " + shiftBlocks + " at level " + (depth-1) + " (had "+startIndex+" free space)")
          if (depth > 1) {
            val newBlockIndex = blockIndex - shift
            val newFocus = focus - shift
            val s = new Vector[B](startIndex - shift, endIndex + 1 - shift, newBlockIndex)
            s.initFrom(this)
            s.dirty = dirty
            s.shiftTopLevel(shiftBlocks, 0) // shift left by n blocks 
            s.debug
            s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex)
            s.display0(lo) = value.asInstanceOf[AnyRef]
            s.debug
            //assert(depth == s.depth)
            s
          } else {
            val newBlockIndex = blockIndex - 32
            val newFocus = focus

            //assert(newBlockIndex == 0)
            //assert(newFocus == 0)

            val s = new Vector[B](startIndex - shift, endIndex + 1 - shift, newBlockIndex)
            s.initFrom(this)
            s.dirty = dirty
            s.shiftTopLevel(shiftBlocks, 0) // shift right by n elements
            s.gotoPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex)
            s.display0(32 - shift) = value.asInstanceOf[AnyRef]
            s.debug
            s
          }
        } else {
          val newBlockIndex = blockIndex
          val newFocus = focus

          val s = new Vector[B](startIndex, endIndex + 1, newBlockIndex)
          s.initFrom(this)
          s.dirty = dirty
          s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex)
          s.display0(lo) = value.asInstanceOf[AnyRef]
          //assert(s.depth == depth+1) might or might not create new level!
          if (s.depth == depth+1) {
            //println("creating new level " + s.depth + " (had "+0+" free space)")
            s.debug
          }
          s
        }
      }
    } else {
      val elems = new Array[AnyRef](32)
      elems(0) = value.asInstanceOf[AnyRef]
      val s = new Vector[B](0,1,0)
      s.depth = 1
      s.display0 = elems
      s
    }
  }


  // low-level implementation (needs cleanup, maybe move to util class)

  private def shiftTopLevel(oldLeft: Int, newLeft: Int) = (depth - 1) match {
    case 0 =>
      display0 = copyRange(display0, oldLeft, newLeft)
    case 1 =>
      display1 = copyRange(display1, oldLeft, newLeft)
    case 2 =>
      display2 = copyRange(display2, oldLeft, newLeft)
    case 3 =>
      display3 = copyRange(display3, oldLeft, newLeft)
    case 4 =>
      display4 = copyRange(display4, oldLeft, newLeft)
    case 5 =>
      display5 = copyRange(display5, oldLeft, newLeft)
  }

  private def zeroLeft(array: Array[AnyRef], index: Int): Unit = {
    var i = 0; while (i < index) { array(i) = null; i+=1 }
  }

  private def zeroRight(array: Array[AnyRef], index: Int): Unit = {
    var i = index; while (i < array.length) { array(i) = null; i+=1 }
  }

  private def copyLeft(array: Array[AnyRef], right: Int): Array[AnyRef] = {
//    if (array eq null)
//      println("OUCH!!! " + right + "/" + depth + "/"+startIndex + "/" + endIndex + "/" + focus)
    val a2 = new Array[AnyRef](array.length)
    Platform.arraycopy(array, 0, a2, 0, right)
    a2
  }
  private def copyRight(array: Array[AnyRef], left: Int): Array[AnyRef] = {
    val a2 = new Array[AnyRef](array.length)
    Platform.arraycopy(array, left, a2, left, a2.length - left)
    a2
  }

  private def preClean(depth: Int) = {
    this.depth = depth
    (depth - 1) match {
      case 0 =>
        display1 = null
        display2 = null
        display3 = null
        display4 = null
        display5 = null
      case 1 =>
        display2 = null
        display3 = null
        display4 = null
        display5 = null
      case 2 =>
        display3 = null
        display4 = null
        display5 = null
      case 3 =>
        display4 = null
        display5 = null
      case 4 =>
        display5 = null
      case 5 =>
    }
  }

  // requires structure is at index cutIndex and writable at level 0
  private def cleanLeftEdge(cutIndex: Int) = {
    if (cutIndex < (1 << 5)) {
      zeroLeft(display0, cutIndex)
    } else
    if (cutIndex < (1 << 10)) {
      zeroLeft(display0, cutIndex & 0x1f)
      display1 = copyRight(display1, (cutIndex >>>  5))
    } else
    if (cutIndex < (1 << 15)) {
      zeroLeft(display0, cutIndex & 0x1f)
      display1 = copyRight(display1, (cutIndex >>>  5) & 0x1f)
      display2 = copyRight(display2, (cutIndex >>> 10))
    } else
    if (cutIndex < (1 << 20)) {
      zeroLeft(display0, cutIndex & 0x1f)
      display1 = copyRight(display1, (cutIndex >>>  5) & 0x1f)
      display2 = copyRight(display2, (cutIndex >>> 10) & 0x1f)
      display3 = copyRight(display3, (cutIndex >>> 15))
    } else
    if (cutIndex < (1 << 25)) {
      zeroLeft(display0, cutIndex & 0x1f)
      display1 = copyRight(display1, (cutIndex >>>  5) & 0x1f)
      display2 = copyRight(display2, (cutIndex >>> 10) & 0x1f)
      display3 = copyRight(display3, (cutIndex >>> 15) & 0x1f)
      display4 = copyRight(display4, (cutIndex >>> 20))
    } else
    if (cutIndex < (1 << 30)) {
      zeroLeft(display0, cutIndex & 0x1f)
      display1 = copyRight(display1, (cutIndex >>>  5) & 0x1f)
      display2 = copyRight(display2, (cutIndex >>> 10) & 0x1f)
      display3 = copyRight(display3, (cutIndex >>> 15) & 0x1f)
      display4 = copyRight(display4, (cutIndex >>> 20) & 0x1f)
      display5 = copyRight(display5, (cutIndex >>> 25))
    } else {
      throw new IllegalArgumentException()
    }
  }

  // requires structure is writable and at index cutIndex
  private def cleanRightEdge(cutIndex: Int) = {

    // we're actually sitting one block left if cutIndex lies on a block boundary
    // this means that we'll end up erasing the whole block!!

    if (cutIndex <= (1 << 5)) {
      zeroRight(display0, cutIndex)
    } else
    if (cutIndex <= (1 << 10)) {
      zeroRight(display0, ((cutIndex-1) & 0x1f) + 1)
      display1 = copyLeft(display1, (cutIndex >>>  5))
    } else
    if (cutIndex <= (1 << 15)) {
      zeroRight(display0, ((cutIndex-1) & 0x1f) + 1)
      display1 = copyLeft(display1, (((cutIndex-1) >>>  5) & 0x1f) + 1)
      display2 = copyLeft(display2, (cutIndex >>> 10))
    } else
    if (cutIndex <= (1 << 20)) {
      zeroRight(display0, ((cutIndex-1) & 0x1f) + 1)
      display1 = copyLeft(display1, (((cutIndex-1) >>>  5) & 0x1f) + 1)
      display2 = copyLeft(display2, (((cutIndex-1) >>> 10) & 0x1f) + 1)
      display3 = copyLeft(display3, (cutIndex >>> 15))
    } else
    if (cutIndex <= (1 << 25)) {
      zeroRight(display0, ((cutIndex-1) & 0x1f) + 1)
      display1 = copyLeft(display1, (((cutIndex-1) >>>  5) & 0x1f) + 1)
      display2 = copyLeft(display2, (((cutIndex-1) >>> 10) & 0x1f) + 1)
      display3 = copyLeft(display3, (((cutIndex-1) >>> 15) & 0x1f) + 1)
      display4 = copyLeft(display4, (cutIndex >>> 20))
    } else
    if (cutIndex <= (1 << 30)) {
      zeroRight(display0, ((cutIndex-1) & 0x1f) + 1)
      display1 = copyLeft(display1, (((cutIndex-1) >>>  5) & 0x1f) + 1)
      display2 = copyLeft(display2, (((cutIndex-1) >>> 10) & 0x1f) + 1)
      display3 = copyLeft(display3, (((cutIndex-1) >>> 15) & 0x1f) + 1)
      display4 = copyLeft(display4, (((cutIndex-1) >>> 20) & 0x1f) + 1)
      display5 = copyLeft(display5, (cutIndex >>> 25))
    } else {
      throw new IllegalArgumentException()
    }
  }

  private def requiredDepth(xor: Int) = {
    if (xor < (1 <<  5)) 1
    else if (xor < (1 << 10)) 2
    else if (xor < (1 << 15)) 3
    else if (xor < (1 << 20)) 4
    else if (xor < (1 << 25)) 5
    else if (xor < (1 << 30)) 6
    else throw new IllegalArgumentException()
  }
  
  private def dropFront0(cutIndex: Int): Vector[A] = {
    var blockIndex = cutIndex & ~31
    var lo = cutIndex & 31
    
    val xor = cutIndex ^ (endIndex - 1)
    val d = requiredDepth(xor)
    val shift = (cutIndex & ~((1 << (5*d))-1))

    //println("cut front at " + cutIndex + ".." + endIndex + " (xor: "+xor+" shift: " + shift + " d: " + d +")")
    
/*
    val s = new Vector(cutIndex-shift, endIndex-shift, blockIndex-shift)
    s.initFrom(this)
    if (s.depth > 1)
      s.gotoPos(blockIndex, focus ^ blockIndex)
    s.depth = d
    s.stabilize(blockIndex-shift)
    s.cleanLeftEdge(cutIndex-shift)
    s
*/    

    // need to init with full display iff going to cutIndex requires swapping block at level >= d

    val s = new Vector[A](cutIndex-shift, endIndex-shift, blockIndex-shift)
    s.initFrom(this)
    s.dirty = dirty
    s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex)
    s.preClean(d)
    s.cleanLeftEdge(cutIndex - shift)
    s
  }

  /**
   * `O,,c,,(1)`, `O,,m,,(1)`
   */
  private def dropBack0(cutIndex: Int): Vector[A] = {
    var blockIndex = (cutIndex - 1) & ~31
    var lo = ((cutIndex - 1) & 31) + 1

    val xor = startIndex ^ (cutIndex - 1)
    val d = requiredDepth(xor)
    val shift = (startIndex & ~((1 << (5*d))-1))
    
/*    
    println("cut back at " + startIndex + ".." + cutIndex + " (xor: "+xor+" d: " + d +")")
    if (cutIndex == blockIndex + 32)
      println("OUCH!!!")
*/    
    val s = new Vector[A](startIndex-shift, cutIndex-shift, blockIndex-shift)
    s.initFrom(this)
    s.dirty = dirty
    s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex)
    s.preClean(d)
    s.cleanRightEdge(cutIndex-shift)
    s
  }
  
}


/** An iterator for looping over a `Vector`.
 *  Usually, you will acquire instances of `VectorIterator` from `Vector.iterator.`
 *
 *  `VectorIterator` is not thread-safe, despite being immutable. It stores some internal variables to cache lookups.
 *  To ensure that this state does not become corrupted, any acces to a `Vector` from multiple threads must be synchronized.
 */
class VectorIterator[+A](_startIndex: Int, _endIndex: Int) extends Iterator[A] with VectorPointer[A @uncheckedVariance] {

  private var blockIndex: Int = _startIndex & ~31
  private var lo: Int = _startIndex & 31
  private var endIndex: Int = _endIndex

  private var endLo = math.min(endIndex - blockIndex, 32)

  def hasNext = _hasNext

  private var _hasNext = blockIndex + lo < endIndex

  def next(): A = {
    if (!_hasNext) throw new NoSuchElementException("reached iterator end")

    val res = display0(lo).asInstanceOf[A]
    lo += 1

    if (lo == endLo) {
      if (blockIndex + lo < endIndex) {
        val newBlockIndex = blockIndex+32
        gotoNextBlockStart(newBlockIndex, blockIndex ^ newBlockIndex)

        blockIndex = newBlockIndex
        endLo = math.min(endIndex - blockIndex, 32)
        lo = 0
      } else {
        _hasNext = false
      }
    }

    res
  }
  
  private[collection] def remainingElementCount: Int = (_endIndex - (blockIndex + lo)) max 0
  
  /** Creates a new vector which consists of elements remaining in this iterator.
   *  Such a vector can then be split into several vectors using methods like `take` and `drop`.
   */
  private[collection] def remainingVector: Vector[A] = {
    val v = new Vector[A](blockIndex + lo, _endIndex, blockIndex + lo)
    v.initFrom(this)
    v
  }
  
  @deprecated("this method is experimental and will be removed in a future release", "2.8.0")
  @inline def foreachFast[U](f: A =>  U) { while (hasNext) f(next()) }
}


/** Builder for `Vector`s.
 *
 *  `VectorBuilder` is not thread-safe, despite being immutable. It stores some internal variables to cache lookups.
 *  To ensure that this state does not become corrupted, any acces to a `Vector` from multiple threads must be synchronized.
 */
final class VectorBuilder[A]() extends Builder[A,Vector[A]] with VectorPointer[A @uncheckedVariance] {
  
  // possible alternative: start with display0 = null, blockIndex = -32, lo = 32
  // to avoid allocating initial array if the result will be empty anyways
  
  display0 = new Array[AnyRef](32)
  depth = 1
  
  private var blockIndex = 0
  private var lo = 0
  
  def += (elem: A): this.type = {
    if (lo >= display0.length) {
      val newBlockIndex = blockIndex+32
      gotoNextBlockStartWritable(newBlockIndex, blockIndex ^ newBlockIndex)
      blockIndex = newBlockIndex
      lo = 0
    }
    display0(lo) = elem.asInstanceOf[AnyRef]
    lo += 1
    this
  }

  override def ++=(xs: TraversableOnce[A]): this.type =
    super.++=(xs)

  def result: Vector[A] = {
    val size = blockIndex + lo
    if (size == 0)
      return Vector.empty
    val s = new Vector[A](0, size, 0) // should focus front or back?
    s.initFrom(this)
    if (depth > 1) s.gotoPos(0, size - 1) // we're currently focused to size - 1, not size!
    s
  }
  
  def clear(): Unit = {
    display0 = new Array[AnyRef](32)
    depth = 1
    blockIndex = 0
    lo = 0
  }
}


/**
 * A path through a trie built from nested arrays. All arrays are of length 32.
 *
 * The path caches the array lookups at each stage of traversing the trie.
 * The path resolves to a 32-element block of values in ``display0``.
 * The depth of the trie is stored in `depth`, ranging from 1..6.
 * The root of the trie is stored in `display{''depth-1''}`. All `display{''n>=depth''}` are undefined.
 *
 * Each `display{''n''}` var is represented as `Array[AnyType`. This is so that when an operation widens the type of the
 * `VectorPointer` from `T` to `U >: T`, existing arrays can all be re-used without copying them to widen their type.
 *
 * The values stored in the arrays can be safely cast as long as the appropriate type-invariants are respected. These
 * are enforced by `initFrom`. When copying an existing structure, the bound on the type stored in the arrays can only
 * ever be widened. When inserting or replacing an element, the array '''must''' be copied first, and the change made to
 * the copy.
 *
 * This structure is not thread-safe.
 */
private[immutable] trait VectorPointer[T] {
    /** Depth of nesting of arrays of arrays. Counting from 0. Maximum of 5. */
    private[immutable] var depth: Int = _
    /** Array[T] */
    private[immutable] var display0: Array[AnyRef] = _
    /** Array[Array[T]] */
    private[immutable] var display1: Array[AnyRef] = _
    /** Array[Array[Array[T]]] */
    private[immutable] var display2: Array[AnyRef] = _
    /** Array[Array[Array[Array[T]]]] */
    private[immutable] var display3: Array[AnyRef] = _
    /** Array[Array[Array[Array[Array[T]]]]] */
    private[immutable] var display4: Array[AnyRef] = _
    /** Array[Array[Array[Array[Array[Array[T]]]]]] */
    private[immutable] var display5: Array[AnyRef] = _

    /** Initialize this `VectorPointer` from another one, to have the same depth and point to the exact same path.
     *
     * This is the version that is used.
     *
     * `O,,c,,(1)`, `O,,m,,(0)`
     *
     * @tparam U  Type of the other vector. We must be at least as general as `that` to maintain type-safety of the
     *          underlying arrays.
     */
    private[immutable] final def initFrom[U <: T](that: VectorPointer[U]): Unit = initFrom(that, that.depth)
    
    /** Initialize this `VectorPointer` from another one, to have the same depth and point to the exact same path.
     *
     * This version is not directly used.
     *
     * `O,,c,,(1)`, `O,,m,,(0)`
     */
    private[immutable] final def initFrom[U <: T](that: VectorPointer[U], depth: Int) = {
      this.depth = depth
      (depth - 1) match {
        case -1 =>
        case 0 =>
          display0 = that.display0
        case 1 =>
          display1 = that.display1
          display0 = that.display0
        case 2 =>
          display2 = that.display2
          display1 = that.display1
          display0 = that.display0
        case 3 =>
          display3 = that.display3
          display2 = that.display2
          display1 = that.display1
          display0 = that.display0
        case 4 =>
          display4 = that.display4
          display3 = that.display3
          display2 = that.display2
          display1 = that.display1
          display0 = that.display0
        case 5 =>
          display5 = that.display5
          display4 = that.display4
          display3 = that.display3
          display2 = that.display2
          display1 = that.display1
          display0 = that.display0
      }
    }


    /** Get an element given the current index and the xor of this and ``oldIndex``, the index that the display arrays
     * are focussed on.
     * 
     * Requires structure is at pos oldIndex = xor ^ index
     * `O,,c,,(1)`, `O,,m,,(0)`
     */
    private[immutable] final def getElem(index: Int, xor: Int): T = {
      if (xor < (1 << 5)) { // level = 0
        display0(index & 31).asInstanceOf[T]
      } else
      if (xor < (1 << 10)) { // level = 1
        display1((index >> 5) & 31).asInstanceOf[Array[AnyRef]](index & 31).asInstanceOf[T]
      } else
      if (xor < (1 << 15)) { // level = 2
        display2((index >> 10) & 31).asInstanceOf[Array[AnyRef]]((index >> 5) & 31).asInstanceOf[Array[AnyRef]](index & 31).asInstanceOf[T]
      } else
      if (xor < (1 << 20)) { // level = 3
        display3((index >> 15) & 31).asInstanceOf[Array[AnyRef]]((index >> 10) & 31).asInstanceOf[Array[AnyRef]]((index >> 5) & 31).asInstanceOf[Array[AnyRef]](index & 31).asInstanceOf[T]
      } else
      if (xor < (1 << 25)) { // level = 4
        display4((index >> 20) & 31).asInstanceOf[Array[AnyRef]]((index >> 15) & 31).asInstanceOf[Array[AnyRef]]((index >> 10) & 31).asInstanceOf[Array[AnyRef]]((index >> 5) & 31).asInstanceOf[Array[AnyRef]](index & 31).asInstanceOf[T]
      } else
      if (xor < (1 << 30)) { // level = 5
        display5((index >> 25) & 31).asInstanceOf[Array[AnyRef]]((index >> 20) & 31).asInstanceOf[Array[AnyRef]]((index >> 15) & 31).asInstanceOf[Array[AnyRef]]((index >> 10) & 31).asInstanceOf[Array[AnyRef]]((index >> 5) & 31).asInstanceOf[Array[AnyRef]](index & 31).asInstanceOf[T]
      } else { // level = 6
        throw new IllegalArgumentException()
      }
    }

    /** Go to the specific position minimizing the lookups for random access patterns.
     *
     * This assumes that the structure is currently at `pos oldIndex = xor ^ index`. If this is not the case, then this
     * has undefined behaviour. Afterwards, the structure is positioned on the block containing `index`.
     *
     * The `xor` argument is used to minimize how many levels down from `display0` need to be altered, so that in the
     * common case of moving within the same block, nothing is updated, for the next-most common case of moving to the
     * next block in `display1`, only one lookup is done, and so on.
     *
     * `O,,c,,(1)`, `O,,m,,(0)`
     */
    private[immutable] final def gotoPos(index: Int, xor: Int): Unit = {
      if (xor < (1 << 5)) { // level = 0 (could maybe removed)
      } else
      if (xor < (1 << 10)) { // level = 1
        display0 = display1((index >> 5) & 31).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 15)) { // level = 2
        display1 = display2((index >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = display1((index >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 20)) { // level = 3
        display2 = display3((index >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = display2((index >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = display1((index >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 25)) { // level = 4
        display3 = display4((index >> 20) & 31).asInstanceOf[Array[AnyRef]]
        display2 = display3((index >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = display2((index >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = display1((index >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 30)) { // level = 5
        display4 = display5((index >> 25) & 31).asInstanceOf[Array[AnyRef]]
        display3 = display4((index >> 20) & 31).asInstanceOf[Array[AnyRef]]
        display2 = display3((index >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = display2((index >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = display1((index >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else { // level = 6
        throw new IllegalArgumentException()
      }
    }



    /** Go to the starting block containing the index.
     *  Ideal for sequential access patterns. This is used by Iterator.
     *
     * This assumes that the structure is currently at `pos oldIndex = xor ^ index`. If this is not the case, then this
     * has undefined behaviour. Afterwards, the structure is positioned on the recursively-starting block (zero-indexed)
     * in the sub-tree spanning the old and new indexes.
     *
     * The `xor` argument is used to minimize how many levels down from `display0` need to be altered, so that in the
     * common case of moving within the same block, nothing is updated, for the next-most common case of moving to the
     * next block in `display1`, only one lookup is done, and so on.
     *
     * `O,,c,,(1)`, `O,,m,,(0)`
     */
    private[immutable] final def gotoNextBlockStart(index: Int, xor: Int): Unit = { // goto block start pos
      if (xor < (1 << 10)) { // level = 1
        display0 = display1((index >> 5) & 31).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 15)) { // level = 2
        display1 = display2((index >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = display1(0).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 20)) { // level = 3
        display2 = display3((index >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = display2(0).asInstanceOf[Array[AnyRef]]
        display0 = display1(0).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 25)) { // level = 4
        display3 = display4((index >> 20) & 31).asInstanceOf[Array[AnyRef]]
        display2 = display3(0).asInstanceOf[Array[AnyRef]]
        display1 = display2(0).asInstanceOf[Array[AnyRef]]
        display0 = display1(0).asInstanceOf[Array[AnyRef]]
      } else
      if (xor < (1 << 30)) { // level = 5
        display4 = display5((index >> 25) & 31).asInstanceOf[Array[AnyRef]]
        display3 = display4(0).asInstanceOf[Array[AnyRef]]
        display2 = display3(0).asInstanceOf[Array[AnyRef]]
        display1 = display2(0).asInstanceOf[Array[AnyRef]]
        display0 = display1(0).asInstanceOf[Array[AnyRef]]
      } else { // level = 6
        throw new IllegalArgumentException()
      }
    }

    /**Go to the starting block containing the index, copying the structure as needed to ensure that it is ready for
     * destructive update, and inserting a new root array.
     * This is used by the builder.
     *
     * This assumes that the structure is currently at `pos oldIndex = xor ^ index`. If this is not the case, then this
     * has undefined behaviour. Afterwards, the structure is positioned on the block containing `index`.
     * The arrays in the path from the root to the block are copied to make them safe for writing to.
     *
     * `O,,c,,(1)`, `O,,m,,(0)`
     */
    private[immutable] final def gotoNextBlockStartWritable(index: Int, xor: Int): Unit = { // goto block start pos
      if (xor < (1 << 10)) { // level = 1
        if (depth == 1) { display1 = new Array(32); display1(0) = display0; depth+=1}
        display0 = new Array(32)
        display1((index >>  5) & 31) = display0
      } else
      if (xor < (1 << 15)) { // level = 2
        if (depth == 2) { display2 = new Array(32); display2(0) = display1; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
      } else
      if (xor < (1 << 20)) { // level = 3
        if (depth == 3) { display3 = new Array(32); display3(0) = display2; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display2 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
        display3((index >> 15) & 31) = display2
      } else
      if (xor < (1 << 25)) { // level = 4
        if (depth == 4) { display4 = new Array(32); display4(0) = display3; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display2 = new Array(32)
        display3 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
        display3((index >> 15) & 31) = display2
        display4((index >> 20) & 31) = display3
      } else
      if (xor < (1 << 30)) { // level = 5
        if (depth == 5) { display5 = new Array(32); display5(0) = display4; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display2 = new Array(32)
        display3 = new Array(32)
        display4 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
        display3((index >> 15) & 31) = display2
        display4((index >> 20) & 31) = display3
        display5((index >> 25) & 31) = display4
      } else { // level = 6
        throw new IllegalArgumentException()
      }
    }



    // STUFF BELOW USED BY APPEND / UPDATE

    /**
     * `O,,c,,(a.length)`, `O,,m,,(a.length)`
     */
    private[immutable] final def copyOf(a: Array[AnyRef]) = {
      //println("copy")
      if (a eq null) println ("NULL")
      val b = new Array[AnyRef](a.length)
      Platform.arraycopy(a, 0, b, 0, a.length)
      b
    }

    /**
     * `O,,c,,(a.length)`, `O,,m,,(a.length)`
     */
    private[immutable] final def nullSlotAndCopy(array: Array[AnyRef], index: Int) = {
      //println("copy and null")
      val x = array(index)
      array(index) = null
      copyOf(x.asInstanceOf[Array[AnyRef]])
    }


    // make sure there is no aliasing
    // requires structure is at pos index
    // ensures structure is clean and at pos index and writable at all levels except 0

    /**
     * `O,,c,,(1)`, `O,,m,,(1)`
     */
    private[immutable] final def stabilize(index: Int) = (depth - 1) match {
      case 5 =>
        display5 = copyOf(display5)
        display4 = copyOf(display4)
        display3 = copyOf(display3)
        display2 = copyOf(display2)
        display1 = copyOf(display1)
        display5((index >> 25) & 31) = display4
        display4((index >> 20) & 31) = display3
        display3((index >> 15) & 31) = display2
        display2((index >> 10) & 31) = display1
        display1((index >>  5) & 31) = display0
      case 4 =>
        display4 = copyOf(display4)
        display3 = copyOf(display3)
        display2 = copyOf(display2)
        display1 = copyOf(display1)
        display4((index >> 20) & 31) = display3
        display3((index >> 15) & 31) = display2
        display2((index >> 10) & 31) = display1
        display1((index >>  5) & 31) = display0
      case 3 =>
        display3 = copyOf(display3)
        display2 = copyOf(display2)
        display1 = copyOf(display1)
        display3((index >> 15) & 31) = display2
        display2((index >> 10) & 31) = display1
        display1((index >>  5) & 31) = display0
      case 2 =>
        display2 = copyOf(display2)
        display1 = copyOf(display1)
        display2((index >> 10) & 31) = display1
        display1((index >>  5) & 31) = display0
      case 1 =>
        display1 = copyOf(display1)
        display1((index >>  5) & 31) = display0
      case 0 =>
    }



    /// USED IN UPDATE AND APPEND BACK
    
    // prepare for writing at an existing position

    /**
     * `O,,c,,(1)`, `O,,m,,(1)`
     */
    // requires structure is clean and at pos oldIndex = xor ^ newIndex,
    // ensures structure is dirty and at pos newIndex and writable at level 0
    private[immutable] final def gotoPosWritable0(newIndex: Int, xor: Int): Unit = (depth - 1) match {
      case 5 => 
        display5 = copyOf(display5)
        display4 = nullSlotAndCopy(display5, (newIndex >> 25) & 31).asInstanceOf[Array[AnyRef]]
        display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31).asInstanceOf[Array[AnyRef]]
        display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      case 4 => 
        display4 = copyOf(display4)
        display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31).asInstanceOf[Array[AnyRef]]
        display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      case 3 => 
        display3 = copyOf(display3)
        display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      case 2 => 
        display2 = copyOf(display2)
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      case 1 => 
        display1 = copyOf(display1)
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      case 0 => 
        display0 = copyOf(display0)
    }
    
    
    // requires structure is dirty and at pos oldIndex,
    // ensures structure is dirty and at pos newIndex and writable at level 0
    private[immutable] final def gotoPosWritable1(oldIndex: Int, newIndex: Int, xor: Int): Unit = {
      if (xor < (1 <<  5)) { // level = 0
        display0 = copyOf(display0)
      } else
      if (xor < (1 << 10)) { // level = 1
        display1 = copyOf(display1)
        display1((oldIndex >> 5) & 31) = display0
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31)
      } else                                                                               
      if (xor < (1 << 15)) { // level = 2                                                  
        display1 = copyOf(display1)
        display2 = copyOf(display2)
        display1((oldIndex >>  5) & 31) = display0                                         
        display2((oldIndex >> 10) & 31) = display1
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else                                                                               
      if (xor < (1 << 20)) { // level = 3                                                  
        display1 = copyOf(display1)
        display2 = copyOf(display2)
        display3 = copyOf(display3)
        display1((oldIndex >>  5) & 31) = display0                                         
        display2((oldIndex >> 10) & 31) = display1                                         
        display3((oldIndex >> 15) & 31) = display2                                         
        display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else                                                                               
      if (xor < (1 << 25)) { // level = 4                                                  
        display1 = copyOf(display1)
        display2 = copyOf(display2)
        display3 = copyOf(display3)
        display4 = copyOf(display4)
        display1((oldIndex >>  5) & 31) = display0                                         
        display2((oldIndex >> 10) & 31) = display1                                         
        display3((oldIndex >> 15) & 31) = display2                                         
        display4((oldIndex >> 20) & 31) = display3                                         
        display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31).asInstanceOf[Array[AnyRef]]
        display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else                                                                               
      if (xor < (1 << 30)) { // level = 5                                                  
        display1 = copyOf(display1)
        display2 = copyOf(display2)
        display3 = copyOf(display3)
        display4 = copyOf(display4)
        display5 = copyOf(display5)
        display1((oldIndex >>  5) & 31) = display0                                         
        display2((oldIndex >> 10) & 31) = display1                                         
        display3((oldIndex >> 15) & 31) = display2                                         
        display4((oldIndex >> 20) & 31) = display3                                         
        display5((oldIndex >> 25) & 31) = display4                                         
        display4 = nullSlotAndCopy(display5, (newIndex >> 25) & 31).asInstanceOf[Array[AnyRef]]
        display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31).asInstanceOf[Array[AnyRef]]
        display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        display0 = nullSlotAndCopy(display1, (newIndex >>  5) & 31).asInstanceOf[Array[AnyRef]]
      } else { // level = 6
        throw new IllegalArgumentException()
      }
    }


    // USED IN DROP
    
    private[immutable] final def copyRange(array: Array[AnyRef], oldLeft: Int, newLeft: Int) = {
      val elems = new Array[AnyRef](32)
      Platform.arraycopy(array, oldLeft, elems, newLeft, 32 - math.max(newLeft,oldLeft))
      elems
    }




    /**
     * `O,,c,,(1)`, `O,,m,,(1)`
     */
    // USED IN APPEND
    // create a new block at the bottom level (and possibly nodes on its path) and prepares for writing

    // requires structure is clean and at pos oldIndex,
    // ensures structure is dirty and at pos newIndex and writable at level 0
    private[immutable] final def gotoFreshPosWritable0(oldIndex: Int, newIndex: Int, xor: Int): Unit = { // goto block start pos
      if (xor < (1 << 5)) { // level = 0
        //println("XXX clean with low xor")
      } else
      if (xor < (1 << 10)) { // level = 1
        if (depth == 1) {
          display1 = new Array(32)
          display1((oldIndex >>  5) & 31) = display0
          depth +=1
        }
        display0 = new Array(32)
      } else
      if (xor < (1 << 15)) { // level = 2
        if (depth == 2) {
          display2 = new Array(32)
          display2((oldIndex >> 10) & 31) = display1
          depth +=1
        }
        display1 = display2((newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        if (display1 == null) display1 = new Array(32)
        display0 = new Array(32)
      } else
      if (xor < (1 << 20)) { // level = 3
        if (depth == 3) {
          display3 = new Array(32)
          display3((oldIndex >> 15) & 31) = display2
          display2 = new Array(32)
          display1 = new Array(32)
          depth +=1
        }
        display2 = display3((newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        if (display2 == null) display2 = new Array(32)
        display1 = display2((newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        if (display1 == null) display1 = new Array(32)
        display0 = new Array(32)
      } else
      if (xor < (1 << 25)) { // level = 4
        if (depth == 4) {
          display4 = new Array(32)
          display4((oldIndex >> 20) & 31) = display3
          display3 = new Array(32)
          display2 = new Array(32)
          display1 = new Array(32)
          depth +=1
        }
        display3 = display4((newIndex >> 20) & 31).asInstanceOf[Array[AnyRef]]
        if (display3 == null) display3 = new Array(32)
        display2 = display3((newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        if (display2 == null) display2 = new Array(32)
        display1 = display2((newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        if (display1 == null) display1 = new Array(32)
        display0 = new Array(32)
      } else
      if (xor < (1 << 30)) { // level = 5
        if (depth == 5) {
          display5 = new Array(32)
          display5((oldIndex >>  25) & 31) = display4
          display4 = new Array(32)
          display3 = new Array(32)
          display2 = new Array(32)
          display1 = new Array(32)
          depth +=1
        }
        display4 = display5((newIndex >> 20) & 31).asInstanceOf[Array[AnyRef]]
        if (display4 == null) display4 = new Array(32)
        display3 = display4((newIndex >> 20) & 31).asInstanceOf[Array[AnyRef]]
        if (display3 == null) display3 = new Array(32)
        display2 = display3((newIndex >> 15) & 31).asInstanceOf[Array[AnyRef]]
        if (display2 == null) display2 = new Array(32)
        display1 = display2((newIndex >> 10) & 31).asInstanceOf[Array[AnyRef]]
        if (display1 == null) display1 = new Array(32)
        display0 = new Array(32)
      } else { // level = 6
        throw new IllegalArgumentException()
      }
    }


    /**
     * `O,,c,,(1)`, `O,,m,,(1)`
     */
    // requires structure is dirty and at pos oldIndex,
    // ensures structure is dirty and at pos newIndex and writable at level 0
    private[immutable] final def gotoFreshPosWritable1(oldIndex: Int, newIndex: Int, xor: Int): Unit = {
      stabilize(oldIndex)
      gotoFreshPosWritable0(oldIndex, newIndex, xor)
    }


    
    
    // DEBUG STUFF

    private[immutable] def debug(): Unit = {
      return
/*
      //println("DISPLAY 5: " + display5 + " ---> " + (if (display5 ne null) display5.map(x=> if (x eq null) "." else x + "->" +x.asInstanceOf[Array[AnyRef]].mkString("")).mkString(" ") else "null"))
      //println("DISPLAY 4: " + display4 + " ---> " + (if (display4 ne null) display4.map(x=> if (x eq null) "." else x + "->" +x.asInstanceOf[Array[AnyRef]].mkString("")).mkString(" ") else "null"))
      //println("DISPLAY 3: " + display3 + " ---> " + (if (display3 ne null) display3.map(x=> if (x eq null) "." else x + "->" +x.asInstanceOf[Array[AnyRef]].mkString("")).mkString(" ") else "null"))
      //println("DISPLAY 2: " + display2 + " ---> " + (if (display2 ne null) display2.map(x=> if (x eq null) "." else x + "->" +x.asInstanceOf[Array[AnyRef]].mkString("")).mkString(" ") else "null"))
      //println("DISPLAY 1: " + display1 + " ---> " + (if (display1 ne null) display1.map(x=> if (x eq null) "." else x + "->" +x.asInstanceOf[Array[AnyRef]].mkString("")).mkString(" ") else "null"))
      //println("DISPLAY 0: " + display0 + " ---> " + (if (display0 ne null) display0.map(x=> if (x eq null) "." else x.toString).mkString(" ") else "null"))
*/
      //println("DISPLAY 5: " + (if (display5 ne null) display5.map(x=> if (x eq null) "." else x.asInstanceOf[Array[AnyRef]].deepMkString("[","","]")).mkString(" ") else "null"))
      //println("DISPLAY 4: " + (if (display4 ne null) display4.map(x=> if (x eq null) "." else x.asInstanceOf[Array[AnyRef]].deepMkString("[","","]")).mkString(" ") else "null"))
      //println("DISPLAY 3: " + (if (display3 ne null) display3.map(x=> if (x eq null) "." else x.asInstanceOf[Array[AnyRef]].deepMkString("[","","]")).mkString(" ") else "null"))
      //println("DISPLAY 2: " + (if (display2 ne null) display2.map(x=> if (x eq null) "." else x.asInstanceOf[Array[AnyRef]].deepMkString("[","","]")).mkString(" ") else "null"))
      //println("DISPLAY 1: " + (if (display1 ne null) display1.map(x=> if (x eq null) "." else x.asInstanceOf[Array[AnyRef]].deepMkString("[","","]")).mkString(" ") else "null"))
      //println("DISPLAY 0: " + (if (display0 ne null) display0.map(x=> if (x eq null) "." else x.toString).mkString(" ") else "null"))
    }


}

