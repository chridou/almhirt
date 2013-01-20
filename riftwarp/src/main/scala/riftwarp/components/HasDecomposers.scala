package riftwarp.components

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasDecomposers {
//  def getRawDecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawDecomposer]
  def getDecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): AlmValidation[Decomposer[T]]

  def addDecomposer(decomposer: Decomposer[_]): Unit
}