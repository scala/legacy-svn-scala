class DynamicClass extends Dynamic {
  def applyDynamic(m: String)() = ()
  nonExistingMethod()
}
