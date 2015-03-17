package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.std._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.problem._
import riftwarp._
import riftwarp.std.kit._

object HasAThrowableDescribedPacker extends WarpPacker[HasAThrowableDescribed] with SimpleWarpPacker[HasAThrowableDescribed] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[HasAThrowableDescribed].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[HasAThrowableDescribed]) :: Nil
  override def pack(what: HasAThrowableDescribed)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("classname", what.classname) ~>
      P("message", what.message) ~>
      P("stacktrace", what.stacktrace) ~>
      WithOpt("cause", what.cause, this)
  }
}

object HasAThrowablePacker extends WarpPacker[HasAThrowable] with SimpleWarpPacker[HasAThrowable] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[HasAThrowable].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[HasAThrowable]) :: Nil
  override def pack(what: HasAThrowable)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    HasAThrowableDescribedPacker { what.toDescription }
  }
}

object ThrowableRepresentationPacker extends WarpPacker[ThrowableRepresentation] with SimpleWarpPacker[ThrowableRepresentation] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[ThrowableRepresentation].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ThrowableRepresentation]) :: Nil
  override def pack(what: ThrowableRepresentation)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case hat: HasAThrowable ⇒ HasAThrowablePacker(hat)
      case hatd: HasAThrowableDescribed ⇒ HasAThrowableDescribedPacker(hatd)
    }
  }
}

object CauseIsThrowablePacker extends WarpPacker[CauseIsThrowable] with SimpleWarpPacker[CauseIsThrowable] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsThrowable].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CauseIsThrowable]) :: Nil
  override def pack(what: CauseIsThrowable)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> With("representation", what.representation, ThrowableRepresentationPacker)
  }
}

object CauseIsProblemPacker extends WarpPacker[CauseIsProblem] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsProblem].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CauseIsProblem]) :: Nil
  override def pack(what: CauseIsProblem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> With("problem", what.problem, ProblemPackaging)
  }
}

object ProblemCausePacker extends WarpPacker[ProblemCause] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[ProblemCause].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ProblemCause]) :: Nil
  override def pack(what: ProblemCause)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case cip: CauseIsProblem ⇒ CauseIsProblemPacker(cip)
      case cit: CauseIsThrowable ⇒ CauseIsThrowablePacker(cit)
    }
  }
}

object HasAThrowableDescribedUnpacker extends RegisterableWarpUnpacker[HasAThrowableDescribed] {
  val warpDescriptor = WarpDescriptor(classOf[HasAThrowableDescribed].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[HasAThrowableDescribed]) :: Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[HasAThrowableDescribed] = {
    withFastLookUp(from) { lookup ⇒
      for {
        classname ← lookup.getAs[String]("classname")
        message ← lookup.getAs[String]("message")
        stacktrace ← lookup.getAs[String]("stacktrace")
        cause ← lookup.tryGetWith("cause", HasAThrowableDescribedUnpacker)
      } yield HasAThrowableDescribed(classname, message, stacktrace, cause)
    }
  }
}

object CauseIsThrowableUnpacker extends RegisterableWarpUnpacker[CauseIsThrowable] {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsThrowable].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CauseIsThrowable]) :: Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CauseIsThrowable] = {
    withFastLookUp(from) { lookup ⇒
      lookup.getWith("representation", HasAThrowableDescribedUnpacker).map(desc ⇒
        CauseIsThrowable(desc))
    }
  }
}

object CauseIsProblemUnpacker extends RegisterableWarpUnpacker[CauseIsProblem] {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsProblem].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CauseIsProblem]) :: Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CauseIsProblem] = {
    withFastLookUp(from) { lookup ⇒
      lookup.getTyped[Problem]("problem").map(prob ⇒
        CauseIsProblem(prob))
    }
  }
}

object ProblemCauseUnpacker extends RegisterableWarpUnpacker[ProblemCause] with DivertingWarpUnpacker[ProblemCause] with DivertingWarpUnpackerWithAutoRegistration[ProblemCause] {
  val warpDescriptor = WarpDescriptor(classOf[ProblemCause].getSimpleName())
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ProblemCause]) :: Nil
  override val unpackers = CauseIsProblemUnpacker :: CauseIsThrowableUnpacker :: Nil
}