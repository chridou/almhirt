package riftwarp.impl

import scalaz.std._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class ConcurrentRecomposerRegistry extends HasRecomposers {
  private val recomposers = new _root_.java.util.concurrent.ConcurrentHashMap[TypeDescriptor, (RawRecomposer, Boolean)](512)

  def tryGetRawRecomposer(typeDescriptor: TypeDescriptor) =
    recomposers.get(typeDescriptor) match {
      case null => None
      case x => Some(x._1)
    }

  def tryGetRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Recomposer[T]] =
    recomposers.get(typeDescriptor) match {
      case null => None
      case (desc, true) => Some(desc.asInstanceOf[Recomposer[T]])
      case _ => None
    }

  def addRawRecomposer(recomposer: RawRecomposer) { recomposers.put(recomposer.typeDescriptor, (recomposer, false)) }
  def addRecomposer(recomposer: Recomposer[_]) { recomposers.put(recomposer.typeDescriptor, (recomposer.asInstanceOf[RawRecomposer], true)) }
}
