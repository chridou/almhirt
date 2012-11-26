package almhirt.riftwarp

import almhirt.common._

trait CanDematerializeMA[M[_], A <: AnyRef, TDimension <: RiftDimension] {
  def tM: Class[_]
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def dematerialize(ma: M[A])(tryGetRawDecomposer: AnyRef => Option[RawDecomposer]): AlmValidation[TDimension]
}