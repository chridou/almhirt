package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.util.OperationStateEvent
import riftwarp._

object  OperationStateEventDecomposer extends Decomposer[OperationStateEvent] {
  val riftDescriptor = RiftDescriptor(classOf[OperationStateEvent])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: OperationStateEvent, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addWith("header", what.header, EventHeaderDecomposer).flatMap(
        _.addWith("operationState", what.operationState, OperationStateDecomposer))
  }
}

object  OperationStateEventRecomposer extends Recomposer[OperationStateEvent] {
  val riftDescriptor = RiftDescriptor(classOf[OperationStateEvent])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[OperationStateEvent] = {
    for {
      header <- from.getWith("header", EventHeaderRecomposer.recompose)
      operationState <- from.getWith("operationState", OperationStateRecomposer.recompose)
    } yield OperationStateEvent(header, operationState)
  }
}