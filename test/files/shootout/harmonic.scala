/* ------------------------------------------------------------------ */
/* The Great Computer Language Shootout                               */
/* http://shootout.alioth.debian.org/                                 */
/*                                                                    */
/* Contributed by Anthony Borla                                       */
/* ------------------------------------------------------------------ */

import java.text.DecimalFormat;
import java.text.FieldPosition;

object harmonic
{
  def main(args: Array[String]): unit =
  {
    var n = Integer.parseInt(args(0));

    var value = harmonic(n, 0.0);

    val formatter = new DecimalFormat("#.000000000");
    var formattedValue = formatter.format(value, new StringBuffer(), new FieldPosition(0));

    System.out.println(formattedValue);
  }

  final def harmonic(n: int, a: double): double =
  {
    if (n == 0) return a;
    return harmonic(n - 1, a + 1.0 / n);
  }
}

