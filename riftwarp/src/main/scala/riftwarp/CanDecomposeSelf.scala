package riftwarp

import almhirt.common._
import riftwarp.components._

trait CanDecomposeSelf {
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
}