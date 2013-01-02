package almhirt.eventlog

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._

trait DomainEventLogFactory {
  def createDomainEventLog(almhirt: Almhirt): AlmValidation[ActorRef]
}