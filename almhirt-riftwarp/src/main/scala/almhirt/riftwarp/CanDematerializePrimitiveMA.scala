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
  val tChannel = mC.erasure
  val tDimension = mD.erasure
}

abstract class CanDematerializePrimitiveMAToCord[M[_], A, TChannel <: RiftChannelDescriptor](mM: Manifest[M[_]] , mA: Manifest[A], mC: Manifest[TChannel]) extends CanDematerializePrimitiveMABase[M, A, TChannel, DimensionCord](mM, mA, mC, manifest[DimensionCord])

abstract class CanDematerializePrimitiveListToCord[A, TChannel <: RiftChannelDescriptor](mA: Manifest[A], mC: Manifest[TChannel]) extends CanDematerializePrimitiveMAToCord[List, A, TChannel](manifest[List[_]], mA, mC)
abstract class CanDematerializePrimitiveVectorToCord[A, TChannel <: RiftChannelDescriptor](mA: Manifest[A], mC: Manifest[TChannel]) extends CanDematerializePrimitiveMAToCord[Vector, A, TChannel](manifest[Vector[_]], mA, mC)
abstract class CanDematerializePrimitiveIterableToCord[A, TChannel <: RiftChannelDescriptor](mA: Manifest[A], mC: Manifest[TChannel]) extends CanDematerializePrimitiveMAToCord[Iterable, A, TChannel](manifest[Iterable[_]], mA, mC)
