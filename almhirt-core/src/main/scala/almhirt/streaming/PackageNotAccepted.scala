package almhirt.streaming

import almhirt.tracking.TrackingTicket

sealed trait DeliveryJobNotAccepted

case object UntrackedDeliveryJobNotAccepted extends DeliveryJobNotAccepted
final case class TrackedDeliveryJobNotAccepted(ticket: TrackingTicket) extends DeliveryJobNotAccepted

object DeliveryJobNotAccepted {
  def apply(): DeliveryJobNotAccepted = UntrackedDeliveryJobNotAccepted
  def apply(ticket: Option[TrackingTicket]): DeliveryJobNotAccepted =
    ticket match {
      case Some(t) => TrackedDeliveryJobNotAccepted(t)
      case None => UntrackedDeliveryJobNotAccepted
    }
}