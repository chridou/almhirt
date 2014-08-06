package almhirt.context

import akka.actor._
import almhirt.common._
import almhirt.streaming.AlmhirtChannels
import com.typesafe.config._

trait AlmhirtContext extends CanCreateUuidsAndDateTimes with AlmhirtChannels with HasExecutionContexts {
  def config: Config
}

object AlmhirtContext {
  def apply(system: ActorSystem): AlmhirtContext =
    ???
}