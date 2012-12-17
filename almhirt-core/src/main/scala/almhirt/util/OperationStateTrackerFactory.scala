package almhirt.util

import almhirt.common._
import almhirt.environment._

trait OperationStateTrackerFactory {
  def createOperationStateTracker(baseOps: AlmhirtBaseOps, system: AlmhirtSystem): AlmValidation[OperationStateTracker]
}