package riftwarp

import almhirt.common._

trait CanDecomposeSelf {
  def decompose[TDimension <: RiftDimension](implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
}