package riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class UnsafeDecomposerRegistry extends HasDecomposers {
  private var decomposers = Map.empty[RiftDescriptor, (RawDecomposer, Boolean)]

  def tryGetRawDecomposer(riftDescriptor: RiftDescriptor) =
    decomposers.get(riftDescriptor).map(_._1)

  def tryGetDecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): Option[Decomposer[T]] =
    decomposers.get(riftDescriptor).flatMap {
      case (desc, isTyped) =>
        boolean.fold(isTyped, Some(desc.asInstanceOf[Decomposer[T]]), None)
    }

  def addRawDecomposer(decomposer: RawDecomposer) { decomposers = decomposers + (decomposer.riftDescriptor -> (decomposer, false)) }
  def addDecomposer(decomposer: Decomposer[_]) { decomposers = decomposers + (decomposer.riftDescriptor -> (decomposer.asInstanceOf[RawDecomposer], true)) }
}