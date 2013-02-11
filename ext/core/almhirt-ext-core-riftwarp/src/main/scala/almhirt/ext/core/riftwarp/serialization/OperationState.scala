package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._

object InProcessDecomposer extends Decomposer[InProcess] {
  val riftDescriptor = RiftDescriptor(classOf[InProcess])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: InProcess)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor).flatMap(
        _.addComplexSelective("ticket", TrackingTicketDecomposer, what.ticket))
}

object ExecutedDecomposer extends Decomposer[Executed] {
  val riftDescriptor = RiftDescriptor(classOf[Executed])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: Executed)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor).flatMap(
        _.addComplexSelective("ticket", TrackingTicketDecomposer, what.ticket).flatMap(
          _.addComplexSelective("action", PerformedActionDecomposer, what.action)))
}

object NotExecutedDecomposer extends Decomposer[NotExecuted] {
  val riftDescriptor = RiftDescriptor(classOf[NotExecuted])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: NotExecuted)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor).flatMap(
        _.addComplexSelective("ticket", TrackingTicketDecomposer, what.ticket).flatMap(
          _.addComplexTyped[Problem]("problem", what.problem)))
}

object OperationStateDecomposer extends Decomposer[OperationState] {
  val riftDescriptor = RiftDescriptor(classOf[OperationState])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: OperationState)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case opstate @ InProcess(_) => into.includeDirect(opstate, InProcessDecomposer)
      case opstate @ Executed(_, _) => into.includeDirect(opstate, ExecutedDecomposer)
      case opstate @ NotExecuted(_, _) => into.includeDirect(opstate, NotExecutedDecomposer)
    }
  }
}

object InProcessRecomposer extends Recomposer[InProcess] {
  val riftDescriptor = RiftDescriptor(classOf[InProcess])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[InProcess] = {
    from.getComplexType("ticket", TrackingTicketRecomposer).map(InProcess.apply)
  }
}

object ExecutedRecomposer extends Recomposer[Executed] {
  val riftDescriptor = RiftDescriptor(classOf[Executed])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[Executed] = {
    val ticket = from.getComplexType[TrackingTicket]("ticket", TrackingTicketRecomposer).toAgg
    val action = from.getComplexType[PerformedAction]("action", PerformedActionRecomposer).toAgg
    (ticket |@| action)(Executed.apply)
  }
}

object NotExecutedRecomposer extends Recomposer[NotExecuted] {
  val riftDescriptor = RiftDescriptor(classOf[NotExecuted])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[NotExecuted] = {
    val ticket = from.getComplexType[TrackingTicket]("ticket", TrackingTicketRecomposer).toAgg
    val problem = from.getComplexType[Problem]("problem").toAgg
    (ticket |@| problem)(NotExecuted.apply)
  }
}

object OperationStateRecomposer extends DivertingRecomposer[OperationState] {
  val riftDescriptor = RiftDescriptor(classOf[OperationState])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      InProcessRecomposer.riftDescriptor -> InProcessRecomposer,
      ExecutedRecomposer.riftDescriptor -> ExecutedRecomposer,
      NotExecutedRecomposer.riftDescriptor -> NotExecutedRecomposer).lift
}



