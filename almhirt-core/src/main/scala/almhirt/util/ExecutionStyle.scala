package almhirt.util

import language.implicitConversions

import java.util.{ UUID => JUUID }
import almhirt.common._

sealed trait ExecutionStyle

object ExecutionStyle {
  def apply(): ExecutionStyle = FireAndForget
  def apply(uuid: JUUID): ExecutionStyle = Correlated(uuid)
  def apply(ticket: TrackingTicket): ExecutionStyle = Tracked(ticket)
  def correlated()(implicit hasUuids: CanCreateUuid): ExecutionStyle = apply(hasUuids.getUuid)
  def tracked()(implicit hasUuids: CanCreateUuid): ExecutionStyle = apply(TrackingTicket(hasUuids.getUuid))

  implicit def fromTicketOption2ExecutionStyle(maybeATicket: Option[TrackingTicket]): ExecutionStyle =
    maybeATicket match {
      case Some(t) => Tracked(t)
      case None => FireAndForget
    }

  implicit def fromTicket2ExecutionStyle(ticket: TrackingTicket): ExecutionStyle =
    apply(ticket)

  implicit def fromUUID2ExecutionStyle(correlationId: JUUID): ExecutionStyle =
    apply(correlationId)
}

case object FireAndForget extends ExecutionStyle
final case class Correlated(correlationId: JUUID) extends ExecutionStyle
final case class Tracked(ticket: TrackingTicket) extends ExecutionStyle