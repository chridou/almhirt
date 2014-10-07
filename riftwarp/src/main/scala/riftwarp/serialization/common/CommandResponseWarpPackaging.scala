package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.tracking._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

object CommandAcceptedWarpPackaging extends WarpPacker[CommandAccepted] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandAccepted] {
  val warpDescriptor = WarpDescriptor("CommandAccepted")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandAccepted]) :: Nil
  override def pack(what: CommandAccepted)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("id", what.id.value)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandAccepted] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id")
      } yield CommandAccepted(CommandId(id))
    }
}

object CommandNotAcceptedWarpPackaging extends WarpPacker[CommandNotAccepted] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandNotAccepted] {
  val warpDescriptor = WarpDescriptor("CommandNotAccepted")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandNotAccepted]) :: Nil
  override def pack(what: CommandNotAccepted)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("id", what.id.value) ~> With("problem", what.problem, ProblemPackaging)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandNotAccepted] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id")
        problem <- lookup.getWith("problem", ProblemPackaging)
      } yield CommandNotAccepted(CommandId(id), problem)
    }
}

object TrackedCommandResultWarpPackaging extends WarpPacker[TrackedCommandResult] with RegisterableWarpPacker with RegisterableWarpUnpacker[TrackedCommandResult] {
  val warpDescriptor = WarpDescriptor("TrackedCommandResult")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[TrackedCommandResult]) :: Nil
  override def pack(what: TrackedCommandResult)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("id", what.id.value) ~> With("status", what.status, CommandStatusCommandResultWarpPackaging)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TrackedCommandResult] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id")
        status <- lookup.getWith("status", CommandStatusCommandResultWarpPackaging)
      } yield TrackedCommandResult(CommandId(id), status)
    }
}

object TrackingFailedWarpPackaging extends WarpPacker[TrackingFailed] with RegisterableWarpPacker with RegisterableWarpUnpacker[TrackingFailed] {
  val warpDescriptor = WarpDescriptor("TrackingFailed")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[TrackingFailed]) :: Nil
  override def pack(what: TrackingFailed)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("id", what.id.value) ~> With("problem", what.problem, ProblemPackaging)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TrackingFailed] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id")
        problem <- lookup.getWith("problem", ProblemPackaging)
      } yield TrackingFailed(CommandId(id), problem)
    }
}

object TrackedCommandResponseWarpPackaging extends WarpPacker[TrackedCommandResponse] with RegisterableWarpPacker with RegisterableWarpUnpacker[TrackedCommandResponse] with DivertingWarpUnpacker[TrackedCommandResponse] with DivertingWarpUnpackerWithAutoRegistration[TrackedCommandResponse] {
  val warpDescriptor = WarpDescriptor("TrackedCommandResponse")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[TrackedCommandResponse]) :: Nil

  override def pack(what: TrackedCommandResponse)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case w: TrackedCommandResult ⇒ TrackedCommandResultWarpPackaging(w)
      case w: TrackingFailed ⇒ TrackingFailedWarpPackaging(w)
   }
  }
  
  override val unpackers = TrackedCommandResultWarpPackaging :: TrackingFailedWarpPackaging :: Nil
}

object CommandResponseWarpPackaging extends WarpPacker[CommandResponse] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandResponse] with DivertingWarpUnpacker[CommandResponse] with DivertingWarpUnpackerWithAutoRegistration[CommandResponse] {
  val warpDescriptor = WarpDescriptor("CommandResponse")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandResponse]) :: Nil

  override def pack(what: CommandResponse)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case w: CommandAccepted ⇒ CommandAcceptedWarpPackaging(w)
      case w: CommandNotAccepted ⇒ CommandNotAcceptedWarpPackaging(w)
      case w: TrackedCommandResult ⇒ TrackedCommandResultWarpPackaging(w)
      case w: TrackingFailed ⇒ TrackingFailedWarpPackaging(w)
    }
  }
  
  override val unpackers = CommandAcceptedWarpPackaging :: CommandNotAcceptedWarpPackaging :: TrackedCommandResultWarpPackaging :: TrackingFailedWarpPackaging :: Nil
}
