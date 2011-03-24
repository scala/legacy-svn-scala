/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.collection.parallel.immutable



import scala.collection.immutable.Range
import scala.collection.parallel.Combiner
import scala.collection.generic.CanCombineFrom
import scala.collection.parallel.ParIterableIterator



/** Parallel ranges.
 *  
 *  $paralleliterableinfo
 *  
 *  $sideeffects
 *  
 *  @param range    the sequential range this parallel range was obtained from
 *  
 *  @author Aleksandar Prokopec
 *  @since 2.9
 *  
 *  @define Coll immutable.ParRange
 *  @define coll immutable parallel range
 */
@SerialVersionUID(1L)
class ParRange(val range: Range)
extends ParSeq[Int]
   with Serializable
{
self =>
  
  override def seq = range
  
  @inline final def length = range.length
  
  @inline final def apply(idx: Int) = range.apply(idx);
  
  def parallelIterator = new ParRangeIterator with SCPI
  
  type SCPI = SignalContextPassingIterator[ParRangeIterator]
  
  class ParRangeIterator(range: Range = self.range)
  extends ParIterator {
  me: SignalContextPassingIterator[ParRangeIterator] =>
    override def toString = "ParRangeIterator(over: " + range + ")"
    private var ind = 0
    private val len = range.length
    
    final def remaining = len - ind
    
    final def hasNext = ind < len
    
    final def next = if (hasNext) {
      val r = range.apply(ind)
      ind += 1
      r
    } else Iterator.empty.next
    
    private def rangeleft = range.drop(ind)
    
    def dup = new ParRangeIterator(rangeleft) with SCPI
    
    def split = {
      val rleft = rangeleft
      val elemleft = rleft.length
      if (elemleft < 2) Seq(new ParRangeIterator(rleft) with SCPI)
      else Seq(
        new ParRangeIterator(rleft.take(elemleft / 2)) with SCPI,
        new ParRangeIterator(rleft.drop(elemleft / 2)) with SCPI
      )
    }
    
    def psplit(sizes: Int*) = {
      var rleft = rangeleft
      for (sz <- sizes) yield {
        val fronttaken = rleft.take(sz)
        rleft = rleft.drop(sz)
        new ParRangeIterator(fronttaken) with SCPI
      }
    }
    
    /* accessors */
    
    override def foreach[U](f: Int => U): Unit = {
      rangeleft.foreach(f)
      ind = len
    }
    
    override def reduce[U >: Int](op: (U, U) => U): U = {
      val r = rangeleft.reduceLeft(op)
      ind = len
      r
    }
    
    /* transformers */
    
    override def map2combiner[S, That](f: Int => S, cb: Combiner[S, That]): Combiner[S, That] = {
      while (hasNext) {
        cb += f(next)
      }
      cb
    }
  }
  
}


object ParRange {
  def apply(start: Int, end: Int, step: Int, inclusive: Boolean) = new ParRange(
    if (inclusive) new Range.Inclusive(start, end, step)
    else new Range(start, end, step)
  )
}





