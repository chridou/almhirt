package almhirt.riftwarp

import almhirt.common._

trait RawDecomposer extends HasTypeDescriptor {
  def decomposeRaw[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: AnyRef)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]]
}

/** instance -> Atoms 
 */
trait Decomposer[T <: AnyRef] extends RawDecomposer {
  def decomposeRaw[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: AnyRef)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = 
    decompose[TDimension, TChannel](what.asInstanceOf[T])
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: T)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]]
}