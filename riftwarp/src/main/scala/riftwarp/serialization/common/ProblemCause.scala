package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.warpbuilder._

object HasAThrowableDescribedPacker extends WarpPacker[HasAThrowableDescribed] with SimpleWarpPacker[HasAThrowableDescribed] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[HasAThrowableDescribed])
  val alternativeRiftDescriptors = Nil
  override def pack(what: HasAThrowableDescribed)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.riftDescriptor ⟿
      P("classname", what.classname) ⟿
      P("mesage", what.message) ⟿
      P("stacktrace", what.stacktrace) ⟿
      WithOpt("cause", what.cause, this)
  }
}

object HasAThrowablePacker extends WarpPacker[HasAThrowable] with SimpleWarpPacker[HasAThrowable] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[HasAThrowable])
  val alternativeRiftDescriptors = Nil
  override def pack(what: HasAThrowable)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    HasAThrowableDescribedPacker { what.toDescription }
  }
}

object ThrowableRepresentationPacker extends WarpPacker[ThrowableRepresentation] with SimpleWarpPacker[ThrowableRepresentation] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[ThrowableRepresentation])
  val alternativeRiftDescriptors = Nil
  override def pack(what: ThrowableRepresentation)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case hat: HasAThrowable => HasAThrowablePacker(hat)
      case hatd: HasAThrowableDescribed => HasAThrowableDescribedPacker(hatd)
    }
  }
}

object CauseIsThrowablePacker extends WarpPacker[CauseIsThrowable] with SimpleWarpPacker[CauseIsThrowable] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[CauseIsThrowable])
  val alternativeRiftDescriptors = Nil
  override def pack(what: CauseIsThrowable)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.riftDescriptor ⟿ With("representation", what.representation, ThrowableRepresentationPacker)
  }
}

object CauseIsProblemPacker extends WarpPacker[CauseIsProblem] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[CauseIsProblem])
  val alternativeRiftDescriptors = Nil
  override def pack(what: CauseIsProblem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.riftDescriptor ⟿ LookUp("problem", what.problem)
  }
}

object ProblemCausePacker extends WarpPacker[ProblemCause] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[ProblemCause])
  val alternativeRiftDescriptors = Nil
  override def pack(what: ProblemCause)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case cip : CauseIsProblem => CauseIsProblemPacker(cip)
      case cit : CauseIsThrowable => CauseIsThrowablePacker(cit)
    }
  }
}
//
//object HasAThrowableDescribedRecomposer extends Recomposer[HasAThrowableDescribed] {
//  val riftDescriptor = RiftDescriptor(classOf[HasAThrowableDescribed])
//  val alternativeRiftDescriptors = Nil
//  def recompose(from: Extractor): AlmValidation[HasAThrowableDescribed] = {
//    val classname = from.getString("classname").toAgg
//    val message = from.getString("message").toAgg
//    val stacktrace = from.getString("stacktrace").toAgg
//    val cause = from.tryGetWith[HasAThrowableDescribed]("cause", this.recompose).toAgg
//    (classname |@| message |@| stacktrace |@| cause)(HasAThrowableDescribed.apply)
//  }
//}
//
//object CauseIsThrowableRecomposer extends Recomposer[CauseIsThrowable] {
//  val riftDescriptor = RiftDescriptor(classOf[CauseIsThrowable])
//  val alternativeRiftDescriptors = Nil
//  def recompose(from: Extractor): AlmValidation[CauseIsThrowable] = {
//    from.getWith("representation", HasAThrowableDescribedRecomposer.recompose).map(desc =>
//      CauseIsThrowable(desc))
//  }
//}
//
//object CauseIsProblemRecomposer extends Recomposer[CauseIsProblem] {
//  val riftDescriptor = RiftDescriptor(classOf[CauseIsProblem])
//  val alternativeRiftDescriptors = Nil
//  def recompose(from: Extractor): AlmValidation[CauseIsProblem] = {
//    from.getComplexByTag[Problem]("problem", None).map(prob =>
//      CauseIsProblem(prob))
//  }
//}
//
//object ProblemCauseRecomposer extends Recomposer[ProblemCause] {
//  val riftDescriptor = RiftDescriptor(classOf[ProblemCause])
//  val alternativeRiftDescriptors = Nil
//  def recompose(from: Extractor): AlmValidation[ProblemCause] = {
//    from.getRiftDescriptor.flatMap(desc =>
//      if (desc == RiftDescriptor(classOf[CauseIsProblem]))
//        CauseIsProblemRecomposer.recompose(from)
//      else if (desc == RiftDescriptor(classOf[CauseIsThrowable]))
//        CauseIsThrowableRecomposer.recompose(from)
//      else
//        BadDataProblem(s"'$desc' is not a valid identifier for ProblemCause").withIdentifier("type").failure)
//  }
//}