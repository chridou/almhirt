package almhirt.eventlog

import almhirt.common._
import almhirt.environment._

trait DomainEventLogFactory {
  def createDomainEventLog(baseOps: AlmhirtBaseOps, system: AlmhirtSystem): AlmValidation[DomainEventLog]
}