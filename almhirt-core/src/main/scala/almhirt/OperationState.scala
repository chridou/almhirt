package almhirt

trait OperationState
case class Executed(ticket: String) extends OperationState
case class InProcess(ticket: String) extends OperationState
case class NotExecuted(ticket: String, problem: Problem) extends OperationState