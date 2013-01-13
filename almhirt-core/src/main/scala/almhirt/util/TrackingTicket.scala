package almhirt.util

sealed trait TrackingTicket
case class StringTrackingTicket(ident: String) extends TrackingTicket
case class UuidTrackingTicket(ident: java.util.UUID) extends TrackingTicket

object TrackingTicket {
  def apply(): TrackingTicket = apply(java.util.UUID.randomUUID())
  def apply(ident: String): TrackingTicket = StringTrackingTicket(ident)
  def apply(ident: java.util.UUID): TrackingTicket = UuidTrackingTicket(ident)
}
