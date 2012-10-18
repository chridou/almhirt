package almhirt.parts

import almhirt.AlmValidation
import almhirt.domain.AggregateRootRepository

trait HasRepositories {
  def get[T <: AggregateRootRepository[_,_]](implicit m: Manifest[T]): AlmValidation[T]
  /** Registers a new repository. Has replace semantics. 
   */
  def register[T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[T]): Unit
}