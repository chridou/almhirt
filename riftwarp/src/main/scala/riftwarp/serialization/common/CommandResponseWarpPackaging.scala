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
    this.warpDescriptor ~> P("id", what.id.value) ~> With("why", what.why, RejectionReasonWarpPackaging)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandNotAccepted] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id")
        why <- lookup.getWith[RejectionReason]("why", RejectionReasonWarpPackaging)
      } yield CommandNotAccepted(CommandId(id), why)
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

object TrackedCommandTimedOutWarpPackaging extends WarpPacker[TrackedCommandTimedOut] with RegisterableWarpPacker with RegisterableWarpUnpacker[TrackedCommandTimedOut] {
  val warpDescriptor = WarpDescriptor("TrackedCommandTimedOut")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[TrackedCommandTimedOut]) :: Nil
  override def pack(what: TrackedCommandTimedOut)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("id", what.id.value)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TrackedCommandTimedOut] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id")
      } yield TrackedCommandTimedOut(CommandId(id))
    }
}

object TrackerFailedWarpPackaging extends WarpPacker[TrackerFailed] with RegisterableWarpPacker with RegisterableWarpUnpacker[TrackerFailed] {
  val warpDescriptor = WarpDescriptor("TrackerFailed")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[TrackerFailed]) :: Nil
  override def pack(what: TrackerFailed)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("id", what.id.value) ~> With("problem", what.problem, ProblemPackaging)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TrackerFailed] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id")
        problem <- lookup.getWith("problem", ProblemPackaging)
      } yield TrackerFailed(CommandId(id), problem)
    }
}

object TrackedCommandResponseWarpPackaging extends WarpPacker[TrackedCommandResponse] with RegisterableWarpPacker with RegisterableWarpUnpacker[TrackedCommandResponse] with DivertingWarpUnpacker[TrackedCommandResponse] with DivertingWarpUnpackerWithAutoRegistration[TrackedCommandResponse] {
  val warpDescriptor = WarpDescriptor("TrackedCommandResponse")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[TrackedCommandResponse]) :: Nil

  override def pack(what: TrackedCommandResponse)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case w: TrackedCommandResult ⇒ TrackedCommandResultWarpPackaging(w)
      case w: TrackedCommandTimedOut ⇒ TrackedCommandTimedOutWarpPackaging(w)
      case w: TrackerFailed ⇒ TrackerFailedWarpPackaging(w)
    }
  }
  
  override val unpackers = TrackedCommandResultWarpPackaging :: TrackedCommandTimedOutWarpPackaging :: TrackerFailedWarpPackaging :: Nil
}

object CommandResponseWarpPackaging extends WarpPacker[CommandResponse] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandResponse] with DivertingWarpUnpacker[CommandResponse] with DivertingWarpUnpackerWithAutoRegistration[CommandResponse] {
  val warpDescriptor = WarpDescriptor("CommandResponse")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandResponse]) :: Nil

  override def pack(what: CommandResponse)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case w: CommandAccepted ⇒ CommandAcceptedWarpPackaging(w)
      case w: CommandNotAccepted ⇒ CommandNotAcceptedWarpPackaging(w)
      case w: TrackedCommandResult ⇒ TrackedCommandResultWarpPackaging(w)
      case w: TrackedCommandTimedOut ⇒ TrackedCommandTimedOutWarpPackaging(w)
      case w: TrackerFailed ⇒ TrackerFailedWarpPackaging(w)
    }
  }
  
  override val unpackers = CommandAcceptedWarpPackaging :: CommandNotAcceptedWarpPackaging :: TrackedCommandResultWarpPackaging :: TrackedCommandTimedOutWarpPackaging :: TrackerFailedWarpPackaging :: Nil
}
