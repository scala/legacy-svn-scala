/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

/* functional version */
object sumcol
{
  def sumFile(res: int): int = 
  {
    try
    {
      val line = Console.readLine
      sumFile(res + Integer.parseInt(line))
    }
    catch
    {
      case _: java.io.EOFException => res
    } 
  }

  def main(args: Array[String]) = 
  {
    Console.println(sumFile(0).toString())
  }
}
