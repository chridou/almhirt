package almhirt.eventlog

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait DomainEventLogFactory {
  def createDomainEventLog(almhirt: Almhirt): AlmValidation[ActorRef]
}