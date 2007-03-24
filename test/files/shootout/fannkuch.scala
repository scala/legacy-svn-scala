/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

object fannkuch
{
  var permN : int = 0
  var maxFlips : int = 0

  def flips(l: List[int]): int = l match {
    case 1 :: ls => 0
    case n :: ls => flips((l take n reverse) ::: (l drop n)) + 1
  }

  def rotateLeft(l: List[int]) = 
    l match { case List() => List() case x :: xs => xs ::: List(x) }

  def printPerm(perm: List[int]) = 
    { perm foreach(x => Console.print(x.toString())); Console.println; }

  def processPerm(perm: List[int]) = {
    val f = flips(perm)
    if (f > maxFlips) maxFlips = f
    if (permN < 30) { printPerm(perm); permN = permN + 1; }
  }

  def permutations(l: List[int], n: int, i: int): unit = {
    if (i < n) {
      if (n == 1)
	processPerm(l)
      else { 
	permutations(l, n - 1, 0)
	permutations(rotateLeft(l take n) ::: (l drop n), n, i + 1)
      }
    }
  }

  def main(args: Array[String]) = 
  {
    val n = Integer.parseInt(args(0))

    permutations(List.range(1, n + 1), n, 0)
    Console.println("Pfannkuchen(" + n + ") = " + maxFlips)
  }
}
