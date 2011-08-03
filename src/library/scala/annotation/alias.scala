/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.annotation

/** An annotation designating that a definition is created with aliases.
  *
  * The created aliases will be final and will have the same parameters and
  * return types as the original definition, if vars, vals or defs are
  * annotated. If classes, objects or traits are annotated, the compiler will
  * create the aliases in the surrounding package object.
  *
  * The compiler will turn the following
  * {{{
  *   trait Widget {
  *     @alias("bg")
  *     def background: Color
  *
  *     @alias("bg_=")
  *     def background_=(color: Color): Unit
  *   }
  * }}}
  *
  * into
  *
  * {{{
  *   trait Widget {
  *     def background: Color
  *     def background_=(color: Color): Unit
  *
  *     final def bg = background
  *     final def bg_=(color: Color): Unit
  *   }
  * }}}
  *
  * @param  aliases  the names of the aliases that will be created
  */
class alias(aliases: String*) extends Annotation
