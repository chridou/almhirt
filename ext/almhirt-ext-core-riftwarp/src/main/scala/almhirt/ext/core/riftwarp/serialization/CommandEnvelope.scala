package almhirt.ext.core.riftwarp.serialization

import scalaz._
import scalaz.Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import almhirt.commanding._
import almhirt.util._
import almhirt.commanding.CommandEnvelope

object CommandEnvelopeWarpPacker extends WarpPacker[CommandEnvelope] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[CommandEnvelope])
  val alternativeWarpDescriptors = Nil
  override def pack(what: CommandEnvelope)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      LookUp("command", what.command) ~>
      WithOpt("ticket", what.ticket, TrackingTicketWarpPacker)
  }
}

object CommandEnvelopeWarpUnpacker extends RegisterableWarpUnpacker[CommandEnvelope] {
  val warpDescriptor = WarpDescriptor(classOf[CommandEnvelope])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandEnvelope] = {
    withFastLookUp(from) { lookup =>
      for {
        command <- lookup.getTyped[Command]("command")
        ticket <- lookup.tryGetWith("ticket", TrackingTicketWarpUnpacker)
      } yield CommandEnvelope(command, ticket)
    }
  }
}