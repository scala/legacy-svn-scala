/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.util

/** Provides access to the defined values of an object.
  *
  * {{{
  * case class Foo(n: Int)
  * case class Bar()
  *
  * object FooBarz extends Values {
  *   val f1 = Foo(1)
  *   val f2 = Foo(2)
  *   val b1, b2, b3 = new Bar()
  * }
  *
  * val bars = FooBarz.values[Bar]    // Iterable(b1,b2,b3)
  * val f = FooBarz.byName[Foo]("f2") // Foo(2)
  * }}}
  */
trait Values {
  def values[A: Manifest]: Iterable[A] = nameValueMap[A].values

  private def nameValueMap[A: Manifest]: Map[String,A] = {
    val fields = getClass.getDeclaredFields

    val methods = getClass.getMethods filter { method =>
      method.getParameterTypes.isEmpty &&
      manifest.erasure.isAssignableFrom(method.getReturnType) &&
      method.getDeclaringClass != classOf[Values] &&
      fields.exists { field =>
        field.getName == method.getName && field.getType == method.getReturnType
      }
    }

    methods map { method =>
      val name = method.getName
      val value = method.invoke(this).asInstanceOf[A]
      (name -> value)
    } toMap
  }

  def byName[A: Manifest](s: String): Option[A] = nameValueMap[A].get(s)
}
