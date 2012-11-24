package almhirt.riftwarp

import almhirt.common._

trait CanRematerializePrimitiveMA[M[_], A, TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor] {
  def tM: Class[_]
  def tA: Class[_]
  def tDimension: Class[_ <: RiftDimension]
  def tChannel: Class[_ <: RiftChannelDescriptor]
  def rematerialize(dim: TDimension): AlmValidation[M[A]]
}

abstract class CanRematerializePrimitiveMABase[M[_], A, TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](mM: Manifest[M[_]] , mA: Manifest[A], mD: Manifest[TDimension], mC: Manifest[TChannel]) extends CanRematerializePrimitiveMA[M, A, TDimension, TChannel]{
  val tM = mM.erasure
  val tA = mA.erasure
  val tDimension = mD.erasure.asInstanceOf[Class[_ <: RiftDimension]]
  val tChannel = mC.erasure.asInstanceOf[Class[_ <: RiftChannelDescriptor]]
}
