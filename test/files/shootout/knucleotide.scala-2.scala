/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy
*/

import java.io._
import scala.collection.mutable.HashMap
import scala.Console

object knucleotide { 

   var sequence: String = _

   def main(args: Array[String]) = {
      val r = new BufferedReader(new InputStreamReader(System.in))
      findSequence(">THREE", r)
      sequence = nextSequence(r)
      r.close

      writeFrequencies(1)
      writeFrequencies(2)

      writeCount("GGT")
      writeCount("GGTA")
      writeCount("GGTATT")
      writeCount("GGTATTTTAATT")
      writeCount("GGTATTTTAATTTATAGT")
   } 



   def findSequence(id: String, r: BufferedReader): Unit = {
      var line = r.readLine
      while (line != null) {
         val c = line.charAt(0)
         if (c == '>' && line.startsWith(id)) return
         line = r.readLine
      }
   } 


   def nextSequence(r: BufferedReader): String = {
      val b = new StringBuffer()

      var line = r.readLine
      while (line != null) {
         val c = line.charAt(0)
         if (c == '>') { 
            return b.toString
         } else { 
            if (c != ';') b.append(line.toUpperCase) 
         }
         line = r.readLine
      }
      b.toString
   }

   
   class Counter(_value: int){ 
      var value = _value 
      def ++() = { value = value + 1 }
   }


   def generateFrequencies(length: int) = {
      val d: HashMap[String,Counter] = new HashMap()
   
      def kFrequency(offset: int, j: int) = {
         val n = sequence.length - j + 1
         var i = offset
         while (i < n){
            val k = sequence.substring(i,i+j)
            d.get(k) match {
               case None => d += k -> new Counter(1)
               case Some(c) => c++
            }
            i = i + j
         }
      }

      for (val o <- Iterator.range(0,length)) kFrequency(o,length)
      d
   } 


   def writeFrequencies(j: int) = {
      val d = generateFrequencies(j)
      val n = sequence.length - j + 1.0
      val sortedValues = d.toList.sort(
         (a,b) => if (a._2.value == b._2.value) a._1 > b._1 
                  else a._2.value > b._2.value )

      for (val a <- sortedValues) 
         Console.printf("{0} {1,number,0.000}\n", a._1, a._2.value / n * 100.0)

      Console.println
   }


   def writeCount(fragment: String) = {
      val d = generateFrequencies(fragment.length)
      val count = if (d.contains(fragment)) d(fragment).value else 0
      Console.println(count + "\t" + fragment)
   }

}
