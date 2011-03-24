/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.dbc
package value;


@deprecated(DbcIsDeprecated) abstract class Unknown extends Value {
  
  val dataType: datatype.Unknown;
  
  def sqlString = sys.error("An 'ANY' value cannot be represented.");
  
}

@deprecated(DbcIsDeprecated) object UnknownType {

  def view (obj:value.Unknown): AnyRef = obj.nativeValue;

}
