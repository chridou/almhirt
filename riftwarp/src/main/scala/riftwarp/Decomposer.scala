package riftwarp

import almhirt.common._

trait RawDecomposer extends HasTypeDescriptor {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] 
}

/** instance -> Atoms 
 */
trait Decomposer[T <: AnyRef] extends RawDecomposer {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = 
    decompose[TDimension](what.asInstanceOf[T])
  def decompose[TDimension <: RiftDimension](what: T)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
}