package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

object ProblemEventDecomposer extends Decomposer[ProblemEvent] {
  val riftDescriptor = RiftDescriptor(classOf[ProblemEvent])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: ProblemEvent, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addWith("header", what.header, EventHeaderDecomposer).flatMap(
        _.addComplex("problem", what.problem, None))
  }
}

object ProblemEventRecomposer extends Recomposer[ProblemEvent] {
  val riftDescriptor = RiftDescriptor(classOf[ProblemEvent])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[ProblemEvent] = {
    for {
      header <- from.getWith("header", EventHeaderRecomposer.recompose)
      problem <- from.getComplexByTag[Problem]("problem", None)
    } yield ProblemEvent(header, problem)
  }
}