package almhirt.riftwarp

import almhirt.common._

trait RawDecomposer extends HasTypeDescriptor {
  def decomposeRaw(what: AnyRef)(implicit into: Dematerializer): AlmValidation[Dematerializer]
}

/** instance -> Atoms 
 */
trait Decomposer[T <: AnyRef] extends RawDecomposer {
  def decomposeRaw(what: AnyRef)(implicit into: Dematerializer): AlmValidation[Dematerializer] = decompose(what.asInstanceOf[T])
  def decompose(what: T)(implicit into: Dematerializer): AlmValidation[Dematerializer]
}