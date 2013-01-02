package almhirt.environment

import scala.concurrent.duration.FiniteDuration
import almhirt.common.Problem
import almhirt.util.OperationState
import almhirt.domain.DomainEvent
import almhirt.messaging.Message
import almhirt.commanding.CommandEnvelope
import akka.dispatch.MessageDispatcher
import almhirt.core.CanCreateUuidsAndDateTimes
import almhirt.common.HasExecutionContext

trait AlmhirtBaseOps extends CanCreateUuidsAndDateTimes with HasExecutionContext {
  def reportProblem(prob: Problem): Unit
  def reportOperationState(opState: OperationState): Unit
  def broadcastDomainEvent(event: DomainEvent): Unit
  def broadcastCommand(comEnvelope: CommandEnvelope): Unit
  def postCommand(comEnvelope: CommandEnvelope): Unit
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty): Unit
  def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty): Message[T]
  
  def shortDuration: FiniteDuration
  def mediumDuration: FiniteDuration
  def longDuration: FiniteDuration
}