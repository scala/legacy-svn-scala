object dynamicObject extends Dynamic {
  def applyDynamic(m: String)() = ()
  this.foo()
}
class dynamicClass extends Dynamic {
  def applyDynamic(m: String)() = ()
  this.bar
  dynamicObject.bar()
}
abstract class dynamicAbstractClass extends Dynamic {
  def applyDynamic(m: String)(args: Any*): Int
  this.pili(1, new dynamicClass, "hello");
}
trait dynamicTrait extends Dynamic {
  def applyDynamic(m: String)(args: Any*) = 1
  def two = 2
  this.mili(1,2,3)
  two
}
object dynamicMixin extends dynamicAbstractClass with dynamicTrait {
  this.foo(None)
}
