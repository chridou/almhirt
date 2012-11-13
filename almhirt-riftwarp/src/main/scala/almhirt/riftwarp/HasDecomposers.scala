package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

trait HasDecomposers {
  def tryGetRawDecomposer(typeDescriptor: TypeDescriptor): Option[RawDecomposer]
  def tryGetDecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Decomposer[T]]
  def tryGetDecomposerFor[T <: HasTypeDescriptor](what: T): Option[Decomposer[T]] = tryGetDecomposer[T](what.typeDescriptor)
 
  def addRawDecomposer(decomposer: RawDecomposer): Unit
  def addDecomposer(decomposer: Decomposer[_]): Unit
}