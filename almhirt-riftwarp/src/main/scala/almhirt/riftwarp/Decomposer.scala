package almhirt.riftwarp

import almhirt.common._

trait RawDecomposer extends HasTypeDescriptor {
  def decomposeRaw[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]](what: AnyRef)(implicit into: Dematerializer[TChannel, TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]]
}

/** instance -> Atoms 
 */
trait Decomposer[T <: AnyRef] extends RawDecomposer {
  def decomposeRaw[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]](what: AnyRef)(implicit into: Dematerializer[TChannel, TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]] = 
    decompose[TChannel, TDimension](what.asInstanceOf[T])
  def decompose[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]](what: T)(implicit into: Dematerializer[TChannel, TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]]
}