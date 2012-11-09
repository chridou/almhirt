package almhirt.commanding

import almhirt._
import almhirt.domain._
import almhirt.environment._
import almhirt.parts.HasRepositories
import almhirt.util.TrackingTicket

trait HandlesCommand {
  def commandType: Class[_ <: DomainCommand]
  def handle(com: DomainCommand, repositories: HasRepositories, context: AlmhirtContext, ticket: Option[TrackingTicket]): Unit
}

