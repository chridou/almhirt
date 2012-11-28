package riftwarp

import almhirt.common._

trait CanDematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension] {
  def tM: Class[_]
  def tA: Class[_]
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def dematerialize(ma: M[A]): AlmValidation[TDimension]
}

abstract class CanDematerializePrimitiveMABase[M[_], A, TDimension <: RiftDimension](val channel: RiftChannel)(implicit mM: Manifest[M[_]] , mA: Manifest[A], mD: Manifest[TDimension]) extends CanDematerializePrimitiveMA[M, A, TDimension]{
  val tM = mM.erasure
  val tA = mA.erasure
  val tDimension = mD.erasure.asInstanceOf[Class[_ <: RiftDimension]]
}

