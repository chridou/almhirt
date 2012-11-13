package almhirt.riftwarp

import almhirt.common._

trait RawDecomposer{
  def decomposeRaw(what: AnyRef)(implicit into: Dematerializer): AlmValidation[Dematerializer]
}

/** instance -> Atoms 
 */
trait Decomposer[T <: AnyRef] extends RawDecomposer {
  def decompose(what: T)(implicit into: Dematerializer): AlmValidation[Dematerializer] = decomposeRaw(what.asInstanceOf[AnyRef])
}