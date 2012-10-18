package almhirt

trait OperationState
case class Ok(ticket: java.util.UUID) extends OperationState
case class InProcess(ticket: java.util.UUID) extends OperationState
case class Nok(ticket: java.util.UUID, problem: Problem) extends OperationState