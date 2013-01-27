package almhirt.domain

import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait AggregateRootRepositoryFactory {
  def createAggregateRootRepository[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](theAlmhirt: Almhirt): AlmValidation[AggregateRootRepository[AR, Event]]
}

