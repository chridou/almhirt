package almhirt.util

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._

trait OperationStateTrackerFactory {
  def createOperationStateTracker(almhirt: Almhirt): AlmValidation[ActorRef]
}