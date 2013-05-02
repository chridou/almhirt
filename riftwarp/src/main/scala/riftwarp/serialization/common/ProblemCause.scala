package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._

object HasAThrowableDescribedPacker extends WarpPacker[HasAThrowableDescribed] with SimpleWarpPacker[HasAThrowableDescribed] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[HasAThrowableDescribed])
  val alternativeWarpDescriptors = Nil
  override def pack(what: HasAThrowableDescribed)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("classname", what.classname) ~>
      P("mesage", what.message) ~>
      P("stacktrace", what.stacktrace) ~>
      WithOpt("cause", what.cause, this)
  }
}

object HasAThrowablePacker extends WarpPacker[HasAThrowable] with SimpleWarpPacker[HasAThrowable] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[HasAThrowable])
  val alternativeWarpDescriptors = Nil
  override def pack(what: HasAThrowable)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    HasAThrowableDescribedPacker { what.toDescription }
  }
}

object ThrowableRepresentationPacker extends WarpPacker[ThrowableRepresentation] with SimpleWarpPacker[ThrowableRepresentation] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[ThrowableRepresentation])
  val alternativeWarpDescriptors = Nil
  override def pack(what: ThrowableRepresentation)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case hat: HasAThrowable => HasAThrowablePacker(hat)
      case hatd: HasAThrowableDescribed => HasAThrowableDescribedPacker(hatd)
    }
  }
}

object CauseIsThrowablePacker extends WarpPacker[CauseIsThrowable] with SimpleWarpPacker[CauseIsThrowable] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsThrowable])
  val alternativeWarpDescriptors = Nil
  override def pack(what: CauseIsThrowable)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> With("representation", what.representation, ThrowableRepresentationPacker)
  }
}

object CauseIsProblemPacker extends WarpPacker[CauseIsProblem] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsProblem])
  val alternativeWarpDescriptors = Nil
  override def pack(what: CauseIsProblem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> LookUp("problem", what.problem)
  }
}

object ProblemCausePacker extends WarpPacker[ProblemCause] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[ProblemCause])
  val alternativeWarpDescriptors = Nil
  override def pack(what: ProblemCause)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case cip: CauseIsProblem => CauseIsProblemPacker(cip)
      case cit: CauseIsThrowable => CauseIsThrowablePacker(cit)
    }
  }
}

object HasAThrowableDescribedUnpacker extends RegisterableWarpUnpacker[HasAThrowableDescribed] {
  val warpDescriptor = WarpDescriptor(classOf[HasAThrowableDescribed])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[HasAThrowableDescribed] = {
    withFastLookUp(from) { lookup =>
      for {
        classname <- lookup.getAs[String]("classname")
        message <- lookup.getAs[String]("message")
        stacktrace <- lookup.getAs[String]("stacktrace")
        cause <- lookup.tryGetWith("cause", HasAThrowableDescribedUnpacker)
      } yield HasAThrowableDescribed(classname, message, stacktrace, cause)
    }
  }
}

object CauseIsThrowableUnpacker extends RegisterableWarpUnpacker[CauseIsThrowable] {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsThrowable])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CauseIsThrowable] = {
    withFastLookUp(from) { lookup =>
      lookup.getWith("representation", HasAThrowableDescribedUnpacker).map(desc =>
        CauseIsThrowable(desc))
    }
  }
}

object CauseIsProblemUnpacker extends RegisterableWarpUnpacker[CauseIsProblem] {
  val warpDescriptor = WarpDescriptor(classOf[CauseIsProblem])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CauseIsProblem] = {
    withFastLookUp(from) { lookup =>
      lookup.getTyped[Problem]("problem").map(prob =>
        CauseIsProblem(prob))
    }
  }
}

object ProblemCauseUnpacker extends RegisterableWarpUnpacker[ProblemCause] {
  val warpDescriptor = WarpDescriptor(classOf[ProblemCause])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ProblemCause] = {
    for {
      wo <- from.asWarpObject
      desc <- wo.getWarpDescriptor
      res <- if (desc == WarpDescriptor(classOf[CauseIsProblem]))
        CauseIsProblemUnpacker.unpack(from)
      else if (desc == WarpDescriptor(classOf[CauseIsThrowable]))
        CauseIsThrowableUnpacker.unpack(from)
      else
        BadDataProblem(s"'$desc' is not a valid identifier for ProblemCause").withIdentifier("type").failure
    } yield res
  }
}