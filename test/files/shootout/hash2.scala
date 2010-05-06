/* The Computer Language Shootout 
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/

import scala.collection.mutable.HashMap;

object hash2 {
   def main(args: Array[String]) = {

      def printValue[A,B](table: HashMap[A,Cell[B]], key: A) = 
         table get(key) match {
            case Some(c) => Console print(c value);
            case None => Console print(None)
      }

      var n = toPositiveInt(args);
      var nKeys = 10000;

      val table1 = new HashMap[String,Cell[Int]]();
      val table2 = new HashMap[String,Cell[Int]]();

      for (val i <- Iterator.range(0,nKeys)) 
         table1 += (("foo_" + i) -> new Cell(i));


      while (n>0) {
         for (val each <- table1.iterator){
            val key = each._1;
            val c1 = each._2;

            table2 get(key) match {
               case Some(c2) => 
                  c2.value = c2.value + c1.value;
               case None => 
                  table2 += (key -> new Cell(c1.value));
            }
         }
         n = n-1;
      }

      printValue(table1,"foo_1");    Console print(" ");
      printValue(table1,"foo_9999"); Console print(" ");
      printValue(table2,"foo_1");    Console print(" ");
      printValue(table2,"foo_9999"); Console print("\n");

   }


   private def toPositiveInt(s: Array[String]) = {
      val i = 
         try { Integer.parseInt(s(0)); } 
         catch { case _ => 1 }
      if (i>0) i; else 1;
   }
}


private class Cell[T](v: T) extends Object { 
   var value: T = v; 
}


