package riftwarp.serialization.common

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.tracking.CommandStatusChanged
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

object CommandStatusChangedWarpPackaging extends EventWarpPackagingTemplate[CommandStatusChanged] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("CommandStatusChanged")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandStatusChanged]) :: Nil

  override def addEventParams(what: CommandStatusChanged, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~> 
  	With("commandHeader", what.commandHeader, CommandHeaderWarpPackaging) ~>
  	With("status", what.status, CommandStatusWarpPackaging)
  	

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[CommandStatusChanged] =
      for {
        commandHeader <- from.getWith("commandHeader", CommandHeaderWarpPackaging)
        status <- from.getWith("status", CommandStatusWarpPackaging)
      } yield CommandStatusChanged(header, commandHeader, status)
}