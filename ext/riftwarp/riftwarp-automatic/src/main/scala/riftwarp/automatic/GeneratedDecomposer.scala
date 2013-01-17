package riftwarp.automatic

import scala.language.experimental.macros

import almhirt.common._
import riftwarp._

trait GeneratedDecomposer[TToDecompose <: AnyRef] extends Decomposer[TToDecompose] {
  def decompose[TDimension <: RiftDimension](what: TToDecompose)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = decomposeUncurried[TToDecompose, TDimension](what, into)
  private def decomposeUncurried[TTWhat <: AnyRef, TTDimension <: RiftDimension](what: TTWhat, into: Dematerializer[TTDimension]): AlmValidation[Dematerializer[TTDimension]] = 
    macro GeneratedDecomposerImpl.decomposeUncurried[TTWhat, TTDimension]
}