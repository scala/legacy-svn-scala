/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

object ary3 {
   def main(args: Array[String]) = {
      val n = toPositiveInt(args);
      var j = 0;

      val x = new Array[Int](n);
      for (val i <- Iterator.range(0,n)) x(i)=i+1;

      val y = new Array[Int](n);
      for (val j <- Iterator.range(0,1000);
           val i <- Iterator.range(0,n))
         y(i)=y(i)+x(i);

      Console.println(y(0) + " " +  y(n-1)); 
   }


   private def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }
}



