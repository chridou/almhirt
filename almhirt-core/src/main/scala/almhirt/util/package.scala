package almhirt

package object util {
  import language.implicitConversions
  implicit def string2TrackingTicket(ident: String): TrackingTicket = TrackingTicket(ident)
  implicit def uuid2TrackingTicket(ident: java.util.UUID): TrackingTicket = TrackingTicket(ident)
}