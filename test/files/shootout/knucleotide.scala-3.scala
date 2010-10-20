/* The Computer Language Benchmarks Game
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy
   fixed by Iulian Dragos
   fixed by Meiko Rachimow
*/

import java.io._ 
import scala.collection.mutable.{HashTable, HashEntry}
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

   def generateFrequencies(length: Int) = {
      val bag: HashBag[String] = new HashBag()

      def kFrequency(offset: Int, j: Int) = {
         val n = sequence.length - j + 1
         var i = offset
         while (i < n){ bag += sequence.substring(i,i+j); i = i+j }
      }

      for (val o <- Iterator.range(0,length)) kFrequency(o,length)
      bag
   }

   def writeFrequencies(j: Int) = {
      val bag = generateFrequencies(j)
      val n = sequence.length - j + 1.0
      val sortedValues = bag.iterator.toList.sort(
         (a,b) => if (a.value == b.value) a.key > b.key
                  else a.value > b.value )

      for (val a <- sortedValues)
         Console.printf("%s %.3f\n", a.key, a.value / n * 100.0)

      Console.println
   }

   def writeCount(fragment: String) = {
      val bag = generateFrequencies(fragment.length)
      Console.println( bag.findOccurrences(fragment) + "\t" + fragment )
   }
}


class HashBag[A] extends HashTable[A, Counter[A]] {
   protected type Entry = Counter[A]
   protected def entryKey(e: Entry) = e.key
   def iterator = entries

   def +=(elem: A): Unit = {
      var bucket = table(index(elemHashCode(elem))).asInstanceOf[Entry]
      while (bucket ne null) {
         if (elemEquals(entryKey(bucket), elem)){
            bucket.inc
            return
         }
         bucket = bucket.next
      }
      addEntry(new Entry(elem, 1))
   }

   def findOccurrences(elem: A): Int = {
      var bucket = table(index(elemHashCode(elem))).asInstanceOf[Entry]
      while (bucket ne null) {
         if (elemEquals(entryKey(bucket), elem)){
            return bucket.value
         }
         bucket = bucket.next
      }
      return 0
   }

/*
   def -=(elem: A): Unit = {
      var bucket = table(index(elemHashCode(elem)))
      while (!bucket.isEmpty) {
         if (elemEquals(entryKey(bucket.head), elem)){
            bucket.head.dec
            if (bucket.head.value == 0) removeEntry(elem)
            return
         }
         bucket = bucket.tail
      }
  }
*/
}

protected class Counter[A](k: A, v: Int) extends AnyRef with HashEntry[A, Counter[A]] {
   val key = k
   var value = v
   def inc = { value = value + 1 }
   def dec = { value = value - 1 }
}

