package riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class ConcurrentDecomposerRegistry extends HasDecomposers {
  private val decomposers = new _root_.java.util.concurrent.ConcurrentHashMap[RiftDescriptor, (RawDecomposer, Boolean)](512)

  def tryGetRawDecomposer(riftDescriptor: RiftDescriptor) =
    decomposers.get(riftDescriptor) match {
      case null => None
      case x => Some(x._1)
    }

  def tryGetDecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): Option[Decomposer[T]] =
    decomposers.get(riftDescriptor) match {
      case null => None
      case (desc, true) => Some(desc.asInstanceOf[Decomposer[T]])
      case _ => None
    }

  def addRawDecomposer(decomposer: RawDecomposer) { decomposers.put(decomposer.riftDescriptor, (decomposer, false)) }
  def addDecomposer(decomposer: Decomposer[_]) { decomposers.put(decomposer.riftDescriptor, (decomposer.asInstanceOf[RawDecomposer], true)) }
}