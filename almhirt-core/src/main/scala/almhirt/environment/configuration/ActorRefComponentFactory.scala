package almhirt.environment.configuration

import almhirt.common._
import akka.actor.ActorRef

trait ActorRefComponentFactory {
  def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef]
}