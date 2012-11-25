package almhirt.core.serialization

import scalaz._, Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.riftwarp._
import almhirt.util._
import almhirt.util.StringTrackingTicket

class TrackingTicketDecomposer extends Decomposer[TrackingTicket] {
  val typeDescriptor = TypeDescriptor(classOf[TrackingTicket], 1)
  def decompose[TChannel <: RiftChannel, TDimension <: RiftTypedDimension[_]](what: TrackingTicket)(implicit into: Dematerializer[TChannel, TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]] = {
    what match {
      case StringTrackingTicket(ident) =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .bind(_.addString("type", "string"))
          .bind(_.addString("ident", ident))
      case UuidTrackingTicket(ident) =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .bind(_.addString("type", "uuid"))
          .bind(_.addUuid("ident", ident))
    }
  }
}

class TrackingTicketRecomposer extends Recomposer[TrackingTicket] {
  val typeDescriptor = TypeDescriptor(classOf[TrackingTicket], 1)
  def recompose(from: RematerializationArray): AlmValidation[TrackingTicket] = {
    from.getString("type").bind {
      case "string" => from.getString("ident").map(StringTrackingTicket.apply)
      case "uuid" => from.getUuid("ident").map(UuidTrackingTicket.apply)
    }
  }
}
