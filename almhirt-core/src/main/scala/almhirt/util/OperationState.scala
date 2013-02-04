package almhirt.util

import almhirt.common._


sealed trait PerformedAction
case class CreateAction(id: java.util.UUID) extends PerformedAction
case class UpdateAction(id: java.util.UUID) extends PerformedAction
case object UnspecifiedAction extends PerformedAction


sealed trait OperationState{ 
  def ticket: TrackingTicket
  def isFinished: Boolean
  def isFinishedSuccesfully: Boolean
  def isFinishedUnsuccesfully: Boolean = !isFinishedSuccesfully
  def tryGetAction: Option[PerformedAction]
}

sealed trait ResultOperationState extends OperationState
case class InProcess(ticket: TrackingTicket) extends OperationState {
  val isFinished = false
  val isFinishedSuccesfully = false
  val tryGetAction = None
}
case class Executed(ticket: TrackingTicket, action: PerformedAction = UnspecifiedAction) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = true
  val tryGetAction = Some(action)
}
case class NotExecuted(ticket: TrackingTicket, problem: Problem) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = false
  val tryGetAction = None
}
