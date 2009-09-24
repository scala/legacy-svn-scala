/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Philipp Haller
*/

import scala.actors._
import scala.actors.Actor._
import scala.actors.scheduler.SingleThreadedScheduler

object message {
  def main(args: Array[String]) = {
    val n = Integer.parseInt(args(0)); val nActors = 50; val finalSum = n * nActors
    Scheduler.impl = new SingleThreadedScheduler

    def beh(next: Actor, sum: Int) {
      react {
        case value: Int =>
          val j = value + 1; val nsum = sum + j
          if (next == null && nsum >= finalSum) {
            Console.println(nsum)
            System.exit(0)
          }
          else {
            if (next != null) next ! j
            beh(next, nsum)
          }
      }
    }

    def actorChain(i: Int, a: Actor): Actor =
      if (i > 0) actorChain(i-1, actor(beh(a, 0))) else a

    val firstActor = actorChain(nActors, null)
    var i = n; while (i > 0) { firstActor ! 0; i = i-1 }

    Scheduler.shutdown()
  }
}
