package almhirt.environment

import akka.util.Duration
import almhirt.common.Problem
import almhirt.util.OperationState
import almhirt.domain.DomainEvent
import almhirt.messaging.Message
import almhirt.commanding.CommandEnvelope
import akka.dispatch.MessageDispatcher
import almhirt.core.CanCreateUuidsAndDateTimes

trait AlmhirtBaseOps extends CanCreateUuidsAndDateTimes {
  def reportProblem(prob: Problem): Unit
  def reportOperationState(opState: OperationState): Unit
  def broadcastDomainEvent(event: DomainEvent): Unit
  def postCommand(comEnvelope: CommandEnvelope): Unit
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty): Unit
  def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty): Message[T]
  def futureDispatcher: MessageDispatcher
  
  def shortDuration: Duration
  def mediumDuration: Duration
  def longDuration: Duration
}