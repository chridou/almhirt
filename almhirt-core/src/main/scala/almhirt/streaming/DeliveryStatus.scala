package almhirt.streaming

import almhirt.tracking.TrackingTicket

sealed trait DeliveryStatus 

sealed trait DeliveryJobDone extends DeliveryStatus
case object UntrackedDeliveryJobDone extends DeliveryJobDone
final case class TrackedDeliveryJobDone(ticket: TrackingTicket) extends DeliveryJobDone

object DeliveryJobDone {
  def apply(): DeliveryJobDone = UntrackedDeliveryJobDone
  def apply(ticket: Option[TrackingTicket]): DeliveryJobDone =
    ticket match {
      case Some(t) ⇒ TrackedDeliveryJobDone(t)
      case None ⇒ UntrackedDeliveryJobDone
    }
}

sealed trait DeliveryJobNotAccepted extends DeliveryStatus

case object UntrackedDeliveryJobNotAccepted extends DeliveryJobNotAccepted
final case class TrackedDeliveryJobNotAccepted(ticket: TrackingTicket) extends DeliveryJobNotAccepted

object DeliveryJobNotAccepted {
  def apply(): DeliveryJobNotAccepted = UntrackedDeliveryJobNotAccepted
  def apply(ticket: Option[TrackingTicket]): DeliveryJobNotAccepted =
    ticket match {
      case Some(t) ⇒ TrackedDeliveryJobNotAccepted(t)
      case None ⇒ UntrackedDeliveryJobNotAccepted
    }
}