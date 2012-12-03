package almhirt.messaging

import almhirt.common._
import almhirt.environment._

trait MessageChannelFactory {
  def createMessageChannel[T <: AnyRef](sys: AlmhirtSystem): AlmValidation[MessageChannel[T]]
}
