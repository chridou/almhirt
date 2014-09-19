package riftwarp.serialization.common

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.problem._
import riftwarp._
import riftwarp.std.kit._
import almhirt.tracking.CommandStatus

object CommandStatusInitiatedWarpPackaging extends WarpPacker[CommandStatus.Initiated.type] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandStatus.Initiated.type] {
  val warpDescriptor = WarpDescriptor("CommandStatus_Initiated")
  val alternativeWarpDescriptors = WarpDescriptor(CommandStatus.Initiated.getClass().getName()) :: Nil
  override def pack(what: CommandStatus.Initiated.type)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    WarpObject(this.warpDescriptor).success

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandStatus.Initiated.type] =
    CommandStatus.Initiated.success
}

object CommandStatusExecutedWarpPackaging extends WarpPacker[CommandStatus.Executed.type] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandStatus.Executed.type] {
  val warpDescriptor = WarpDescriptor("CommandStatus_Executed")
  val alternativeWarpDescriptors = WarpDescriptor(CommandStatus.Executed.getClass().getName()) :: Nil
  override def pack(what: CommandStatus.Executed.type)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    WarpObject(this.warpDescriptor).success

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandStatus.Executed.type] =
    CommandStatus.Executed.success
}

object CommandStatusNotExecutedWarpPackaging extends WarpPacker[CommandStatus.NotExecuted] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandStatus.NotExecuted] {
  val warpDescriptor = WarpDescriptor("CommandStatus_NotExecuted")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandStatus.NotExecuted].getName()) :: Nil
  override def pack(what: CommandStatus.NotExecuted)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~> With("cause", what.cause, ProblemCausePacker)

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandStatus.NotExecuted] =
    withFastLookUp(from) { lookup ⇒
      for {
        cause <- lookup.getWith("cause", ProblemCauseUnpacker)
      } yield CommandStatus.NotExecuted(cause)
    }
}

object CommandStatusCommandResultWarpPackaging extends WarpPacker[CommandStatus.CommandResult] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandStatus.CommandResult] with DivertingWarpUnpacker[CommandStatus.CommandResult] with DivertingWarpUnpackerWithAutoRegistration[CommandStatus.CommandResult] {
  val warpDescriptor = WarpDescriptor("CommandStatus_CommandResult")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandStatus.CommandResult]) :: WarpDescriptor(classOf[CommandStatus.CommandResult].getSimpleName()) :: Nil

  override def pack(what: CommandStatus.CommandResult)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case CommandStatus.Executed ⇒ CommandStatusExecutedWarpPackaging(CommandStatus.Executed)
      case w: CommandStatus.NotExecuted ⇒ CommandStatusNotExecutedWarpPackaging(w)
    }
  }
  
  override val unpackers = CommandStatusExecutedWarpPackaging :: CommandStatusNotExecutedWarpPackaging :: Nil
}

object CommandStatusWarpPackaging extends WarpPacker[CommandStatus] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandStatus] with DivertingWarpUnpacker[CommandStatus] with DivertingWarpUnpackerWithAutoRegistration[CommandStatus] {
  val warpDescriptor = WarpDescriptor("CommandStatus")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandStatus]) :: WarpDescriptor(classOf[CommandStatus].getSimpleName()) :: Nil

  override def pack(what: CommandStatus)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case CommandStatus.Initiated ⇒ CommandStatusInitiatedWarpPackaging(CommandStatus.Initiated)
      case CommandStatus.Executed ⇒ CommandStatusExecutedWarpPackaging(CommandStatus.Executed)
      case w: CommandStatus.NotExecuted ⇒ CommandStatusNotExecutedWarpPackaging(w)
    }
  }
  
  override val unpackers = CommandStatusInitiatedWarpPackaging :: CommandStatusExecutedWarpPackaging :: CommandStatusNotExecutedWarpPackaging :: Nil
}