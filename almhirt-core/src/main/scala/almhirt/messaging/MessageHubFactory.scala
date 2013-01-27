package almhirt.messaging

import almhirt.common._
import almhirt.environment._

trait MessageHubFactory {
  def createMessageHub(implicit foundations: HasConfig with HasActorSystem with HasExecutionContext): AlmValidation[MessageHub]
}
