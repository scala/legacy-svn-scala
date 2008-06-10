/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

/* imperative version */
object sumcol
{
  def main(args: Array[String])
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
