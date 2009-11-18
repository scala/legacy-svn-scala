/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy
*/

import java.io.InputStreamReader, java.util.regex._

object regexdna { 
   def main(args: Array[String]) = {

      var sequence = readFully()
      val initialLength = sequence.length

      // remove FASTA sequence descriptions and new-lines
      var m = Pattern.compile(">.*\n|\n").matcher(sequence)
      sequence = m.replaceAll("")
      val codeLength = sequence.length

      // regex match
      val variants = Array (
         "agggtaaa|tttaccct"
         ,"[cgt]gggtaaa|tttaccc[acg]"
         ,"a[act]ggtaaa|tttacc[agt]t"
         ,"ag[act]gtaaa|tttac[agt]ct"
         ,"agg[act]taaa|ttta[agt]cct"
         ,"aggg[acg]aaa|ttt[cgt]ccct"
         ,"agggt[cgt]aa|tt[acg]accct"
         ,"agggta[cgt]a|t[acg]taccct"
         ,"agggtaa[cgt]|[acg]ttaccct"
         )

      for (v <- variants){
         var count = 0
         m = Pattern.compile(v).matcher(sequence)
         while (m.find()) count = count + 1
         Console.println(v + " " + count)
      }

      // regex substitution
      val codes = Array (
             Pair("B", "(c|g|t)")
            ,Pair("D", "(a|g|t)")
            ,Pair("H", "(a|c|t)")
            ,Pair("K", "(g|t)")
            ,Pair("M", "(a|c)")
            ,Pair("N", "(a|c|g|t)")
            ,Pair("R", "(a|g)")
            ,Pair("S", "(c|g)")
            ,Pair("V", "(a|c|g)")
            ,Pair("W", "(a|t)")
            ,Pair("Y", "(c|t)")
         )

      for (iub <- codes){
         iub match { 
            case Pair(code,alternative) => 
               sequence = Pattern.compile(code).matcher(sequence).replaceAll(alternative)
         }
      }

      Console.println("\n" + initialLength + "\n" + codeLength + "\n" + sequence.length)
   } 


   def readFully() = {
      val blockSize = 10240
      val block = new Array[Char](blockSize)
      val buffer = new StringBuffer(blockSize)
      val r = new InputStreamReader(System.in)

      var charsRead = r.read(block, 0, blockSize)
      while (charsRead > -1){
         buffer.append(block,0,charsRead)
         charsRead = r.read(block, 0, blockSize)
      }

      r.close
      buffer.toString
   }
}
