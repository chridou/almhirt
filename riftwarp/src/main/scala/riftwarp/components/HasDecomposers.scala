package riftwarp.components

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasDecomposers {
//  def getRawDecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawDecomposer]
  def getRawDecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawDecomposer]

  def addDecomposer(decomposer: Decomposer[_ <: AnyRef]): Unit
}