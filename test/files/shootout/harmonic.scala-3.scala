/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
*/
object Test extends Application {
  for(val n <- List(6000000,8000000,10000000)) harmonic.main(Array(n.toString)) 
} 
object harmonic {
   def main(args: Array[String]) = {
      val n = Integer.parseInt(args(0));
      var partialSum = 0.0;
      var i = 1;

      while (i < n){ partialSum = partialSum + 1.0/i; i = i + 1; }
      Console.printf("{0,number,#.000000000}\n")(partialSum);
   }
}
