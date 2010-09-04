/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2005-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.collection
package immutable

/** A base class containing the implementations for `TreeMaps` and `TreeSets`.
 *  
 *  @since 2.3
 */
@serializable @SerialVersionUID(8691885935445612921L)
abstract class RedBlack[A] {

  def isSmaller(x: A, y: A): Boolean

  private def blacken[B](t: Tree[B]): Tree[B] = t match {
    case RedTree(k, v, l, r) => BlackTree(k, v, l, r)
    case t => t
  }
  private def mkTree[B](isBlack: Boolean, k: A, v: B, l: Tree[B], r: Tree[B]) = 
    if (isBlack) BlackTree(k, v, l, r) else RedTree(k, v, l, r)
    
  @serializable
  abstract class Tree[+B] {
    def isEmpty: Boolean
    def isBlack: Boolean
    def lookup(x: A): Tree[B]
    def update[B1 >: B](k: A, v: B1): Tree[B1] = blacken(upd(k, v))
    def delete(k: A): Tree[B] = blacken(del(k))
    def foreach[U](f: (A, B) =>  U)
    @deprecated("use `foreach' instead")
    def visit[T](input: T)(f: (T, A, B) => (Boolean, T)): (Boolean, T)
    def toStream: Stream[(A,B)]
    def iterator: Iterator[(A, B)]
    @deprecated("use `iterator' instead") def elements = iterator
    def upd[B1 >: B](k: A, v: B1): Tree[B1]
    def del(k: A): Tree[B]
    def smallest: NonEmpty[B]
    def range(from: Option[A], until: Option[A]): Tree[B]
    def first : A
    def last : A
    def count : Int
  }
  @serializable
  abstract class NonEmpty[+B] extends Tree[B] {
    def isEmpty = false
    def key: A
    def value: B
    def left: Tree[B]
    def right: Tree[B]
    def lookup(k: A): Tree[B] = 
      if (isSmaller(k, key)) left.lookup(k)
      else if (isSmaller(key, k)) right.lookup(k)
      else this
    def upd[B1 >: B](k: A, v: B1): Tree[B1] = {
      def balanceLeft(isBlack: Boolean, z: A, zv: B, l: Tree[B1], d: Tree[B1]) = l match {
        case RedTree(y, yv, RedTree(x, xv, a, b), c) => 
          RedTree(y, yv, BlackTree(x, xv, a, b), BlackTree(z, zv, c, d))
        case RedTree(x, xv, a, RedTree(y, yv, b, c)) =>
          RedTree(y, yv, BlackTree(x, xv, a, b), BlackTree(z, zv, c, d))
        case _ =>
          mkTree(isBlack, z, zv, l, d)
      }
      def balanceRight(isBlack: Boolean, x: A, xv: B, a: Tree[B1], r: Tree[B1]) = r match {
        case RedTree(z, zv, RedTree(y, yv, b, c), d) => 
          RedTree(y, yv, BlackTree(x, xv, a, b), BlackTree(z, zv, c, d))
        case RedTree(y, yv, b, RedTree(z, zv, c, d)) =>
          RedTree(y, yv, BlackTree(x, xv, a, b), BlackTree(z, zv, c, d))
        case _ =>
          mkTree(isBlack, x, xv, a, r)
      }
      if (isSmaller(k, key)) balanceLeft(isBlack, key, value, left.upd(k, v), right)
      else if (isSmaller(key, k)) balanceRight(isBlack, key, value, left, right.upd(k, v))
      else mkTree(isBlack, k, v, left, right)
    }
    
    // Based on Stefan Kahrs' Haskell version of Okasaki's Red&Black Trees
    // http://www.cse.unsw.edu.au/~dons/data/RedBlackTree.html
    
    // fixes red violations while maintaining the black invariant
    // the effect is always to move red nodes up the tree (towards the root), 
    // and also to reduce the number of red nodes
    private[this] def balance(x: A, xv: B, tl: Tree[B], tr: Tree[B]) = (tl, tr) match {
      case (RedTree(y, yv, a, b), RedTree(z, zv, c, d)) =>
        RedTree(x, xv, BlackTree(y, yv, a, b), BlackTree(z, zv, c, d))
      case (RedTree(y, yv, RedTree(z, zv, a, b), c), d) =>
        RedTree(y, yv, BlackTree(z, zv, a, b), BlackTree(x, xv, c, d))
      case (RedTree(y, yv, a, RedTree(z, zv, b, c)), d) =>
        RedTree(z, zv, BlackTree(y, yv, a, b), BlackTree(x, xv, c, d))
      case (a, RedTree(y, yv, b, RedTree(z, zv, c, d))) =>
        RedTree(y, yv, BlackTree(x, xv, a, b), BlackTree(z, zv, c, d))
      case (a, RedTree(y, yv, RedTree(z, zv, b, c), d)) =>
        RedTree(z, zv, BlackTree(x, xv, a, b), BlackTree(y, yv, c, d))
      case (a, b) => 
        BlackTree(x, xv, a, b)
    }
    
    // Invariance check
    private[this] def subl(t: Tree[B]) = t match {
      case BlackTree(x, xv, a, b) => RedTree(x, xv, a, b)
      case _ => error("Defect: invariance violation; expected black, got "+t)
    }
    
    // balance a tree whose depth on the left side might be smaller
    private[this] def balLeft(x: A, xv: B, tl: Tree[B], tr: Tree[B]) = (tl, tr) match {
      case (RedTree(y, yv, a, b), c) => 
        RedTree(x, xv, BlackTree(y, yv, a, b), c)
      case (bl, BlackTree(y, yv, a, b)) => 
        balance(x, xv, bl, RedTree(y, yv, a, b))
      case (bl, RedTree(y, yv, BlackTree(z, zv, a, b), c)) => 
        RedTree(z, zv, BlackTree(x, xv, bl, a), balance(y, yv, b, subl(c)))
      case _ => error("Defect: invariance violation at "+right)
    }
    
    // balance a tree whose depth on the right side might be smaller
    private[this] def balRight(x: A, xv: B, tl: Tree[B], tr: Tree[B]) = (tl, tr) match {
      case (a, RedTree(y, yv, b, c)) =>
        RedTree(x, xv, a, BlackTree(y, yv, b, c))
      case (BlackTree(y, yv, a, b), bl) =>
        balance(x, xv, RedTree(y, yv, a, b), bl)
      case (RedTree(y, yv, a, BlackTree(z, zv, b, c)), bl) =>
        RedTree(z, zv, balance(y, yv, subl(a), b), BlackTree(x, xv, c, bl))
      case _ => error("Defect: invariance violation at "+left)
    }
    
    // append the left and right trees
    // we know the max of the left is smaller than the min of the right
    // and we know that they have the same black-depth
    private[this] def append(tl: Tree[B], tr: Tree[B]): Tree[B] = (tl, tr) match {
      case (Empty, t) => t
      case (t, Empty) => t
      case (RedTree(x, xv, a, b), RedTree(y, yv, c, d)) =>
        append(b, c) match {
          case RedTree(z, zv, bb, cc) => RedTree(z, zv, RedTree(x, xv, a, bb), RedTree(y, yv, cc, d))
          case bc => RedTree(x, xv, a, RedTree(y, yv, bc, d))
        }
      case (BlackTree(x, xv, a, b), BlackTree(y, yv, c, d)) =>
        append(b, c) match {
          case RedTree(z, zv, bb, cc) => RedTree(z, zv, BlackTree(x, xv, a, bb), BlackTree(y, yv, cc, d))
          case bc => balLeft(x, xv, a, BlackTree(y, yv, bc, d))
        }
      case (a, RedTree(x, xv, b, c)) => RedTree(x, xv, append(a, b), c)
      case (RedTree(x, xv, a, b), c) => RedTree(x, xv, a, append(b, c))
    }
    
    def del(k: A): Tree[B] = {
      def delLeft = left match {
        case _: BlackTree[_] => balLeft(key, value, left.del(k), right)
        case _ => RedTree(key, value, left.del(k), right)
      }
      def delRight = right match {
        case _: BlackTree[_] => balRight(key, value, left, right.del(k))
        case _ => RedTree(key, value, left, right.del(k))
      }
      // RedBlack is neither A : Ordering[A], nor A <% Ordered[A]
      k match {
        case _ if isSmaller(k, key) => delLeft
        case _ if isSmaller(key, k) => delRight
        case _ => append(left, right)
      }
    }

    def smallest: NonEmpty[B] = if (left.isEmpty) this else left.smallest

    def toStream: Stream[(A,B)] = 
      left.toStream ++ Stream((key,value)) ++ right.toStream

    def iterator: Iterator[(A, B)] = 
      left.iterator ++ Iterator.single(Pair(key, value)) ++ right.iterator

    def foreach[U](f: (A, B) => U) {
      left foreach f
      f(key, value)
      right foreach f
    }

    @deprecated("use `foreach' instead")
    def visit[T](input: T)(f: (T,A,B) => (Boolean, T)): (Boolean, T) = {
      val left = this.left.visit(input)(f)
      if (!left._1) return left
      val middle = f(left._2, key, value)
      if (!middle._1) return middle
      return this.right.visit(middle._2)(f)
    }
   override def range(from: Option[A], until: Option[A]): Tree[B] = {
      if (from == None && until == None) return this
      if (from != None && isSmaller(key, from.get)) return right.range(from, until);
      if (until != None && (isSmaller(until.get,key) || !isSmaller(key,until.get)))
        return left.range(from, until);
      val newLeft = left.range(from, None)
      val newRight = right.range(None, until)
      if ((newLeft eq left) && (newRight eq right)) this
      else if (newLeft eq Empty) newRight.upd(key, value);
      else if (newRight eq Empty) newLeft.upd(key, value);
      else rebalance(newLeft, newRight)
    }
    
    // The zipper returned might have been traversed left-most (always the left child)
    // or right-most (always the right child). Left trees are traversed right-most,
    // and right trees are traversed leftmost.
    
    // Returns the zipper for the side with deepest black nodes depth, a flag 
    // indicating whether the trees were unbalanced at all, and a flag indicating
    // whether the zipper was traversed left-most or right-most.
    
    // If the trees were balanced, returns an empty zipper
    private[this] def compareDepth(left: Tree[B], right: Tree[B]): 
                                  (List[NonEmpty[B]], Boolean, Boolean) = {
      def unzip(zipper: List[NonEmpty[B]], leftMost: Boolean): List[NonEmpty[B]] = {
        val next = if (leftMost) zipper.head.left else zipper.head.right
        next match {
          case node: NonEmpty[B] => unzip(node :: zipper, leftMost)
          case Empty => zipper
        }
      }
    
      def unzipBoth(left: Tree[B],
                    right: Tree[B],
                    leftZipper: List[NonEmpty[B]],
                    rightZipper: List[NonEmpty[B]]): 
                   (List[NonEmpty[B]], Boolean, Boolean) = (left, right) match {
        case (l: BlackTree[B], r: BlackTree[B]) =>
          unzipBoth(l.right, r.left, l :: leftZipper, r :: rightZipper)
        case (l: RedTree[B], r: RedTree[B]) =>
          unzipBoth(l.right, r.left, l :: leftZipper, r :: rightZipper)
        case (_, r: RedTree[B]) =>
          unzipBoth(left, r.left, leftZipper, r :: rightZipper)
        case (l: RedTree[B], _) =>
          unzipBoth(l.right, right, l :: leftZipper, rightZipper)
        case (Empty, Empty) =>
          (Nil, true, false)
        case (Empty, r: BlackTree[B]) =>
          val leftMost = true
          (unzip(r :: rightZipper, leftMost), false, leftMost)
        case (l: BlackTree[B], Empty) =>
          val leftMost = false
          (unzip(l :: leftZipper, leftMost), false, leftMost)
      }
      
      unzipBoth(left, right, Nil, Nil)
    }
      
    private[this] def rebalance(left: Tree[B], right: Tree[B]) = {
      def computeDepth(t: Tree[B]) = Iterator.iterate(t) {
        case ne: NonEmpty[B] => ne.left
        case other => other
      }.takeWhile(!_.isEmpty).filter(_.isBlack).size
      
      def  findDepth(zipper: List[NonEmpty[B]], depth: Int): List[NonEmpty[B]] = 
        zipper match {
          case (_: BlackTree[B]) :: tail =>
            if (depth == 1) zipper else findDepth(tail, depth - 1)
          case _ :: tail => findDepth(tail, depth)
          case Nil => error("Defect: unexpected empty zipper while computing range")
        }
      
      val (zipper, levelled, leftMost) = compareDepth(left, right)
      
      if (levelled) {
        BlackTree(key, value, left, right)
      } else {
        val smaller = if (leftMost) left else right
        val newTree = smaller.update(key, value)
        val smallerDepth = computeDepth(newTree)
        val zipFrom = findDepth(zipper, smallerDepth)
        val union = if (leftMost) append(newTree, zipFrom.head) else append(zipFrom.head, newTree)
        val zippedTree = zipFrom.tail.foldLeft(union) {
          case (tree: BlackTree[B], node) =>
            if (leftMost)
              balLeft(node.key, node.value, tree, node.right)
            else
              balRight(node.key, node.value, node.left, tree)
          case (tree: RedTree[B], node) =>
            if (leftMost) 
              RedTree(node.key, node.value, tree, node.right)
            else
              RedTree(node.key, node.value, node.left, tree)
        }
        blacken(zippedTree)
      }
    }
          
    def first = if (left .isEmpty) key else left.first
    def last  = if (right.isEmpty) key else right.last
    def count = 1 + left.count + right.count
  }
  @serializable
  case object Empty extends Tree[Nothing] {
    def isEmpty = true
    def isBlack = true
    def lookup(k: A): Tree[Nothing] = this
    def upd[B](k: A, v: B): Tree[B] = RedTree(k, v, Empty, Empty)
    def del(k: A): Tree[Nothing] = this
    def smallest: NonEmpty[Nothing] = throw new NoSuchElementException("empty map")
    def iterator: Iterator[(A, Nothing)] = Iterator.empty
    def toStream: Stream[(A,Nothing)] = Stream.empty

    def foreach[U](f: (A, Nothing) => U) {}

    @deprecated("use `foreach' instead")
    def visit[T](input: T)(f: (T, A, Nothing) => (Boolean, T)) = (true, input)

    def range(from: Option[A], until: Option[A]) = this
    def first = throw new NoSuchElementException("empty map")
    def last = throw new NoSuchElementException("empty map")
    def count = 0
  }
  @serializable
  case class RedTree[+B](override val key: A,
                         override val value: B,
                         override val left: Tree[B],
                         override val right: Tree[B]) extends NonEmpty[B] {
    def isBlack = false
  }
  @serializable
  case class BlackTree[+B](override val key: A,
                           override val value: B,
                           override val left: Tree[B], 
                           override val right: Tree[B]) extends NonEmpty[B] {
    def isBlack = true
  }
}

