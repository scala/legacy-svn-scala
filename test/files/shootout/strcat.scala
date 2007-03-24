/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/
object Test extends Application {
  for(val n <- List(10000,20000,30000,40000)) strcat.main(Array(n.toString)) 
} 
object strcat {
   def main(args: Array[String]) = {
      var n = toPositiveInt(args);
      val s = "hello\n";
      val b = new StringBuffer(32);

      while (n>0) { b.append(s); n=n-1; }

      Console.println( b.length() );
   }


   private def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }
}



