package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import scala.concurrent.duration._
import almhirt.common._
import almhirt.util._
import riftwarp._
import riftwarp.std.kit._

object CommandWithMaxResponseDurationWarpPacker extends WarpPacker[CommandWithMaxResponseDuration] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("CommandWithMaxResponseDuration")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[FullComandInfo]) :: Nil
  override def pack(what: CommandWithMaxResponseDuration)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      LookUp("command", what.command) ~>
      POpt("maxResponseDuration", what.maxResponseDuration)
  }
}

object CommandWithMaxResponseDurationUnpacker extends RegisterableWarpUnpacker[CommandWithMaxResponseDuration] {
  val warpDescriptor = WarpDescriptor("CommandWithMaxResponseDuration")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[FullComandInfo]) :: Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandWithMaxResponseDuration] = {
    withFastLookUp(from) { lookup =>
      for {
        command <- lookup.getTyped[Command]("command")
        maxResponseDuration <- lookup.tryGetAs[FiniteDuration]("commandType")
      } yield CommandWithMaxResponseDuration(command, maxResponseDuration)
    }
  }
}

