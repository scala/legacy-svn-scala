/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

object except {

   var Lo = 0;
   var Hi = 0;

   def main(args: Array[String]) = {
      val n = toPositiveInt(args);

      for (val i <- Iterator.range(0,n)) 
         someFunction(i);

      Console print("Exceptions: HI=" + Hi);
      Console println(" / LO=" + Lo);
   }


   def blowup(n: Int) = {
      if ((n % 2) == 0)
         throw new LoException();
      else 
         throw new HiException();   }


   def loFunction(n: Int) = {
      try { blowup(n); } 
      catch { case _: LoException => Lo = Lo + 1; }
   }


   def hiFunction(n: Int) = {
      try { loFunction(n); } 
      catch { case _: HiException => Hi = Hi + 1; }
   }


   def someFunction(n: Int) = {
      try { hiFunction(n); } 
      catch { case e: Exception =>  
         Console println("We shouldn't get here: " + e);
      }
   }


   def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }

}

private class LoException extends Exception {}
private class HiException extends Exception {}
