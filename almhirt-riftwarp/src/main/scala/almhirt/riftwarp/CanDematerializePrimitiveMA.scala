package almhirt.riftwarp

import almhirt.common._

trait CanDematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension, TChannel <: RiftChannelDescriptor] {
  def dematerialize(ma: M[A]): AlmValidation[TDimension]
}