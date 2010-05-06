/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

object nestedloop {
   def main(args: Array[String]) = {
      val n = toPositiveInt(args);
      var count = 0;

      for (val a <- Iterator.range(0,n); 
           val b <- Iterator.range(0,n); 
           val c <- Iterator.range(0,n); 
           val d <- Iterator.range(0,n); 
           val e <- Iterator.range(0,n); 
           val f <- Iterator.range(0,n)
         ) 
         count = count + 1;

      Console println(count);
   }


   private def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }
}
