package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

object HasAThrowableDescribedDecomposer extends Decomposer[HasAThrowableDescribed] {
  val riftDescriptor = RiftDescriptor(classOf[HasAThrowableDescribed])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: HasAThrowableDescribed, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addString("classname", what.classname)
      .addString("message", what.message)
      .addString("stacktrace", what.stacktrace)
      .addOptionalComplexSelective("cause", this, what.cause)
  }
}

object HasAThrowableDecomposer extends Decomposer[HasAThrowable] {
  val riftDescriptor = RiftDescriptor(classOf[HasAThrowable])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: HasAThrowable, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.includeDirect(what.toDescription, HasAThrowableDescribedDecomposer)
  }
}

object ThrowableRepresentationDecomposer extends Decomposer[ThrowableRepresentation] {
  val riftDescriptor = RiftDescriptor(classOf[ThrowableRepresentation])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: ThrowableRepresentation, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case hat @ HasAThrowable(_) => into.includeDirect(hat, HasAThrowableDecomposer)
      case hatd @ HasAThrowableDescribed(_, _, _, _) => into.includeDirect(hatd, HasAThrowableDescribedDecomposer)
    }
  }
}

object CauseIsThrowableDecomposer extends Decomposer[CauseIsThrowable] {
  val riftDescriptor = RiftDescriptor(classOf[CauseIsThrowable])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: CauseIsThrowable, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addComplexSelective("representation", ThrowableRepresentationDecomposer, what.representation)
  }
}

object CauseIsProblemDecomposer extends Decomposer[CauseIsProblem] {
  val riftDescriptor = RiftDescriptor(classOf[CauseIsProblem])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: CauseIsProblem, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor).addComplex("problem", what.problem, None)
  }
}

object ProblemCauseDecomposer extends Decomposer[ProblemCause] {
  val riftDescriptor = RiftDescriptor(classOf[ProblemCause])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: ProblemCause, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case cip @ CauseIsProblem(_) => into.includeDirect(cip, CauseIsProblemDecomposer)
      case cit @ CauseIsThrowable(_) => into.includeDirect(cit, CauseIsThrowableDecomposer)
    }
  }
}

object HasAThrowableDescribedRecomposer extends Recomposer[HasAThrowableDescribed] {
  val riftDescriptor = RiftDescriptor(classOf[HasAThrowableDescribed])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[HasAThrowableDescribed] = {
    val classname = from.getString("classname").toAgg
    val message = from.getString("message").toAgg
    val stacktrace = from.getString("stacktrace").toAgg
    val cause = from.tryGetComplexType[HasAThrowableDescribed]("cause", this).toAgg
    (classname |@| message |@| stacktrace |@| cause)(HasAThrowableDescribed.apply)
  }
}

object CauseIsThrowableRecomposer extends Recomposer[CauseIsThrowable] {
  val riftDescriptor = RiftDescriptor(classOf[CauseIsThrowable])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[CauseIsThrowable] = {
    from.getComplexType("representation", HasAThrowableDescribedRecomposer).map(desc =>
      CauseIsThrowable(desc))
  }
}

object CauseIsProblemRecomposer extends Recomposer[CauseIsProblem] {
  val riftDescriptor = RiftDescriptor(classOf[CauseIsProblem])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[CauseIsProblem] = {
    from.getComplexType("problem").map(prob =>
      CauseIsProblem(prob))
  }
}

object ProblemCauseRecomposer extends Recomposer[ProblemCause] {
  val riftDescriptor = RiftDescriptor(classOf[ProblemCause])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[ProblemCause] = {
    from.getRiftDescriptor.flatMap(desc =>
      if (desc == RiftDescriptor(classOf[CauseIsProblem]))
        from.divertDirect(CauseIsProblemRecomposer)
      else if (desc == RiftDescriptor(classOf[CauseIsThrowable]))
        from.divertDirect(CauseIsThrowableRecomposer)
      else
        BadDataProblem(s"'$desc' is not a valid identifier for ProblemCause").withIdentifier("type").failure)
  }
}