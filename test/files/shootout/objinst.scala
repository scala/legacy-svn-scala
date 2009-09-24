/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

object objinst {
   def main(args: Array[String]) = {
      var n = toPositiveInt(args);

      var toggle = new Toggle(true);
      for (val i <- Iterator.range(0,5)) 
         Console println( toggle.activate.value );
      for (val i <- Iterator.range(0,n)) 
         toggle = new Toggle(true);

      Console print("\n");

      var ntoggle = new NToggle(true,3);
      for (val i <- Iterator.range(0,8)) 
         Console println( ntoggle.activate.value );   
      for (val i <- Iterator.range(0,n)) 
         ntoggle = new NToggle(true,3);  
   }


   private def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }
}


private class Toggle(b: Boolean) {
   var state = b;

   def value = state;

   def activate = {
      state = !state;
      this 
   }
}


private class NToggle(b: Boolean, trigger: Int) 
extends Toggle(b) {

   val toggleTrigger = trigger;
   var count = 0;

   override def activate = {
      count = count + 1;
      if (count >= toggleTrigger) {
         state = !state;
         count = 0;
      }
      this
   }
}
