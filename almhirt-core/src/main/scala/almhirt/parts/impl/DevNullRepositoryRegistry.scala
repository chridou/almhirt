package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.domain.AggregateRootRepository
import almhirt.parts.HasRepositories
import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent

class DevNullRepositoryRegistry extends HasRepositories {
  def getByType(repoType: Class[_ <: AggregateRootRepository[_,_]]): AlmValidation[AnyRef] =
    NotFoundProblem("Repository of type '%s' not found".format(repoType.getName)).failure

  def register[T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[T]) {}
}