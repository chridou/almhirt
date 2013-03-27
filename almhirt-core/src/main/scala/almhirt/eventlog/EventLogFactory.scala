package almhirt.eventlog

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait EventLogFactory {
  def createEventLog(almhirt: Almhirt): AlmValidation[ActorRef]
}