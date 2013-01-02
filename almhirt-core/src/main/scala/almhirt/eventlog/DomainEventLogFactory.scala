package almhirt.eventlog

import almhirt.common._
import almhirt.environment._

trait DomainEventLogFactory {
  def createDomainEventLog(almhirt: Almhirt): AlmValidation[DomainEventLog]
}