package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.serialization.common.EventWarpPackagingTemplate
import riftwarp.std.WarpObjectLookUp
import riftwarp.serialization.common.ProblemPackaging

object CommandExecutedWarpPackaging extends EventWarpPackagingTemplate[CommandExecuted] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("CommandExecuted")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandExecuted]) :: Nil

  override def addEventParams(what: CommandExecuted, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~>
      P("commandId", what.commandId)

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[CommandExecuted] =
    for {
      commandId <- from.getAs[JUUID]("commandId")
    } yield CommandExecuted(header, commandId)
}

object CommandNotExecutedWarpPackaging extends EventWarpPackagingTemplate[CommandNotExecuted] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("CommandNotExecuted")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandNotExecuted]) :: Nil

  override def addEventParams(what: CommandNotExecuted, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~>
      P("commandId", what.commandId) ~>
      With("reason", what.reason, ProblemPackaging)

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[CommandNotExecuted] =
    for {
      commandId <- from.getAs[JUUID]("commandId")
      reason <- from.getWith("reason", ProblemPackaging)
    } yield CommandNotExecuted(header, commandId, reason)
}

object CommandHandledEventWarpPackaging extends WarpPacker[CommandHandledEvent] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandHandledEvent] with DivertingWarpUnpacker[CommandHandledEvent] with DivertingWarpUnpackerWithAutoRegistration[CommandHandledEvent] {
  val warpDescriptor = WarpDescriptor("CommandHandledEvent")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandHandledEvent]) :: Nil
  override def pack(what: CommandHandledEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    what match {
      case x: CommandExecuted => CommandExecutedWarpPackaging(x)
      case x: CommandNotExecuted => CommandNotExecutedWarpPackaging(x)
    }

  def unpackers = CommandExecutedWarpPackaging :: CommandNotExecutedWarpPackaging :: Nil
}