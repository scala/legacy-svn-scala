/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy (Scala novice)
   modified for Scala 2.x by Anthony Borla
*/
object Test extends Application {
  for(val n <- List(1,50,100,150,200)) hello.main(Array(n.toString)) 
}  
object hello extends Application {
  Console.println("hello world")
}

