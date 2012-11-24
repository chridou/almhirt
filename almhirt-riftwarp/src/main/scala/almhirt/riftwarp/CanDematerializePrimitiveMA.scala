package almhirt.riftwarp

import almhirt.common._

trait CanDematerializePrimitiveMA[M[_], A, TChannel <: RiftChannelDescriptor, TDimension <: RiftDimension] {
  def tM: Class[_]
  def tA: Class[_]
  def tChannel: Class[_ <: RiftChannelDescriptor]
  def tDimension: Class[_ <: RiftDimension]
  def dematerialize(ma: M[A]): AlmValidation[TDimension]
}

abstract class CanDematerializePrimitiveMABase[M[_], A, TChannel <: RiftChannelDescriptor, TDimension <: RiftDimension](mM: Manifest[M[_]] , mA: Manifest[A], mC: Manifest[TChannel], mD: Manifest[TDimension]) extends CanDematerializePrimitiveMA[M, A, TChannel, TDimension]{
  val tM = mM.erasure
  val tA = mA.erasure
  val tChannel = mC.erasure.asInstanceOf[Class[_ <: RiftChannelDescriptor]]
  val tDimension = mD.erasure.asInstanceOf[Class[_ <: RiftDimension]]
}

abstract class CanDematerializePrimitiveMAToCord[M[_], A, TChannel <: RiftChannelDescriptor](mM: Manifest[M[_]] , mA: Manifest[A], mC: Manifest[TChannel]) extends CanDematerializePrimitiveMABase[M, A, TChannel, DimensionCord](mM, mA, mC, manifest[DimensionCord])

abstract class CanDematerializePrimitiveMAToJsonCord[M[_], A](mM: Manifest[M[_]], mA: Manifest[A]) extends CanDematerializePrimitiveMAToCord[M, A, RiftJson](mM, mA, manifest[RiftJson])
