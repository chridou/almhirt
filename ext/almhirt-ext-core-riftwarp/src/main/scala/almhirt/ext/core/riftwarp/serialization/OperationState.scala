package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._

object InProcessDecomposer extends Decomposer[InProcess] {
  val riftDescriptor = RiftDescriptor(classOf[InProcess])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: InProcess, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addWith("ticket", what.ticket, TrackingTicketDecomposer).flatMap(
        _.addWith("commandInfo", what.commandInfo, CommandInfoDecomposer).map(
          _.addDateTime("timestamp", what.timestamp)))
}

object ExecutedDecomposer extends Decomposer[Executed] {
  val riftDescriptor = RiftDescriptor(classOf[Executed])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: Executed, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addWith("ticket", what.ticket, TrackingTicketDecomposer).flatMap(
        _.addWith("action", what.action, PerformedActionDecomposer).map(
          _.addDateTime("timestamp", what.timestamp)))
}

object NotExecutedDecomposer extends Decomposer[NotExecuted] {
  val riftDescriptor = RiftDescriptor(classOf[NotExecuted])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: NotExecuted, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addWith("ticket", what.ticket, TrackingTicketDecomposer).flatMap(
        _.addComplex("problem", what.problem, None).map(
          _.addDateTime("timestamp", what.timestamp)))
}

object OperationStateDecomposer extends Decomposer[OperationState] {
  val riftDescriptor = RiftDescriptor(classOf[OperationState])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: OperationState, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case opstate: InProcess => into.includeDirect(opstate, InProcessDecomposer)
      case opstate: Executed => into.includeDirect(opstate, ExecutedDecomposer)
      case opstate: NotExecuted => into.includeDirect(opstate, NotExecutedDecomposer)
    }
  }
}

object InProcessRecomposer extends Recomposer[InProcess] {
  val riftDescriptor = RiftDescriptor(classOf[InProcess])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[InProcess] = {
    val ticket = from.getWith[TrackingTicket]("ticket", TrackingTicketRecomposer.recompose).toAgg
    val commandInfo = from.getWith[CommandInfo]("commandInfo", CommandInfoRecomposer.recompose).toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (ticket |@| commandInfo |@| timestamp)(InProcess.apply)
  }
}

object ExecutedRecomposer extends Recomposer[Executed] {
  val riftDescriptor = RiftDescriptor(classOf[Executed])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[Executed] = {
    val ticket = from.getWith[TrackingTicket]("ticket", TrackingTicketRecomposer.recompose).toAgg
    val action = from.getWith[PerformedAction]("action", PerformedActionRecomposer.recompose).toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (ticket |@| action |@| timestamp)(Executed.apply)
  }
}

object NotExecutedRecomposer extends Recomposer[NotExecuted] {
  val riftDescriptor = RiftDescriptor(classOf[NotExecuted])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[NotExecuted] = {
    val ticket = from.getWith[TrackingTicket]("ticket", TrackingTicketRecomposer.recompose).toAgg
    val problem = from.getComplexByTag[Problem]("problem", None).toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (ticket |@| problem |@| timestamp)(NotExecuted.apply)
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



