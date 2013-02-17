package almhirt.util

import java.util.{UUID => JUUID}
import almhirt.core.CanCreateUuid

sealed trait ExecutionStyle

object ExecutionStyle {
  def apply(): ExecutionStyle = FireAndForget
  def apply(uuid: JUUID): ExecutionStyle = Correlated(uuid)
  def apply(ticket: TrackingTicket): ExecutionStyle = Tracked(ticket)
  def correlated()(implicit hasUuids: CanCreateUuid): ExecutionStyle = apply(hasUuids.getUuid)
  def tracked()(implicit hasUuids: CanCreateUuid): ExecutionStyle = apply(TrackingTicket(hasUuids.getUuid))
}

case object FireAndForget extends ExecutionStyle
final case class Correlated(correlationId: JUUID) extends ExecutionStyle
final case class Tracked(ticket: TrackingTicket) extends ExecutionStyle