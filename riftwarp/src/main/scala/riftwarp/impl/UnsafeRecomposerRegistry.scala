package riftwarp.impl

import scalaz.std._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class UnsafeRecomposerRegistry extends HasRecomposers {
  private var recomposers = Map.empty[TypeDescriptor, (RawRecomposer, Boolean)]

  def tryGetRawRecomposer(typeDescriptor: TypeDescriptor) =
    recomposers.get(typeDescriptor).map(_._1)

  def tryGetRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Recomposer[T]] =
    recomposers.get(typeDescriptor).flatMap {
      case (desc, true) =>
       Some(desc.asInstanceOf[Recomposer[T]])
      case (desc, false) =>
        Some(new EnrichedRawRecomposer[T](desc))
    }

  def addRawRecomposer(recomposer: RawRecomposer) { recomposers = recomposers + (recomposer.typeDescriptor -> (recomposer, false)) }
  def addRecomposer(recomposer: Recomposer[_]) { recomposers = recomposers + (recomposer.typeDescriptor -> (recomposer.asInstanceOf[RawRecomposer], true)) }
}