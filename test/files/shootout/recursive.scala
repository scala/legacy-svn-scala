/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

object recursive
{
  def ack(x: Int, y: Int): Int =
    if (x == 0) y + 1 
    else if (y == 0) ack(x - 1, 1) 
    else ack(x - 1, ack(x, y - 1))

  def fib(n: Int): Int =
    if (n < 2) 1 else fib(n - 2) + fib(n - 1)

  def fib(n: Double): Double =
    if (n < 2.0) 1.0 else fib(n - 2.0) + fib(n - 1.0)

  def tak(x: Int, y: Int, z: Int): Int =
    if (y < x) tak(tak(x - 1, y, z), tak(y - 1, z, x), tak(z - 1, x, y))
    else z

  def tak(x: Double, y: Double, z: Double): Double = 
    if (y < x)  tak(tak(x - 1.0, y, z), tak(y - 1.0, z, x), tak(z - 1.0, x, y)) 
    else z

  def main(args: Array[String]) {
    var n = Integer.parseInt(args(0))

    // spawn a new thread fixes stack overflows on Windows 1.5 JDK
    // see more on ticket #1508
    val t = new Thread {
      override def run {
	Console.println("Ack(3," + n + "): " + ack(3, n))
	Console.printf("Fib(%.1f): %.1f\n", (27.0+n), fib(27.0+n))
	n = n - 1
	Console.println("Tak(" + (3*n) + "," + (2*n) + "," + n + "): " + tak(3*n, 2*n, n))
	Console.println("Fib(3): " + fib(3))
	Console.println("Tak(3.0,2.0,1.0): " + tak(3.0,2.0,1.0))
      }
    }
    t.start()
    t.join()
  }
}
