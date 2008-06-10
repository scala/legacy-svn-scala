/* ------------------------------------------------------------------ */
/* The Great Computer Language Shootout                               */
/* http://shootout.alioth.debian.org/                                 */
/*                                                                    */
/* Contributed by Anthony Borla                                       */
/* ------------------------------------------------------------------ */

object takfp
{
  def main(args: Array[String])
  {
    var n = Integer.parseInt(args(0));

    System.out.println(tak(n * 3.0f, n * 2.0f, n * 1.0f));
  }

  final def tak(x: Float, y: Float, z: Float): Float =
  {
    if (y >= x) return z;
    return tak(tak(x - 1.0f, y, z), tak(y - 1.0f, z, x), tak(z - 1.0f, x, y));
  }
}

