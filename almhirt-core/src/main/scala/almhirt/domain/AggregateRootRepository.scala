package almhirt.domain

import almhirt._

trait AggregateRootRepository {
  def get[T <: AggregateRoot[_,_]]: AlmValidation[T]
}