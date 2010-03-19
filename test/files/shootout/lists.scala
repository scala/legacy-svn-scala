/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

import collection.mutable.ListBuffer;

object lists {
   def main(args: Array[String]) = {
      var n = toPositiveInt(args);
      val nSize = 10;
      var L1Count = 0;
      var i = 0;

      while (n > 0) {
         var L1 = new ListBuffer[Int]();
         i=0; while (i < nSize){ L1 += i; i=i+1; } 
         var L2 = L1.clone().asInstanceOf[ListBuffer[Int]];
         var L3 = new ListBuffer[Int]();
         while (L2.length > 0) L3 += L2.remove(0); 
         i = L3.length;
         while (i > 0){ i=i-1; L2 += L3.remove(i); }

         Console println(L2 length);
         n = n - 1;
      }
   }

   private def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }
}
