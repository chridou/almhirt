package riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class ConcurrentDecomposerRegistry extends HasDecomposers {
  private val decomposers = new _root_.java.util.concurrent.ConcurrentHashMap[RiftDescriptor, Decomposer[_]](512)

//  def getRawDecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawDecomposer] =
//    decomposers.get(riftDescriptor) match {
//      case null => KeyNotFoundProblem(s"No (Raw-)Decomposer found for $riftDescriptor").failure
//      case x => x.success
//    }

  def getDecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): AlmValidation[Decomposer[T]] =
    decomposers.get(riftDescriptor) match {
      case null => KeyNotFoundProblem(s"No Decomposer found for  $riftDescriptor").failure
      case x => x.asInstanceOf[Decomposer[T]].success
    }

  def addDecomposer(decomposer: Decomposer[_]) { decomposers.put(decomposer.riftDescriptor, decomposer) }
}