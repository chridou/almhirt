package riftwarp.serialization.common

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.tracking._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

object RejectionReasonTooBusyWarpPackaging extends WarpPacker[RejectionReason.TooBusy] with RegisterableWarpPacker with RegisterableWarpUnpacker[RejectionReason.TooBusy] {
  val warpDescriptor = WarpDescriptor("TooBusy")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[RejectionReason.TooBusy]) :: Nil
  override def pack(what: RejectionReason.TooBusy)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("msg", what.msg)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[RejectionReason.TooBusy] =
    withFastLookUp(from) { lookup ⇒
      for {
        msg <- lookup.getAs[String]("msg")
      } yield RejectionReason.TooBusy(msg)
    }
}

object RejectionReasonNotReadyWarpPackaging extends WarpPacker[RejectionReason.NotReady] with RegisterableWarpPacker with RegisterableWarpUnpacker[RejectionReason.NotReady] {
  val warpDescriptor = WarpDescriptor("NotReady")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[RejectionReason.NotReady]) :: Nil
  override def pack(what: RejectionReason.NotReady)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("msg", what.msg)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[RejectionReason.NotReady] =
    withFastLookUp(from) { lookup ⇒
      for {
        msg <- lookup.getAs[String]("msg")
      } yield RejectionReason.NotReady(msg)
    }
}

object RejectionReasonAProblemWarpPackaging extends WarpPacker[RejectionReason.AProblem] with RegisterableWarpPacker with RegisterableWarpUnpacker[RejectionReason.AProblem] {
  val warpDescriptor = WarpDescriptor("AProblem")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[RejectionReason.AProblem]) :: Nil
  override def pack(what: RejectionReason.AProblem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> With("problem", what.problem, ProblemPackaging)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[RejectionReason.AProblem] =
    withFastLookUp(from) { lookup ⇒
      for {
        problem <- lookup.getWith("problem", ProblemPackaging)
      } yield RejectionReason.AProblem(problem)
    }
}

object RejectionReasonWarpPackaging extends WarpPacker[RejectionReason] with RegisterableWarpPacker with RegisterableWarpUnpacker[RejectionReason] with DivertingWarpUnpacker[RejectionReason] with DivertingWarpUnpackerWithAutoRegistration[RejectionReason] {
  val warpDescriptor = WarpDescriptor("RejectionReason")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[RejectionReason]) :: Nil

  override def pack(what: RejectionReason)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case w: RejectionReason.TooBusy ⇒ RejectionReasonTooBusyWarpPackaging(w)
      case w: RejectionReason.NotReady ⇒ RejectionReasonNotReadyWarpPackaging(w)
      case w: RejectionReason.AProblem ⇒ RejectionReasonAProblemWarpPackaging(w)
    }
  }
  
  override val unpackers = RejectionReasonTooBusyWarpPackaging :: RejectionReasonNotReadyWarpPackaging :: RejectionReasonAProblemWarpPackaging :: Nil
}