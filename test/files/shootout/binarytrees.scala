/*
  The Computer Language Shootout
  http://shootout.alioth.debian.org/

  - tree: disjoint union type
  - loop: "for" loop over iterator range

  Contributed by Kannan Goundan
  De-optimized by Isaac Gouy
*/

object binarytrees {

  abstract class Tree;
  case class Node(i: Int, left: Tree, right: Tree) extends Tree
  case class Empty() extends Tree

  def check(tree: Tree) : Int = tree match {
    case Node(i, left, right) => i + check(left) - check(right)
    case Empty() => 0
  }

  def make(i: Int, depth: Int) : Tree = depth match {
/*  case 0 => Empty() */
    case 0 => Node(i, Empty(), Empty())
    case _ => Node(i, make((2*i)-1, depth-1), make(2*i, depth-1))
  }

  def main(args: Array[String]) = {
    val n = try { Integer.parseInt(args(0)) } catch { case _ => 1 }
    val minDepth = 4
    val maxDepth = Math.max(minDepth+2, n)

    print("stretch tree", maxDepth+1, check(make(0, maxDepth+1)))

    val longLived = make(0, maxDepth)

    for (val depth <- Iterator.range(minDepth, maxDepth+1, 2)) {
      val iterations = 1 << (maxDepth - depth + minDepth)

      var sum = 0
      for (val i <- Iterator.range(1, iterations+1))
        sum = sum + check(make(i, depth)) + check(make(-i, depth))

      print(iterations*2 + "\t trees", depth, sum)
    }

    print("long lived tree", maxDepth, check(longLived))
  }

  def print(name: String, depth: Int, check: Int) =
    Console.println(name + " of depth " + depth + "\t check: " + check)
}
