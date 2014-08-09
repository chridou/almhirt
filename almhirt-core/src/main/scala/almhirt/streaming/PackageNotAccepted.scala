package almhirt.streaming

import almhirt.tracking.TrackingTicket

sealed trait PackageNotAccepted

case object UntrackedPackageNotAccepted extends PackageNotAccepted
final case class TrackedPackageNotAccepted(ticket: TrackingTicket) extends PackageNotAccepted

object PackageNotAccepted {
  def apply(): PackageNotAccepted = UntrackedPackageNotAccepted
  def apply(ticket: Option[TrackingTicket]): PackageNotAccepted =
    ticket match {
      case Some(t) => TrackedPackageNotAccepted(t)
      case None => UntrackedPackageNotAccepted
    }
}