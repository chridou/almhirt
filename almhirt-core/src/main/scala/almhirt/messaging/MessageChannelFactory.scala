package almhirt.messaging

import almhirt.common._
import almhirt.core._
import almhirt.environment._

trait MessageChannelFactory {
  def createMessageChannel[T <: AnyRef](foundations: HasConfig with HasActorSystem with HasExecutionContext): AlmValidation[MessageChannel[T]]
}
