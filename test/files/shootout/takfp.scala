/* ------------------------------------------------------------------ */
/* The Great Computer Language Shootout                               */
/* http://shootout.alioth.debian.org/                                 */
/*                                                                    */
/* Contributed by Anthony Borla                                       */
/* ------------------------------------------------------------------ */

object takfp
{
  def main(args: Array[String]): unit =
  {
    var n = Integer.parseInt(args(0));

    System.out.println(tak(n * 3.0f, n * 2.0f, n * 1.0f));
  }

  final def tak(x: float, y: float, z: float): float =
  {
    if (y >= x) return z;
    return tak(tak(x - 1.0f, y, z), tak(y - 1.0f, z, x), tak(z - 1.0f, x, y));
  }
}

