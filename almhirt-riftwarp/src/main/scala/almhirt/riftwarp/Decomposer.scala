package almhirt.riftwarp

import almhirt.common._

trait RawDecomposer extends HasTypeDescriptor {
  def decomposeRaw[TDimension <: RiftDimension[TManifest], TManifest](what: AnyRef)(implicit into: Dematerializer[Manifestation[TManifest]]): AlmValidation[Dematerializer[Manifestation[TManifest]]] 
}

/** instance -> Atoms 
 */
trait Decomposer[T <: AnyRef] extends RawDecomposer {
  def decomposeRaw[TDimension <: RiftDimension[TManifest], TManifest](what: AnyRef)(implicit into: Dematerializer[Manifestation[TManifest]]): AlmValidation[Dematerializer[Manifestation[TManifest]]] = 
    decompose[TDimension, TManifest](what.asInstanceOf[T])
  def decompose[TDimension <: RiftDimension[TManifest], TManifest](what: T)(implicit into: Dematerializer[Manifestation[TManifest]]): AlmValidation[Dematerializer[Manifestation[TManifest]]]
}