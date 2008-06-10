/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

object fannkuch
{
  def main(args: Array[String]) {
    val n = Integer.parseInt(args(0))
    var maxFlips = 0
    var permN = 0
    var k: Int = 0
    var temp: Int = 0
    var first: Int = 0
    var flips: Int = 0
    var perm0: Array[Int] = new Array[Int](n)
    var perm: Array[Int] = new Array[Int](n)
    var rot: Array[Int] = new Array[Int](n)

    while (k < n) { perm(k) = k + 1; rot(k) = 0; k = k + 1; }
    while (rot(n - 1) < n) {
      if (permN < 30) {
	k = 0
	while (k < n) {
	  Console.print(perm(k).toString())
	  k = k + 1
	}
	Console.println
	permN = permN + 1
      }

      flips = 0
      k = 0
      while (k < n) { perm0(k) = perm(k); k = k + 1; }
      first = perm0(0)
      while(first != 1) {
	k = 0
	while (k < first / 2) {
	  temp = perm0(k); perm0(k) = perm0(first - 1 - k); perm0(first - 1 - k) = temp;
	  k = k + 1
	}
	first = perm0(0)
	flips = flips + 1
      }

      if (flips > maxFlips) maxFlips = flips

      temp = perm(0); perm(0) = perm(1); perm(1) = temp;
      rot(1) = rot(1) + 1
      var j = 1
      while (j < n - 1 && rot(j) > j) {
	rot(j) = 0
	j = j + 1

	k = 0
  	while (k < j) {
	  temp = perm(k); perm(k) = perm(k + 1); perm(k + 1) = temp;
	  k = k + 1
	}
	rot(j) = rot(j) + 1
      }
    }

    Console.println("Pfannkuchen(" + n + ") = " + maxFlips)
  }
}

