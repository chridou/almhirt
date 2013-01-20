package riftwarp.impl

import scalaz.std._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class UnsafeRecomposerRegistry extends HasRecomposers {
  private var recomposers = Map.empty[RiftDescriptor, (RawRecomposer, Boolean)]

  def tryGetRawRecomposer(riftDescriptor: RiftDescriptor) =
    recomposers.get(riftDescriptor).map(_._1)

  def tryGetRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): Option[Recomposer[T]] =
    recomposers.get(riftDescriptor).flatMap {
      case (desc, true) =>
       Some(desc.asInstanceOf[Recomposer[T]])
      case (desc, false) =>
        Some(new EnrichedRawRecomposer[T](desc))
    }

  def addRawRecomposer(recomposer: RawRecomposer) { recomposers = recomposers + (recomposer.riftDescriptor -> (recomposer, false)) }
  def addRecomposer(recomposer: Recomposer[_]) { recomposers = recomposers + (recomposer.riftDescriptor -> (recomposer.asInstanceOf[RawRecomposer], true)) }
}