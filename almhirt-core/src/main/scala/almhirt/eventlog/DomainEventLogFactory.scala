package almhirt.eventlog

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt
import almhirt.environment.configuration.ActorRefComponentFactory

trait DomainEventLogFactory extends ActorRefComponentFactory{
  def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef] = createDomainEventLog(args)
  def createDomainEventLog(args: Map[String, Any]): AlmValidation[ActorRef]
}