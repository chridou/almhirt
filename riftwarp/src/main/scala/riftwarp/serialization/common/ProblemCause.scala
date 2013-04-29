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
    this.riftDescriptor ~+
      P("classname", what.classname) ~+
      P("mesage", what.message) ~+
      P("stacktrace", what.stacktrace) ~+
      WithOpt("cause", what.cause, this)
  }
}

//object HasAThrowableDecomposer extends Decomposer[HasAThrowable] {
//  val riftDescriptor = RiftDescriptor(classOf[HasAThrowable])
//  val alternativeRiftDescriptors = Nil
//  def decompose[TDimension <: RiftDimension](what: HasAThrowable, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
//    into.includeDirect(what.toDescription, HasAThrowableDescribedDecomposer)
//  }
//}
//
//object ThrowableRepresentationDecomposer extends Decomposer[ThrowableRepresentation] {
//  val riftDescriptor = RiftDescriptor(classOf[ThrowableRepresentation])
//  val alternativeRiftDescriptors = Nil
//  def decompose[TDimension <: RiftDimension](what: ThrowableRepresentation, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
//    what match {
//      case hat @ HasAThrowable(_) => into.includeDirect(hat, HasAThrowableDecomposer)
//      case hatd @ HasAThrowableDescribed(_, _, _, _) => into.includeDirect(hatd, HasAThrowableDescribedDecomposer)
//    }
//  }
//}
//
//object CauseIsThrowableDecomposer extends Decomposer[CauseIsThrowable] {
//  val riftDescriptor = RiftDescriptor(classOf[CauseIsThrowable])
//  val alternativeRiftDescriptors = Nil
//  def decompose[TDimension <: RiftDimension](what: CauseIsThrowable, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
//    into.addRiftDescriptor(this.riftDescriptor)
//      .addWith("representation", what.representation, ThrowableRepresentationDecomposer)
//  }
//}
//
//object CauseIsProblemDecomposer extends Decomposer[CauseIsProblem] {
//  val riftDescriptor = RiftDescriptor(classOf[CauseIsProblem])
//  val alternativeRiftDescriptors = Nil
//  def decompose[TDimension <: RiftDimension](what: CauseIsProblem, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
//    into.addRiftDescriptor(this.riftDescriptor).addComplex("problem", what.problem, None)
//  }
//}
//
//object ProblemCauseDecomposer extends Decomposer[ProblemCause] {
//  val riftDescriptor = RiftDescriptor(classOf[ProblemCause])
//  val alternativeRiftDescriptors = Nil
//  def decompose[TDimension <: RiftDimension](what: ProblemCause, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
//    what match {
//      case cip @ CauseIsProblem(_) => into.includeDirect(cip, CauseIsProblemDecomposer)
//      case cit @ CauseIsThrowable(_) => into.includeDirect(cit, CauseIsThrowableDecomposer)
//    }
//  }
//}
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