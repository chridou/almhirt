package almhirt.streaming

import almhirt.tracking.TrackingTicket

sealed trait DeliveryJobDone
case object UntrackedDeliveryJobDone extends DeliveryJobDone
final case class TrackedDeliveryJobDone(ticket: TrackingTicket) extends DeliveryJobDone

object DeliveryJobDone {
  def apply(): DeliveryJobDone = UntrackedDeliveryJobDone
  def apply(ticket: Option[TrackingTicket]): DeliveryJobDone =
    ticket match {
      case Some(t) => TrackedDeliveryJobDone(t)
      case None => UntrackedDeliveryJobDone
    }
}