package almhirt.messaging

import almhirt.common._
import almhirt.environment._

trait MessageHubFactory {
  def createMessageHub(sys: AlmhirtSystem): AlmValidation[MessageHub]
}
