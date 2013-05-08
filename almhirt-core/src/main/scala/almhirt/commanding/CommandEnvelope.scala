package almhirt.commanding

import almhirt.common.Command
import almhirt.util.TrackingTicket

case class CommandEnvelope(command: Command, ticket: Option[TrackingTicket])