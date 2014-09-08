package almhirt.tracking

import almhirt.common.CommandHeader
import almhirt.problem.ProblemCause

sealed trait CommandStatus 

object CommandStatus {
  case object Initiated extends CommandStatus
  sealed trait CommandResult extends CommandStatus
  case object Executed extends CommandResult
  final case class NotExecuted(cause: ProblemCause) extends CommandResult
  
  implicit class CommandStatusOps(self: CommandStatus) {
    def notExecuted: Boolean =
      self match {
      case NotExecuted(_) => true
      case _ => false
    }
  }
}

