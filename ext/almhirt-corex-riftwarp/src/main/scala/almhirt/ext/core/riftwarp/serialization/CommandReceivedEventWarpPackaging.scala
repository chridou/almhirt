package almhirt.ext.core.riftwarp.serialization

import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.serialization.common.EventWarpPackagingTemplate
import riftwarp.std.WarpObjectLookUp

object CommandReceivedWarpPackaging extends EventWarpPackagingTemplate[CommandReceived] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("CommandReceived")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandReceived]) :: Nil

  override def addEventParams(what: CommandReceived, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~>
      LookUp("command", what.command)

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[CommandReceived] =
    for {
      command <- from.getTyped[Command]("command")
    } yield CommandReceived(header, command)
}

object CommandReceivedAsHeaderWarpPackaging extends EventWarpPackagingTemplate[CommandReceivedAsHeader] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("CommandReceivedAsHeader")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandReceivedAsHeader]) :: Nil

  override def addEventParams(what: CommandReceivedAsHeader, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~>
      LookUp("commandHeader", what.commandHeader) ~>
      P("commandType", what.commandType)

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[CommandReceivedAsHeader] =
    for {
      commandHeader <- from.getTyped[CommandHeader]("commandHeader")
      commandType <- from.getAs[String]("commandType")
    } yield CommandReceivedAsHeader(header, commandHeader, commandType)
}

object CommandReceivedEventWarpPackaging extends WarpPacker[CommandReceivedEvent] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandReceivedEvent]  with DivertingWarpUnpacker[CommandReceivedEvent] with DivertingWarpUnpackerWithAutoRegistration[CommandReceivedEvent]{
  val warpDescriptor = WarpDescriptor("CommandReceivedEvent")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandReceivedEvent]) :: Nil
  override def pack(what: CommandReceivedEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    what match {
      case x: CommandReceived => CommandReceivedWarpPackaging(x)
      case x: CommandReceivedAsHeader => CommandReceivedAsHeaderWarpPackaging(x)
    }

  def unpackers = CommandReceivedWarpPackaging :: CommandReceivedAsHeaderWarpPackaging :: Nil
}