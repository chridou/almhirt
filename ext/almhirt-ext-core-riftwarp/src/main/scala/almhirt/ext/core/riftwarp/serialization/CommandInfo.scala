package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.util._
import riftwarp._
import riftwarp.std.kit._
import almhirt.commanding.DomainCommand
import almhirt.domain.AggregateRootRef

object FullComandInfoWarpPacker extends WarpPacker[FullComandInfo] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[FullComandInfo])
  val alternativeWarpDescriptors = Nil
  override def pack(what: FullComandInfo)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      LookUp("command", what.command)
  }
}

object HeadCommandInfoWarpPacker extends WarpPacker[HeadCommandInfo] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[HeadCommandInfo])
  val alternativeWarpDescriptors = Nil
  override def pack(what: HeadCommandInfo)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("commandId", what.commandId) ~>
      P("commandType", what.commandType) ~>
      WithOpt("aggRef", what.aggRef, AggregateRootRefWarpPacker)
  }
}

object CommandInfoWarpPacker extends WarpPacker[CommandInfo] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[CommandInfo])
  val alternativeWarpDescriptors = Nil
  override def pack(what: CommandInfo)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case ci: FullComandInfo => FullComandInfoWarpPacker.pack(ci)
      case ci: HeadCommandInfo => HeadCommandInfoWarpPacker.pack(ci)
    }
  }
}

object FullComandInfoWarpUnpacker extends RegisterableWarpUnpacker[FullComandInfo] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[FullComandInfo])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[FullComandInfo] = {
    withFastLookUp(from) { lookup =>
      lookup.getTyped("command", None).map(FullComandInfo.apply)
    }
  }
}

object HeadCommandInfoWarpUnpacker extends RegisterableWarpUnpacker[HeadCommandInfo] {
  val warpDescriptor = WarpDescriptor(classOf[HeadCommandInfo])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[HeadCommandInfo] = {
    withFastLookUp(from) { lookup =>
      for {
        commandId <- lookup.getAs[JUUID]("commandId")
        commandType <- lookup.getAs[String]("commandType")
        aggRef <- lookup.tryGetWith[AggregateRootRef]("aggRef", AggregateRootRefWarpUnpacker)
      } yield HeadCommandInfo(commandId, commandType, aggRef)
    }
  }
}

object CommandInfoWarpUnpacker extends RegisterableWarpUnpacker[CommandInfo] with DivertingWarpUnpacker[CommandInfo] {
  val warpDescriptor = WarpDescriptor(classOf[CommandInfo])
  val alternativeWarpDescriptors = Nil
  val divert =
    Map(
      FullComandInfoWarpUnpacker.warpDescriptor -> FullComandInfoWarpUnpacker,
      HeadCommandInfoWarpUnpacker.warpDescriptor -> HeadCommandInfoWarpUnpacker).lift
}