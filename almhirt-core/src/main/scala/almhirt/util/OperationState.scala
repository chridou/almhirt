package almhirt.util

import org.joda.time.DateTime
import almhirt.common._
import almhirt.domain.AggregateRootRef

sealed trait OperationState {
  def ticket: TrackingTicket
  def isFinished: Boolean
  def isFinishedSuccesfully: Boolean
  def isFinishedUnsuccesfully: Boolean = !isFinishedSuccesfully
  def tryGetAction: Option[PerformedAction]
}

sealed trait ResultOperationState extends OperationState

final case class InProcess(ticket: TrackingTicket, commandInfo: CommandInfo, timestamp: DateTime) extends OperationState {
  val isFinished = false
  val isFinishedSuccesfully = false
  val tryGetAction = None
  def withHeadCommandInfo: InProcess =
    this.copy(commandInfo = this.commandInfo.toHeadCommandInfo)
}

object InProcess {
  def apply(ticket: TrackingTicket, commandInfo: CommandInfo)(implicit createsDateTimes: CanCreateDateTime): InProcess =
    apply(ticket, commandInfo, createsDateTimes.getDateTime)
}

case class Executed(ticket: TrackingTicket, action: PerformedAction, timestamp: DateTime) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = true
  val tryGetAction = Some(action)
}

object Executed {
  def apply(ticket: TrackingTicket, action: PerformedAction)(implicit createsDateTimes: CanCreateDateTime): Executed = apply(ticket, action, createsDateTimes.getDateTime)
}

final case class NotExecuted(ticket: TrackingTicket, problem: Problem, timestamp: DateTime) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = false
  val tryGetAction = None
}

object NotExecuted {
  def apply(ticket: TrackingTicket, problem: Problem)(implicit createsDateTimes: CanCreateDateTime): NotExecuted = apply(ticket, problem, createsDateTimes.getDateTime)
}
