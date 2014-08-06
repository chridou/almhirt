package almhirt.context

import akka.actor._
import almhirt.common._
import almhirt.streaming.AlmhirtStreams
import com.typesafe.config._

trait AlmhirtContext extends CanCreateUuidsAndDateTimes with AlmhirtStreams with HasExecutionContexts {
  def config: Config
}

object AlmhirtContext {
  def apply(system: ActorSystem): AlmhirtContext =
    ???
}