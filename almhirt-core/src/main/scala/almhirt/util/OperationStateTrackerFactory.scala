package almhirt.util

import almhirt.common._
import almhirt.environment._

trait OperationStateTrackerFactory {
  def createOperationStateTracker(ctx: AlmhirtContext): AlmValidation[OperationStateTracker]
}