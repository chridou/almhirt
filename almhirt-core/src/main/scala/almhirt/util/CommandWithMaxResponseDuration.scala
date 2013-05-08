package almhirt.util

import almhirt.common.Command
import scala.concurrent.duration.FiniteDuration

final case class CommandWithMaxResponseDuration(command: Command, maxResponseDuration: Option[FiniteDuration])