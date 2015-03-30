package almhirt.configuration

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._

trait Params {
  def getRaw(path: String): AlmValidation[Any]
  def findRaw(path: String): Option[Any]
  def keys: Iterator[String]
  def toMap: Map[String, Any]
  def ++(other: Params): Params
  def +(toAdd: (String, Any)): Params
}

object Params {
  def apply(params: (String, Any)*): Params =
    ParamsImpl(params.toMap)

  def fromMap(v: Map[String, Any]): Params =
    ParamsImpl(v)
    
  val empty: Params = Params.fromMap(Map.empty)

  private case class ParamsImpl(v: Map[String, Any]) extends Params {
    def getRaw(path: String): AlmValidation[Any] = findRaw(path) match { case Some(v) ⇒ v.success case None ⇒ NoSuchElementProblem(s"""Path "$path" not found.""").failure }
    def findRaw(path: String): Option[Any] = v get path
    def keys: Iterator[String] = v.keysIterator
    def toMap = v
    def ++(other: Params): Params = Params.fromMap(v ++ other.toMap)
    def +(toAdd: (String, Any)): Params = Params.fromMap(v + toAdd)
  }

  implicit class ParamsOps(val self: Params) extends AnyVal {
    def value[T](path: String)(implicit converter: ParamsConverter[T]): AlmValidation[T] =
      self.getRaw(path).flatMap(converter.apply)

    def v[T](path: String)(implicit converter: ParamsConverter[T]): AlmValidation[T] = self.value(path)

    def optValue[T](path: String)(implicit converter: ParamsConverter[T]): AlmValidation[Option[T]] =
      self findRaw (path) match {
        case None        ⇒ None.success
        case Some(thing) ⇒ converter(thing).map(Some(_))
      }

    def opt[T](path: String)(implicit converter: ParamsConverter[T]): AlmValidation[Option[T]] = self.optValue(path)

    def getOrElse[T](default: ⇒ T)(path: String)(implicit converter: ParamsConverter[T]): AlmValidation[T] =
      self findRaw path match {
        case None        ⇒ default.success
        case Some(thing) ⇒ converter(thing)
      }
  }
}