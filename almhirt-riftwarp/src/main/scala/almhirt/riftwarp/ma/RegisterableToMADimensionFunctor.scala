package almhirt.riftwarp.ma

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.riftwarp._

trait RegisterableToMADimensionFunctor[M[_]] {
  def tM: Class[M[_]]
  def map[A, TDimension <: RiftDimension](ma: M[A])(f: A => AlmValidation[TDimension]): AlmValidation[M[TDimension]]
}

trait ToListDimensionFunctor extends RegisterableToMADimensionFunctor[List] {
  val tM = classOf[List[_]]
  def map[A, TDimension <: RiftDimension](ma: List[A])(f: A => AlmValidation[TDimension]): AlmValidation[List[TDimension]] =
    ma.map(f).map(_.toAgg).sequence
}

trait ToVectorDimensionFunctor extends RegisterableToMADimensionFunctor[Vector] {
  val tM = classOf[Vector[_]]
  def map[A, TDimension <: RiftDimension](ma: Vector[A])(f: A => AlmValidation[TDimension]): AlmValidation[Vector[TDimension]] =
    ma.map(f).map(_.toAgg).toList.sequence.map(x => Vector(x: _*))
}

trait ToSetDimensionFunctor extends RegisterableToMADimensionFunctor[Set] {
  val tM = classOf[Set[_]]
  def map[A, TDimension <: RiftDimension](ma: Set[A])(f: A => AlmValidation[TDimension]): AlmValidation[Set[TDimension]] =
    ma.map(f).map(_.toAgg).toList.sequence.map(x => Set(x: _*))
}

trait ToIterableDimensionFunctor extends RegisterableToMADimensionFunctor[Iterable] {
  val tM = classOf[Iterable[_]]
  def map[A, TDimension <: RiftDimension](ma: Iterable[A])(f: A => AlmValidation[TDimension]): AlmValidation[Iterable[TDimension]] =
    ma.map(f).map(_.toAgg).toList.sequence.map(x => Iterable(x: _*))
}

trait ToTreeDimensionFunctor extends RegisterableToMADimensionFunctor[scalaz.Tree] {
  val tM = classOf[scalaz.Tree[_]]
  def map[A, TDimension <: RiftDimension](ma: scalaz.Tree[A])(f: A => AlmValidation[TDimension]): AlmValidation[scalaz.Tree[TDimension]] =
    sys.error("NotImplemented: DimensionFunctorTree")
}

object RegisterableToMADimensionFunctors {
  object toListDimensionFunctor extends ToListDimensionFunctor
  object toVectorDimensionFunctor extends ToVectorDimensionFunctor
  object toSetDimensionFunctor extends ToSetDimensionFunctor
  object toIterableDimensionFunctor extends ToIterableDimensionFunctor
  object toTreeDimensionFunctor extends ToTreeDimensionFunctor
}


