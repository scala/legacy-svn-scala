/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy
*/

object nbody { 
   def main(args: Array[String]) {
      var n = Integer.parseInt(args(0))

      Console.printf("%.9f\n", JovianSystem.energy )
      while (n > 0) { JovianSystem.advance(0.01); n -= 1 }
      Console.printf("%.9f\n", JovianSystem.energy )
   } 
}


abstract class NBodySystem {

   def energy() = {
      var e = 0.0
      for (i <- Iterator.range(0,bodies.length)) {
         e = e + 0.5 * bodies(i).mass *
            ( bodies(i).vx * bodies(i).vx
            + bodies(i).vy * bodies(i).vy
            + bodies(i).vz * bodies(i).vz )

         for (val j <- Iterator.range(i+1,bodies.length)) {
            val dx = bodies(i).x - bodies(j).x
            val dy = bodies(i).y - bodies(j).y
            val dz = bodies(i).z - bodies(j).z

            val distance = Math.sqrt(dx*dx + dy*dy + dz*dz)
            e = e - (bodies(i).mass * bodies(j).mass) / distance
         }
      }
      e
   }


   def advance(dt: Double) = {
      var i = 0
      while (i < bodies.length) {

         var j = i+1
         while (j < bodies.length) {
            val dx = bodies(i).x - bodies(j).x
            val dy = bodies(i).y - bodies(j).y
            val dz = bodies(i).z - bodies(j).z

            val distance = Math.sqrt(dx*dx + dy*dy + dz*dz)
            val mag = dt / (distance * distance * distance)

            bodies(i).vx = bodies(i).vx - dx * bodies(j).mass * mag
            bodies(i).vy = bodies(i).vy - dy * bodies(j).mass * mag
            bodies(i).vz = bodies(i).vz - dz * bodies(j).mass * mag

            bodies(j).vx = bodies(j).vx + dx * bodies(i).mass * mag
            bodies(j).vy = bodies(j).vy + dy * bodies(i).mass * mag
            bodies(j).vz = bodies(j).vz + dz * bodies(i).mass * mag

            j += 1
         }
         i += 1
      }

      i = 0
      while (i < bodies.length) {
         bodies(i).x = bodies(i).x + dt * bodies(i).vx
         bodies(i).y = bodies(i).y + dt * bodies(i).vy
         bodies(i).z = bodies(i).z + dt * bodies(i).vz

         i += 1
      }
   }


   protected val bodies: Array[Body]

   class Body() {
      var x = 0.0; var y = 0.0; var z = 0.0
      var vx = 0.0; var vy = 0.0; var vz = 0.0
      var mass = 0.0
   }
}



object JovianSystem extends NBodySystem {

   protected val bodies = initialValues

   private def initialValues() = { 
      val PI = 3.141592653589793
      val SOLAR_MASS = 4 * PI * PI
      val DAYS_PER_YEAR = 365.24

      val sun = new Body
      sun.mass = SOLAR_MASS

      val jupiter = new Body
      jupiter.x = 4.84143144246472090e+00 
      jupiter.y = -1.16032004402742839e+00
      jupiter.z = -1.03622044471123109e-01
      jupiter.vx = 1.66007664274403694e-03 * DAYS_PER_YEAR
      jupiter.vy = 7.69901118419740425e-03 * DAYS_PER_YEAR
      jupiter.vz = -6.90460016972063023e-05 * DAYS_PER_YEAR
      jupiter.mass = 9.54791938424326609e-04 * SOLAR_MASS

      val saturn = new Body
      saturn.x = 8.34336671824457987e+00
      saturn.y = 4.12479856412430479e+00
      saturn.z = -4.03523417114321381e-01
      saturn.vx = -2.76742510726862411e-03 * DAYS_PER_YEAR
      saturn.vy = 4.99852801234917238e-03 * DAYS_PER_YEAR
      saturn.vz = 2.30417297573763929e-05 * DAYS_PER_YEAR
      saturn.mass = 2.85885980666130812e-04 * SOLAR_MASS

      val uranus = new Body
      uranus.x = 1.28943695621391310e+01
      uranus.y = -1.51111514016986312e+01
      uranus.z = -2.23307578892655734e-01
      uranus.vx = 2.96460137564761618e-03 * DAYS_PER_YEAR
      uranus.vy = 2.37847173959480950e-03 * DAYS_PER_YEAR
      uranus.vz = -2.96589568540237556e-05 * DAYS_PER_YEAR
      uranus.mass = 4.36624404335156298e-05 * SOLAR_MASS

      val neptune = new Body
      neptune.x = 1.53796971148509165e+01
      neptune.y = -2.59193146099879641e+01
      neptune.z = 1.79258772950371181e-01
      neptune.vx = 2.68067772490389322e-03 * DAYS_PER_YEAR
      neptune.vy = 1.62824170038242295e-03 * DAYS_PER_YEAR
      neptune.vz = -9.51592254519715870e-05 * DAYS_PER_YEAR
      neptune.mass = 5.15138902046611451e-05  * SOLAR_MASS


      val initialValues = Array ( sun, jupiter, saturn, uranus, neptune )

      var px = 0.0; var py = 0.0; var pz = 0.0;
      for (b <- initialValues){
         px = px + (b.vx * b.mass)
         py = py + (b.vy * b.mass)
         pz = pz + (b.vz * b.mass)
      }
      sun.vx = -px / SOLAR_MASS
      sun.vy = -py / SOLAR_MASS
      sun.vz = -pz / SOLAR_MASS

      initialValues
   }
}

