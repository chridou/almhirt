package almhirt.eventlog

import almhirt.common._
import almhirt.environment.AlmhirtContext

trait DomainEventLogFactory {
  def createDomainEventLog(ctx: AlmhirtContext): AlmValidation[DomainEventLog]
}