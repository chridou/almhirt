package almhirt.riftwarp

import almhirt.common._

trait RawDecomposer extends HasTypeDescriptor {
  def decomposeRaw(what: AnyRef)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel]
}

/** instance -> Atoms 
 */
trait Decomposer[T <: AnyRef] extends RawDecomposer {
  def decomposeRaw(what: AnyRef)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = decompose(what.asInstanceOf[T])
  def decompose(what: T)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel]
}