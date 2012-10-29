package almhirt

sealed trait OperationState{ def ticket: String }
sealed trait ResultOperationState extends OperationState
case class InProcess(ticket: String) extends OperationState
case class Executed(ticket: String) extends ResultOperationState
case class NotExecuted(ticket: String, problem: Problem) extends ResultOperationState
