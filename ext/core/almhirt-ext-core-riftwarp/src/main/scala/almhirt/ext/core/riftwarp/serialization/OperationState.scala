package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._

class PerformedActionDecomposer extends Decomposer[PerformedAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedAction], 1)
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedAction)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case PerformedCreateAction(id) =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "create"))
          .flatMap(_.addUuid("id", id))
      case PerformedUpdateAction(id) =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "update"))
          .flatMap(_.addUuid("id", id))
      case PerformedUnspecifiedAction =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "unspecified"))
    }
  }
}

class PerformedActionRecomposer extends Recomposer[PerformedAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedAction], 1)
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PerformedAction] = {
    from.getString("type").flatMap {
      case "create" =>
        from.getUuid("id").map(PerformedCreateAction.apply)
      case "update" =>
        from.getUuid("id").map(PerformedUpdateAction.apply)
      case "unspecified" =>
        PerformedUnspecifiedAction.success
      case x =>
        UnspecifiedProblem("'%s' is not a valid CommandAction type").failure
    }
  }
}

class OperationStateDecomposer extends Decomposer[OperationState] {
  val riftDescriptor = RiftDescriptor(classOf[OperationState], 1)
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: OperationState)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case InProcess(ticket) =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "inProcess"))
          .flatMap(_.addComplexTyped[TrackingTicket]("ticket", ticket))
      case Executed(ticket, action) =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "executed"))
          .flatMap(_.addComplexTyped[TrackingTicket]("ticket", ticket))
          .flatMap(_.addComplexTyped[PerformedAction]("action", action))
      case NotExecuted(ticket, problem) =>
        into
          .addRiftDescriptor(this.riftDescriptor)
          .flatMap(_.addString("type", "notExecuted"))
          .flatMap(_.addComplexTyped[TrackingTicket]("ticket", ticket))
          .flatMap(_.addComplexTyped[Problem]("action", problem))
      case x =>
        UnspecifiedProblem("'%s' is not a valid OperationState type").failure
    }
  }
}

class OperationStateRecomposer extends Recomposer[OperationState] {
  val riftDescriptor = RiftDescriptor(classOf[OperationState], 1)
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[OperationState] = {
    from.getString("type").flatMap {
      case "inProcess" =>
        from.getComplexTypeFixed[TrackingTicket]("ticket").map(InProcess.apply)
      case "executed" =>
        val ticket = from.getComplexTypeFixed[TrackingTicket]("ticket").toAgg
        val action = from.getComplexTypeFixed[PerformedAction]("action").toAgg
        (ticket |@| action)(Executed.apply)
      case "notExecuted" =>
        val ticket = from.getComplexTypeFixed[TrackingTicket]("ticket").toAgg
        val problem = from.getComplexType[Problem]("problem").toAgg
        (ticket |@| problem)(NotExecuted.apply)
      case x =>
        UnspecifiedProblem("'%s' is not a valid OperationState type").failure
    }
  }
}



