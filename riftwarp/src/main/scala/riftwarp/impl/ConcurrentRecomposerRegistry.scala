package riftwarp.impl

import scalaz.std._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class ConcurrentRecomposerRegistry extends HasRecomposers {
  private val recomposers = new _root_.java.util.concurrent.ConcurrentHashMap[RiftDescriptor, (RawRecomposer, Boolean)](512)

  def tryGetRawRecomposer(riftDescriptor: RiftDescriptor) =
    recomposers.get(riftDescriptor) match {
      case null => None
      case x => Some(x._1)
    }

  def tryGetRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): Option[Recomposer[T]] =
    recomposers.get(riftDescriptor) match {
      case null => None
      case (desc, true) => Some(desc.asInstanceOf[Recomposer[T]])
      case _ => None
    }

  def addRawRecomposer(recomposer: RawRecomposer) { recomposers.put(recomposer.riftDescriptor, (recomposer, false)) }
  def addRecomposer(recomposer: Recomposer[_]) { recomposers.put(recomposer.riftDescriptor, (recomposer.asInstanceOf[RawRecomposer], true)) }
}
