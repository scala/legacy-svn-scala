class Test {
  abstract class A {
    this_a =>
    def b= new B
    class B {
      def a: this_a.type= this_a
    }
  }

  def a= new A {
    def c { }
  }

  val x= a
  x.b.a.c // this worked
  a.b.a.c // this exhibited https://issues.scala-lang.org/browse/SI-5130

  trait A2 {
    def c { }
  }

  def a2= new A with A2

  a2.b.a.c // also failed
}
