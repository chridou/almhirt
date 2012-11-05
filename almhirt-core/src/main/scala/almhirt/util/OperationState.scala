package almhirt.util

import almhirt.Problem

sealed trait OperationState{ 
  def ticket: String
  def isFinished: Boolean
  def isFinishedSuccesfully: Boolean
  def isFinishedUnsuccesfully: Boolean = !isFinishedSuccesfully
}
sealed trait ResultOperationState extends OperationState
case class InProcess(ticket: String) extends OperationState {
  val isFinished = false
  val isFinishedSuccesfully = false
}
case class Executed(ticket: String) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = true
}
case class NotExecuted(ticket: String, problem: Problem) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = false
}
