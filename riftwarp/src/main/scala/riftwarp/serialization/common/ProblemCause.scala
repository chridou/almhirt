package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait HasAThrowableDescribedDecomposer extends Decomposer[HasAThrowableDescribed] {
  val riftDescriptor = RiftDescriptor(classOf[HasAThrowableDescribed], 1)
  def decompose[TDimension <: RiftDimension](what: HasAThrowableDescribed)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor).flatMap(
      _.addString("classname", what.classname).flatMap(
        _.addString("message", what.message).flatMap(
          _.addString("stacktrace", what.stacktrace).flatMap(
            _.addOptionalComplexSelective("cause", this, what.cause)))))
  }
}

trait ProblemCauseDecomposer extends Decomposer[ProblemCause] {
  val riftDescriptor = RiftDescriptor(classOf[ProblemCause], 1)
  def decompose[TDimension <: RiftDimension](what: ProblemCause)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    val first = into.addRiftDescriptor(this.riftDescriptor)
    what match {
      case CauseIsProblem(prob) =>
        first.flatMap(
          _.addString("type", "CauseIsProblem").flatMap(
            _.addComplex("cause", prob, None)))
      case CauseIsThrowable(hat @ HasAThrowable(_)) =>
        first.flatMap(
          _.addString("type", "HasAThrowableDescribed").flatMap(
            _.addComplex("cause", hat.toDescription, Some(RiftDescriptor(classOf[HasAThrowableDescribed], 1)))))
      case CauseIsThrowable(hatd @ HasAThrowableDescribed(_, _, _, _)) =>
        first.flatMap(
          _.addString("type", "HasAThrowableDescribed").flatMap(
            _.addComplex("cause", hatd, Some(RiftDescriptor(classOf[HasAThrowableDescribed], 1)))))
    }
  }
}

class HasAThrowableDescribedRecomposer extends Recomposer[HasAThrowableDescribed] {
  val riftDescriptor = RiftDescriptor(classOf[HasAThrowableDescribed], 1)
  def recompose(from: Rematerializer): AlmValidation[HasAThrowableDescribed] = {
    val classname = from.getString("classname").toAgg
    val message = from.getString("message").toAgg
    val stacktrace = from.getString("stacktrace").toAgg
    val cause = from.tryGetComplexType[HasAThrowableDescribed]("cause", this).toAgg
    (classname |@| message |@| stacktrace |@| cause)(HasAThrowableDescribed.apply)
  }
}

class ProblemCauseRecomposer extends Recomposer[ProblemCause] {
  val riftDescriptor = RiftDescriptor(classOf[ProblemCause], 1)
  def recompose(from: Rematerializer): AlmValidation[ProblemCause] = {
    from.getString("type").flatMap {
      case "CauseIsProblem" => from.getComplexType[Problem]("cause").map(CauseIsProblem(_))
      case "HasAThrowableDescribed" => from.getComplexType[HasAThrowableDescribed]("cause").map(CauseIsThrowable(_))
      case x => BadDataProblem(s"'$x' is not a valid identifier for ProblemCause").withIdentifier("type").failure
    }
  }
}