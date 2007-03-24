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
/* functional version */
object sumcol
{
  def sumFile(res: int): int = 
  {
    val line = Console.readLine
    if (line == null) res else sumFile(res + Integer.parseInt(line))
  }

  def main(args: Array[String]) = 
  {
    Console.println(sumFile(0).toString())
  }
}
