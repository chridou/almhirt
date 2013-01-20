package almhirt.ext.core.riftwarp.serialization

import scalaz._
import scalaz.Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._
import almhirt.util.StringTrackingTicket

class TrackingTicketDecomposer extends Decomposer[TrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[TrackingTicket], 1)
  def decompose[TDimension <: RiftDimension](what: TrackingTicket)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case StringTrackingTicket(ident) =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "string"))
          .flatMap(_.addString("ident", ident))
      case UuidTrackingTicket(ident) =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "uuid"))
          .flatMap(_.addUuid("ident", ident))
    }
  }
}

class TrackingTicketRecomposer extends Recomposer[TrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[TrackingTicket], 1)
  def recompose(from: Rematerializer): AlmValidation[TrackingTicket] = {
    from.getString("type").flatMap {
      case "string" => from.getString("ident").map(StringTrackingTicket.apply)
      case "uuid" => from.getUuid("ident").map(UuidTrackingTicket.apply)
    }
  }
}
