package riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class ConcurrentDecomposerRegistry extends HasDecomposers {
  private val decomposers = new _root_.java.util.concurrent.ConcurrentHashMap[TypeDescriptor, (RawDecomposer, Boolean)](512)

  def tryGetRawDecomposer(typeDescriptor: TypeDescriptor) =
    decomposers.get(typeDescriptor) match {
      case null => None
      case x => Some(x._1)
    }

  def tryGetDecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Decomposer[T]] =
    decomposers.get(typeDescriptor) match {
      case null => None
      case (desc, true) => Some(desc.asInstanceOf[Decomposer[T]])
      case _ => None
    }

  def addRawDecomposer(decomposer: RawDecomposer) { decomposers.put(decomposer.typeDescriptor, (decomposer, false)) }
  def addDecomposer(decomposer: Decomposer[_]) { decomposers.put(decomposer.typeDescriptor, (decomposer.asInstanceOf[RawDecomposer], true)) }
}