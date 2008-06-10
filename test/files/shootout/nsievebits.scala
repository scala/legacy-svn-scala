/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy
*/

import scala.collection.mutable.BitSet

object nsievebits { 

   def nsieve(m: Int): Int = {
      val notPrime = new BitSet(m+1)
      notPrime += 1

      var i = 2
      while (i <= m){
         if (!notPrime.contains(i)){
            var k = i+i
            while (k <= m){ 
               if (!notPrime.contains(k)) notPrime += k 
               k = k+i 
            }
         }

         i = i+1
      }
      m - notPrime.size
   }


   def main(args: Array[String]) {

      def printPrimes(m: Int) = {

         def pad(i: Int, width: Int) = {
            val s = i.toString
            List.range(0, width - s.length)
               .map((i) => " ") .foldLeft("")((a,b) => a+b) + s 
         }

         Console.println("Primes up to " +  pad(m,8) + pad(nsieve(m),9))
      }

      val n = Integer.parseInt(args(0))
      printPrimes( (1<<n  )*10000 )
      printPrimes( (1<<n-1)*10000 )
      printPrimes( (1<<n-2)*10000 )
   } 
}
