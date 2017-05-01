abstract class DynamicClass extends SuperClass with Dynamic {
  def applyDynamic(m: String)()
}

class SuperClass {
  private def privateMethod()= ()
}

abstract class Test {
  val test: DynamicClass;
  test.privateMethod()
}
