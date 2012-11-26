package almhirt.riftwarp

import almhirt.common._

trait CanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension] {
  def tM: Class[_]
  def tA: Class[_]
  def tDimension: Class[_ <: RiftDimension]
  def channel: RiftChannel
  def rematerialize(dim: TDimension): AlmValidation[M[A]]
}

abstract class CanRematerializePrimitiveMABase[M[_], A, TDimension <: RiftDimension](val channel: RiftChannel)(implicit mM: Manifest[M[_]] , mA: Manifest[A], mD: Manifest[TDimension]) extends CanRematerializePrimitiveMA[M, A, TDimension]{
  val tM = mM.erasure
  val tA = mA.erasure
  val tDimension = mD.erasure.asInstanceOf[Class[_ <: RiftDimension]]
}
