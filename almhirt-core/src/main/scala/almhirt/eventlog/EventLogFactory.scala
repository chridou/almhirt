package almhirt.eventlog

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt
import almhirt.environment.configuration.ActorRefComponentFactory

trait EventLogFactory extends ActorRefComponentFactory {
  def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef] = createEventLog(args)
  def createEventLog(args: Map[String, Any], filterPredicate: Option[Event => Boolean]): AlmValidation[ActorRef]
  def createEventLog(args: Map[String, Any]): AlmValidation[ActorRef] = createEventLog(args, None)
}