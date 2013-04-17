package almhirt.args

import scala.reflect.ClassTag
import scalaz.syntax.Ops
import almhirt.common._

trait ArgsOps1 extends Ops[Map[String, Any]] {
  def value[T](key: String)(implicit tag: ClassTag[T]): AlmValidation[T] = funs.getValue(key, self)
  def fromPath(path: String, sep: Char): AlmValidation[Any] = funs.getFromPath(path, sep, self)
  def fromPropertyPath(path: String): AlmValidation[Any] = funs.getFromPropertyPath(path, self)
  def valueFromPropertyPath[T](path: String)(implicit tag: ClassTag[T]): AlmValidation[T] = funs.getValueFromPropertyPath(path, self)
  
  def isSetTrue(ident: String): Boolean = funs.isBooleanTrue(ident, self)
  def isSetFalse(ident: String): Boolean = funs.isBooleanFalse(ident, self)
  def isNotSetTrue(ident: String): Boolean = funs.isBooleanNotTrue(ident, self)
  def isNotSetFalse(ident: String): Boolean = funs.isBooleanNotFalse(ident, self)
}

import language.implicitConversions

trait ToArgsOps{
  implicit def FromArgsOps1(a: Map[String, Any]): ArgsOps1 = new ArgsOps1 { def self = a }
}