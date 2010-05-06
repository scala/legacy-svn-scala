/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

import scala.collection.mutable.HashMap;

object hash {
   def main(args: Array[String]) = {
      val n = toPositiveInt(args);
      var count = 0;
      val table = new HashMap[String,Int]();

      for (val i <- Iterator.range(1,n+1)) 
         table += (Integer.toString(i, 16) -> i)

      for (val i <- Iterator.range(1,n+1)) 
         if (table contains Integer.toString(i, 10)) 
            count = count + 1;

      Console println(count);
   }


   private def toPositiveInt(s: Array[String]) = {
      val i = 
        try { Integer.parseInt(s(0)); } 
        catch { case e: Exception => 1 }

      if (i>0) i; else 1;
   }

}



