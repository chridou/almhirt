package almhirt.commanding

import almhirt.util.TrackingTicket

case class CommandEnvelope(command: DomainCommand, ticket: Option[TrackingTicket])