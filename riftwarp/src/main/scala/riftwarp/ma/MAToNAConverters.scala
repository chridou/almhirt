package riftwarp.ma

import scalaz.syntax.validation._
import almhirt.common._

class IdentityMAToNAConverter[M[_]] extends ConvertsMAToNA[M, M] {
  def convert[A](ma: M[A]): AlmValidation[M[A]] = ma.success
}

trait ListToVectorConverter extends RegisterableConvertsMAToNA[List, Vector] {
  def tM = classOf[List[_]]
  def tN = classOf[Vector[_]]
  def convert[A](ma: List[A]): AlmValidation[Vector[A]] = Vector(ma: _*).success
}

trait ListToSetConverter extends RegisterableConvertsMAToNA[List, Set] {
  def tM = classOf[List[_]]
  def tN = classOf[Set[_]]
  def convert[A](ma: List[A]): AlmValidation[Set[A]] = Set(ma: _*).success
}

trait ListToIterableConverter extends RegisterableConvertsMAToNA[List, Iterable] {
  def tM = classOf[List[_]]
  def tN = classOf[Set[_]]
  def convert[A](ma: List[A]): AlmValidation[Iterable[A]] = Iterable(ma: _*).success
}

object MAToNAConverters { 
  object listToVectorConverter extends ListToVectorConverter
  object listToSetConverter extends ListToSetConverter
  object listToIterableConverter extends ListToIterableConverter
}