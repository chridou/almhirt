package almhirt.streaming

import almhirt.common._
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
  
  def unapply(d: DeliveryStatus): Option[Option[TrackingTicket]] =
    d match {
    case UntrackedDeliveryJobDone => Some(None) 
    case TrackedDeliveryJobDone(ticket) => Some(Some(ticket)) 
    case _ => None
  }
}

sealed trait DeliveryJobFailed extends DeliveryStatus { def problem: Problem }
final case class UntrackedDeliveryJobFailed(problem: Problem ) extends DeliveryJobFailed
final case class TrackedDeliveryJobFailed(problem: Problem, ticket: TrackingTicket) extends DeliveryJobFailed

object DeliveryJobFailed {
  def apply(problem: Problem): DeliveryJobFailed = UntrackedDeliveryJobFailed(problem)
  def apply(problem: Problem , ticket: Option[TrackingTicket]): DeliveryJobFailed =
    ticket match {
      case Some(t) ⇒ TrackedDeliveryJobFailed(problem, t)
      case None ⇒ UntrackedDeliveryJobFailed(problem)
    }
  
  def unapply(d: DeliveryStatus): Option[(Problem, Option[TrackingTicket])] =
    d match {
    case UntrackedDeliveryJobFailed(problem) => Some((problem, None)) 
    case TrackedDeliveryJobFailed(problem, ticket) => Some((problem, Some(ticket))) 
    case _ => None
  }
}
