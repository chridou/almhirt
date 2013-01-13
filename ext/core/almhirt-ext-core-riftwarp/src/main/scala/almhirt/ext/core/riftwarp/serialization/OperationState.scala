package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._

class CommandActionDecomposer extends Decomposer[CommandAction] {
  val typeDescriptor = TypeDescriptor(classOf[CommandAction], 1)
  def decompose[TDimension <: RiftDimension](what: CommandAction)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case CreateAction(id) =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .flatMap(_.addString("type", "create"))
          .flatMap(_.addUuid("id", id))
      case UpdateAction(id) =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .flatMap(_.addString("type", "update"))
          .flatMap(_.addUuid("id", id))
      case UnspecifiedAction =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .flatMap(_.addString("type", "unspecified"))
    }
  }
}

class CommandActionRecomposer extends Recomposer[CommandAction] {
  val typeDescriptor = TypeDescriptor(classOf[CommandAction], 1)
  def recompose(from: Rematerializer): AlmValidation[CommandAction] = {
    from.getString("type").flatMap {
      case "create" => 
        from.getUuid("id").map(CreateAction.apply)
      case "update" => 
        from.getUuid("id").map(UpdateAction.apply)
      case "unspecified" => 
        UnspecifiedAction.success
      case x => 
        UnspecifiedProblem("'%s' is not a valid CommandAction type").failure
    }
  }
}

class OperationStateDecomposer extends Decomposer[OperationState] {
  val typeDescriptor = TypeDescriptor(classOf[OperationState], 1)
  def decompose[TDimension <: RiftDimension](what: OperationState)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case InProcess(ticket) =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .flatMap(_.addString("type", "inProcess"))
          .flatMap(_.addComplexTypeFixed[TrackingTicket]("ticket", ticket))
      case Executed(ticket, action) =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .flatMap(_.addString("type", "executed"))
          .flatMap(_.addComplexTypeFixed[TrackingTicket]("ticket", ticket))
          .flatMap(_.addComplexTypeFixed[CommandAction]("action", action))
      case NotExecuted(ticket, problem) =>
        into
          .addTypeDescriptor(this.typeDescriptor)
          .flatMap(_.addString("type", "notExecuted"))
          .flatMap(_.addComplexTypeFixed[TrackingTicket]("ticket", ticket))
          .flatMap(_.addComplexType[Problem]("action", problem))
      case x => 
        UnspecifiedProblem("'%s' is not a valid OperationState type").failure
    }
  }
}

class OperationStateRecomposer extends Recomposer[OperationState] {
  val typeDescriptor = TypeDescriptor(classOf[OperationState], 1)
  def recompose(from: Rematerializer): AlmValidation[OperationState] = {
    from.getString("type").flatMap {
      case "inProcess" => 
        from.getComplexTypeFixed[TrackingTicket]("ticket").map(InProcess.apply)
       case "executed" => 
        val ticket = from.getComplexTypeFixed[TrackingTicket]("ticket").toAgg
        val action = from.getComplexTypeFixed[CommandAction]("action").toAgg
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



