object Test extends App with Dynamic {
  def applyDynamic(m: String)(a: Any*) = "DynamicClass."+m+"("+a+")"
  def regularMethod() = "Regular method"
  println(this.bar)
  println(this.bar())
  println(Test.bar())
  println(this.regularMethod)
  println(this.regularMethod())
  println(Test.regularMethod())
}
