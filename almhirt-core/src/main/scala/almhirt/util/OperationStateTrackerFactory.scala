package almhirt.util

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt
import almhirt.environment.configuration.ActorRefComponentFactory

trait OperationStateTrackerFactory extends ActorRefComponentFactory {
  def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef]
  def createOperationStateTracker(theAlmhirt: Almhirt): AlmValidation[ActorRef] = createActorRefComponent(Map(("almhirt" -> theAlmhirt)))
}