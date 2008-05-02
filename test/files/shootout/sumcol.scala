/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

/* imperative version */
object sumcol
{
  def main(args: Array[String]) = 
  {
    var sum = 0
    try
    {
      while (true)
      {
        val line = Console.readLine
        sum += Integer.parseInt(line)
      }
    }
    catch {
      case e: java.io.EOFException => //nop
    }

    Console.println(sum.toString())
  }
}
