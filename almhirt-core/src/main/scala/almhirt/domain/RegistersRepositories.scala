package almhirt.domain

import almhirt._

trait RegistersRepositories {
  def get[T <: AggregateRootRepository[_,_]](implicit m: Manifest[T]): AlmValidation[T]
}