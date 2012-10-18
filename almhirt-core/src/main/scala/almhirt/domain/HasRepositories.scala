package almhirt.domain

import almhirt._

trait HasRepositories {
  def get[T <: AggregateRootRepository[_,_]](implicit m: Manifest[T]): AlmValidation[T]
  def register[T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[T]): Unit
}