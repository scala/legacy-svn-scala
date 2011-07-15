/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.reflect

/** This type is required by the compiler and <b>should not be used in client code</b>. */
abstract class Type

/** This type is required by the compiler and <b>should not be used in client code</b>. */
case object NoPrefix extends Type
/** This type is required by the compiler and <b>should not be used in client code</b>. */
case object NoType extends Type

/** This type is required by the compiler and <b>should not be used in client code</b>.
  * fullname */
case class NamedType(fullname: String) extends Type

/** This type is required by the compiler and <b>should not be used in client code</b>.
  * pre # sym */
case class PrefixedType(pre: Type, sym: Symbol) extends Type

/** This type is required by the compiler and <b>should not be used in client code</b>.
  * pre.type # sym == pre.sym */
case class SingleType(pre: Type, sym: Symbol) extends Type

/** This type is required by the compiler and <b>should not be used in client code</b>.
  * clazz.this */
case class ThisType(clazz: Symbol) extends Type

/** This type is required by the compiler and '''should not be used in client code'''.
  * clazz.super[superClazz]
  * `tpe[args,,1,,, ..., args,,n,,]`
  */
case class AppliedType(tpe: Type, args: List[Type]) extends Type

/** This type is required by the compiler and '''should not be used in client code'''.
  * [a &lt;: lo &gt;: hi] */
case class TypeBounds(lo: Type, hi: Type) extends Type

/** This type is required by the compiler and '''should not be used in client code'''.
  * `(formals,,1,, ... formals,,n,,) restpe`
  */
case class MethodType(formals: List[Symbol], restpe: Type) extends Type

/** This type is required by the compiler and '''should not be used in client code'''. */
case class NullaryMethodType(resultType: Type) extends Type

/** This type is required by the compiler and '''should not be used in client code'''. */
case class PolyType(typeParams: List[Symbol], typeBounds: List[(Type, Type)], resultType: Type) extends Type

/* Standard pattern match:

  case reflect.NoPrefix =>
  case reflect.NoType =>
  case reflect.NamedType(fullname) => 
  case reflect.PrefixedType(pre, sym) => 
  case reflect.SingleType(pre, sym) => 
  case reflect.ThisType(clazz) => 
  case reflect.AppliedType(tpe, args) => 
  case reflect.TypeBounds(lo, hi) => 
  case reflect.MethodType(formals, restpe) =>
  case reflect.NullaryMethodType(restpe) =>
  case reflect.PolyType(typeParams, typeBounds, resultType) =>
*/
