/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.dbc
package value;


/** A SQL-99 value of type character large object. */
@deprecated(DbcIsDeprecated) abstract class CharacterLargeObject extends Value {
  
  override val dataType: datatype.CharacterLargeObject;
  
  /** An SQL-99 compliant string representation of the value. */
  def sqlString: String = {
    "'" + nativeValue + "'"
  }
  
}

/** An object offering transformation methods (views) on the value.
  * This object must be visible in an expression to use value auto-
  * conversion. */
@deprecated(DbcIsDeprecated) object CharacterLargeObject {
  
  /** A character large object value as a native string. */
  implicit def characterLargeObjectToString (obj:value.CharacterLargeObject): String = obj.nativeValue;
  
}
