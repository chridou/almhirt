package riftwarp

import almhirt.common._
import riftwarp.components._

trait RawDecomposer extends HasAlternativeRiftDescriptors {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
}

/**
 * instance -> Atoms
 */
trait Decomposer[-T <: AnyRef] extends RawDecomposer {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    almhirt.almvalidation.funs.computeSafely(decompose[TDimension](what.asInstanceOf[T])(into))
  def decompose[TDimension <: RiftDimension](what: T)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
}