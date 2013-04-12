package almhirt.eventlog

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait DomainEventLogFactory {
  def createDomainEventLog(args: Map[String, Any]): AlmValidation[ActorRef]
}