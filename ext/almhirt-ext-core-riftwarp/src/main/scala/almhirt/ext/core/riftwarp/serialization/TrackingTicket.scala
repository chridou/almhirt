package almhirt.ext.core.riftwarp.serialization

import java.util.{UUID => JUUID}
import scalaz._
import scalaz.Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import almhirt.util._
import almhirt.util.StringTrackingTicket

object StringTrackingTicketWarpPacker extends WarpPacker[StringTrackingTicket] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[StringTrackingTicket])
  val alternativeWarpDescriptors = Nil
  override def pack(what: StringTrackingTicket)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = 
    this.warpDescriptor ~> P("ident", what.ident)
}

object UuidTrackingTicketWarpPacker extends WarpPacker[UuidTrackingTicket] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[UuidTrackingTicket])
  val alternativeWarpDescriptors = Nil
  override def pack(what: UuidTrackingTicket)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = 
    this.warpDescriptor ~> P("ident", what.ident)
}

object TrackingTicketWarpPacker extends WarpPacker[TrackingTicket] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TrackingTicket])
  val alternativeWarpDescriptors = Nil
  override def pack(what: TrackingTicket)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case ticket : UuidTrackingTicket => UuidTrackingTicketWarpPacker(ticket)
      case ticket : StringTrackingTicket => StringTrackingTicketWarpPacker(ticket)
    }
  }
}

object StringTrackingTicketWarpUnpacker extends RegisterableWarpUnpacker[StringTrackingTicket] {
  val warpDescriptor = WarpDescriptor(classOf[StringTrackingTicket])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[StringTrackingTicket] =
    withFastLookUp(from) { lookup =>
    lookup.getAs[String]("ident").map(StringTrackingTicket.apply)
  }
}

object UuidTrackingTicketWarpUnpacker extends RegisterableWarpUnpacker[UuidTrackingTicket] {
  val warpDescriptor = WarpDescriptor(classOf[UuidTrackingTicket])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[UuidTrackingTicket] =
    withFastLookUp(from) { lookup =>
    lookup.getAs[JUUID]("ident").map(UuidTrackingTicket.apply)
  }
}

object TrackingTicketWarpUnpacker extends RegisterableWarpUnpacker[TrackingTicket] with DivertingWarpUnpacker[TrackingTicket] {
  val warpDescriptor = WarpDescriptor(classOf[TrackingTicket])
  val alternativeWarpDescriptors = Nil
  val divert =
    Map(
      StringTrackingTicketWarpUnpacker.warpDescriptor -> StringTrackingTicketWarpUnpacker,
      UuidTrackingTicketWarpUnpacker.warpDescriptor -> UuidTrackingTicketWarpUnpacker).lift
}
