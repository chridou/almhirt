package almhirt.util

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait OperationStateTrackerFactory {
  def createOperationStateTracker(almhirt: Almhirt): AlmValidation[ActorRef]
}