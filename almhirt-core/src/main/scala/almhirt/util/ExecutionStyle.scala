package almhirt.util

import java.util.{UUID => JUUID}

sealed trait ExecutionStyle
case object FireAndForget extends ExecutionStyle
final case class Correlated(correlationId: JUUID) extends ExecutionStyle
final case class Tracked(ticket: TrackingTicket) extends ExecutionStyle