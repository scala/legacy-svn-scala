/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Andrei Formiga
 */

object partialsums
{
  val twodiv3 = 2.0 / 3.0
  var n: double = _

  def calculate(s1:double, s2:double, s3:double, s4:double, s5:double, s6:double,
		s7:double, s8:double, s9:double, sign: double, kd: double): unit = {

    if (kd > n) {
      val f = "{0,number,0.000000000}\t"

      Console.printf( f + "(2/3)^k\n", s1)
      Console.printf( f + "k^-0.5\n", s2)
      Console.printf( f + "1/k(k+1)\n", s3)
      Console.printf( f + "Flint Hills\n", s4)
      Console.printf( f + "Cookson Hills\n", s5)
      Console.printf( f + "Harmonic\n", s6)
      Console.printf( f + "Riemann Zeta\n", s7)
      Console.printf( f + "Alternating Harmonic\n" ,s8)
      Console.printf( f + "Gregory\n", s9)
    }
    else {
      val k2 =  Math.pow(kd, 2.0)
      val k3 = k2 * kd
      val sin = Math.sin(kd)
      val cos = Math.cos(kd)
      calculate(s1 + Math.pow(twodiv3, kd - 1.0), s2 + Math.pow(kd, -0.5),
		s3 + 1.0 / (kd * (kd + 1.0)), s4 + 1.0 / (k3 * sin*sin),
		s5 + 1.0 / (k3 * cos*cos), s6 + 1.0 / kd, s7 + 1.0 / k2,
		s8 + sign / kd, s9 + sign / (2.0 * kd - 1.0), -sign, kd + 1.0)
    }
  }

  def main(args: Array[String]) = {
    n = Integer.parseInt(args(0)).toDouble
    calculate(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0)
  }
}
