package almhirt.args

import scala.reflect.ClassTag
import scalaz.syntax.Ops
import almhirt.common._

trait ArgsOps1 extends Ops[Map[String, Any]] {
  def value[T](key: String)(implicit tag: ClassTag[T]): AlmValidation[T] = funs.getValue(key, self)
  def fromPath(path: String, sep: Char): AlmValidation[Any] = funs.getFromPath(path, sep, self)
  def fromPropertyPath(path: String): AlmValidation[Any] = funs.getFromPropertyPath(path, self)
  def valueFromPropertyPath[T](path: String)(implicit tag: ClassTag[T]): AlmValidation[T] = funs.getValueFromPropertyPath(path, self)
}

import language.implicitConversions

trait ToArgsOps{
  implicit def FromArgsOps1(a: Map[String, Any]): ArgsOps1 = new ArgsOps1 { def self = a }
}