/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Yura Taras
   modified by Isaac Gouy
*/


object chameneos {
  abstract class Colour 
  case object RED extends Colour
  case object YELLOW extends Colour
  case object BLUE extends Colour
  case object FADED extends Colour
  val colours = List(RED, BLUE, YELLOW, FADED)
  class MeetingPlace(var n: Int) {
    var other: Creature = _
    def meet(c: Creature) = synchronized {
      if(n > 0) {
          if(other == null) {
            other = c;
            this.wait()
          } else {
            other.setOther(c.colour)
            c.setOther(other.colour)
            other = null
            n = n - 1
            this.notify()
          }
        } else {
          c.setOther(FADED)
      }
    }
  }
  class Creature(private val mp: MeetingPlace, var colour: Colour) extends Thread {
    private var met = 0
    var other: Colour = _
    def setOther(_o: Colour) {
      other = _o
    }
    def getCreaturesMet = met
    override def run() {
      try {
        while(colour != FADED) {
          mp.meet(this)
          if(other == FADED) {
            colour = FADED
          } else {
            met = met + 1
            colour = complement(other)
          }
        }
      } catch {
        case e:InterruptedException => () // Let the thread exit
      }
    }
    
    def complement(other: Colour) = (colour, other) match {
      case (RED, YELLOW)   => BLUE
      case (RED, BLUE)     => YELLOW
      case (RED, RED)      => RED
      case (YELLOW, BLUE)  => RED
      case (YELLOW, RED)   => BLUE
      case (YELLOW,YELLOW) => YELLOW
      case (BLUE, RED)     => YELLOW
      case (BLUE, YELLOW)  => RED
      case (BLUE, BLUE)    => BLUE
      case (FADED, _)      => FADED
    }
  }
  
  def apply(n: Int) {
      val mp = new MeetingPlace(n)
      val creatures = for(x <- colours) yield {
        val cr = new Creature(mp, x);
        cr.start();
        cr
      }
      creatures.foreach(x => x.join)
      val meetings = (creatures foldLeft 0) {(x, y) => (x + y.getCreaturesMet)}
      Console.println(meetings)
  }

  def main(args: Array[String]) {
    if(args.length < 1) throw new IllegalArgumentException();
    chameneos(Integer.parseInt(args(0)))
  }
}



