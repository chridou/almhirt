package riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

class UnsafeDecomposerRegistry extends HasDecomposers {
  private var decomposers = Map.empty[TypeDescriptor, (RawDecomposer, Boolean)]

  def tryGetRawDecomposer(typeDescriptor: TypeDescriptor) =
    decomposers.get(typeDescriptor).map(_._1)

  def tryGetDecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Decomposer[T]] =
    decomposers.get(typeDescriptor).flatMap {
      case (desc, isTyped) =>
        boolean.fold(isTyped, Some(desc.asInstanceOf[Decomposer[T]]), None)
    }

  def addRawDecomposer(decomposer: RawDecomposer) { decomposers = decomposers + (decomposer.typeDescriptor -> (decomposer, false)) }
  def addDecomposer(decomposer: Decomposer[_]) { decomposers = decomposers + (decomposer.typeDescriptor -> (decomposer.asInstanceOf[RawDecomposer], true)) }
}