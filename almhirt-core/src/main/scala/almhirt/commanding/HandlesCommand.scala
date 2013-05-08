package almhirt.commanding

import almhirt._
import almhirt.common.Command
import almhirt.domain._
import almhirt.environment._
import almhirt.parts.HasRepositories
import almhirt.util.TrackingTicket

trait HandlesCommand {
  def commandType: Class[_ <: Command]
  def handle(com: Command, ticket: Option[TrackingTicket]): Unit
}

