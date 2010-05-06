/* The Computer Language Shootout
   http://shootout.alioth.debian.org/

   contributed by Kannan Goundan
   modified by Isaac Gouy
*/

object binarytrees {
   def main(args: Array[String]) = {
      val n = try { Integer.parseInt(args(0)) } catch { case _ => 1 }
      val minDepth = 4
      val maxDepth = Math.max(minDepth+2,n)

      print("stretch tree", maxDepth+1, new Tree(0,maxDepth+1).isum)

      val longLivedTree = new Tree(0,maxDepth)

      var depth = minDepth
      while (depth <= maxDepth) {
         val iterations = 1 << (maxDepth - depth + minDepth)

         var sum = 0
         for (val i <- Iterator.range(1,iterations+1))
            sum = sum + new Tree(i,depth).isum + new Tree(-i,depth).isum

         print(iterations*2 + "\t trees", depth, sum)
         depth = depth + 2
      }
      print("long lived tree", maxDepth, longLivedTree.isum)
   }

   def print(name: String, depth: Int, check: Int) =
      Console.println(name + " of depth " + depth + "\t check: " + check)
}


final class Tree(_i: Int, depth: Int) {
    private val i = _i
    private val left = if (depth == 0) null else new Tree(2*i -1, depth-1)
    private val right = if (depth == 0) null else new Tree(2*i, depth-1)

    def isum : Int = if (left == null) i else i + left.isum - right.isum
}
