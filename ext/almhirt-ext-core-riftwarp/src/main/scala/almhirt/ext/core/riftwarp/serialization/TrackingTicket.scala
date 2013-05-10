package almhirt.ext.core.riftwarp.serialization

import scalaz._
import scalaz.Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._
import almhirt.util.StringTrackingTicket

object StringTrackingTicketDecomposer extends Decomposer[StringTrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[StringTrackingTicket])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: StringTrackingTicket, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addString("ident", what.ident).ok
}

object UuidTrackingTicketDecomposer extends Decomposer[UuidTrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[UuidTrackingTicket])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: UuidTrackingTicket, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addUuid("ident", what.ident).ok
}

object TrackingTicketDecomposer extends Decomposer[TrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[TrackingTicket])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: TrackingTicket, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case ticket @ UuidTrackingTicket(_) => into.includeDirect(ticket, UuidTrackingTicketDecomposer)
      case ticket @ StringTrackingTicket(_) => into.includeDirect(ticket, StringTrackingTicketDecomposer)
    }
  }
}

object StringTrackingTicketRecomposer extends Recomposer[StringTrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[StringTrackingTicket])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[StringTrackingTicket] =
    from.getString("ident").map(StringTrackingTicket.apply)
}

object UuidTrackingTicketRecomposer extends Recomposer[UuidTrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[UuidTrackingTicket])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[UuidTrackingTicket] =
    from.getUuid("ident").map(UuidTrackingTicket.apply)
}

object TrackingTicketRecomposer extends DivertingRecomposer[TrackingTicket] {
  val riftDescriptor = RiftDescriptor(classOf[TrackingTicket])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      StringTrackingTicketRecomposer.riftDescriptor -> StringTrackingTicketRecomposer,
      UuidTrackingTicketRecomposer.riftDescriptor -> UuidTrackingTicketRecomposer).lift
}
