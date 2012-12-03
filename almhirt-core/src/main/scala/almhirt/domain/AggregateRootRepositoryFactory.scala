package almhirt.domain

import almhirt.common._
import almhirt.environment._

trait AggregateRootRepositoryFactory {
  def createAggregateRootRepository[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](ctx: AlmhirtContext): AlmValidation[AggregateRootRepository[AR, Event]]
}

