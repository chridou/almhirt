package almhirt.util

import almhirt.common._


sealed trait CommandAction
case class CreateAction(id: java.util.UUID) extends CommandAction
case class UpdateAction(id: java.util.UUID) extends CommandAction
case object UnspecifiedAction extends CommandAction


sealed trait OperationState{ 
  def ticket: TrackingTicket
  def isFinished: Boolean
  def isFinishedSuccesfully: Boolean
  def isFinishedUnsuccesfully: Boolean = !isFinishedSuccesfully
  def tryGetAction: Option[CommandAction]
}

sealed trait ResultOperationState extends OperationState
case class InProcess(ticket: TrackingTicket) extends OperationState {
  val isFinished = false
  val isFinishedSuccesfully = false
  val tryGetAction = None
}
case class Executed(ticket: TrackingTicket, action: CommandAction = UnspecifiedAction) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = true
  val tryGetAction = Some(action)
}
case class NotExecuted(ticket: TrackingTicket, problem: Problem) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = false
  val tryGetAction = None
}
