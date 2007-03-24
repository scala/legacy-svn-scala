/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */
object Test extends Application {
  for(val n <- List(400,700,1000,8000)) {
    System.setIn(new java.io.FileInputStream(System.getProperty("scalatest.cwd")+"/sumcol-input.txt"))
    sumcol.main(Array(n.toString)) 
  }
} 
/* imperative version */
object sumcol
{
  def main(args: Array[String]) = 
  {
    var sum = 0
    var line = Console.readLine

    while (line != null)
    {
      sum = sum + Integer.parseInt(line)
      line = Console.readLine
    }

    Console.println(sum.toString())
  }
}
