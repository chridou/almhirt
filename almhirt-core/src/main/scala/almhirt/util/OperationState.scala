package almhirt.util

import almhirt.Problem

sealed trait TrackingTicket
case class StringTrackingTicket(ident: String) extends TrackingTicket
case class UuidTrackingTicket(ident: java.util.UUID) extends TrackingTicket

object TrackingTicket {
  def apply(): TrackingTicket = apply(java.util.UUID.randomUUID())
  def apply(ident: String): TrackingTicket = StringTrackingTicket(ident)
  def apply(ident: java.util.UUID): TrackingTicket = UuidTrackingTicket(ident)
}


sealed trait OperationState{ 
  def ticket: TrackingTicket
  def isFinished: Boolean
  def isFinishedSuccesfully: Boolean
  def isFinishedUnsuccesfully: Boolean = !isFinishedSuccesfully
}

sealed trait ResultOperationState extends OperationState
case class InProcess(ticket: TrackingTicket) extends OperationState {
  val isFinished = false
  val isFinishedSuccesfully = false
}
case class Executed(ticket: TrackingTicket) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = true
}
case class NotExecuted(ticket: TrackingTicket, problem: Problem) extends ResultOperationState {
  val isFinished = true
  val isFinishedSuccesfully = false
}
