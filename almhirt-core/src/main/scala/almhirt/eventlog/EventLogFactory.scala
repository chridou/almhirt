package almhirt.eventlog

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt
import almhirt.core.Event

trait EventLogFactory {
  def createEventLog(args: Map[String, Any], filterPredicate: Option[Event => Boolean]): AlmValidation[ActorRef]
  def createEventLog(args: Map[String, Any]): AlmValidation[ActorRef] = createEventLog(args, None)
}