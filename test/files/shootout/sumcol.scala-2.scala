/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

/* functional version */
object sumcol
{
  def sumFile(res: Int): Int = 
  {
    val line = Console.readLine
    if (line == null) res else sumFile(res + Integer.parseInt(line))
  }

  def main(args: Array[String])
  {
    Console.println(sumFile(0).toString())
  }
}
