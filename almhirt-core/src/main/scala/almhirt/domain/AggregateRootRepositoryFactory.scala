package almhirt.domain

import almhirt.common._
import almhirt.environment._

trait AggregateRootRepositoryFactory {
  def createAggregateRootRepository[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](theAlmhirt: Almhirt): AlmValidation[AggregateRootRepository[AR, Event]]
}

