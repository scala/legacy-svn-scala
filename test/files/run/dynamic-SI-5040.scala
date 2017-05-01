class DynamicClass extends SuperClass with Dynamic {
  def applyDynamic(m: String)()= "Dynamic call: "+m
}

class SuperClass {
  private def privateMethod()= "Private method"
  def publicMethod()= "Public method"
}

object Test extends DynamicClass with App {
  println(this.privateMethod())
  println(this.publicMethod())
}
class DynamicClass extends SuperClass with Dynamic {
  def applyDynamic(m: String)()= "Dynamic call: "+m
}

class SuperClass {
  private def privateMethod()= "Private method"
  def publicMethod()= "Public method"
}

object Test extends DynamicClass with App {
  println(this.privateMethod())
  println(this.publicMethod())
}
class DynamicClass extends SuperClass with Dynamic {
  def applyDynamic(m: String)()= "Dynamic call: "+m
}

class SuperClass {
  private def privateMethod()= "Private method"
  def publicMethod()= "Public method"
}

object Test extends DynamicClass with App {
  println(this.privateMethod())
  println(this.publicMethod())
}
