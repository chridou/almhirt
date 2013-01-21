package riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class UnsafeDecomposerRegistry extends HasDecomposers {
  private var decomposers = Map.empty[RiftDescriptor, Decomposer[_ <: AnyRef]]

  def getRawDecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawDecomposer] =
    decomposers.get(riftDescriptor) match {
      case None => KeyNotFoundProblem(s"No ecomposer found for $riftDescriptor").failure
      case Some(x) => x.success
    }

  def addDecomposer(decomposer: Decomposer[_ <: AnyRef]) { decomposers = decomposers + (decomposer.riftDescriptor -> decomposer) }
}