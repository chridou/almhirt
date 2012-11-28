package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

trait HasRecomposers {
  def tryGetRawRecomposer(typeDescriptor: TypeDescriptor): Option[RawRecomposer]
  def tryGetRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Recomposer[T]]
 
  def addRawRecomposer(recomposer: RawRecomposer): Unit
  def addRecomposer(recomposer: Recomposer[_]): Unit
}

