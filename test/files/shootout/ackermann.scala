/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

object ackermann {
   def main(args: Array[String]) = {
      val n = toPositiveInt(args);
      Console println("Ack(3," + n + "): " + ack(3,n));
   }

   def ack(m: Int, n: Int): Int = 
      if (m == 0) n + 1;
      else if (n == 0) ack(m-1, 1);
      else ack(m-1, ack(m, n-1));

   private def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }
}
