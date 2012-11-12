package almhirt.environment

import akka.util.Duration
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._

trait Almhirt extends AlmhirtEnvironmentOps with HasServices with Disposable {
  def environment: AlmhirtEnvironment

  def reportProblem(prob: Problem) { environment.reportProblem(prob) }
  def reportOperationState(opState: OperationState) { environment.reportOperationState(opState) }
  def executeCommand(cmdEnv: CommandEnvelope) { environment.executeCommand(cmdEnv) }
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String]) { environment.broadcast(payload, metaData) }
  def getDateTime = environment.getDateTime
  def getUuid = environment.getUuid
  def getReadOnlyRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmFuture[HasAggregateRoots[AR, TEvent]] =
    environment.getReadOnlyRepository

  def queryOperationStateFor(ticket: TrackingTicket)(implicit atMost: Duration) = environment.operationStateTracker.queryStateFor(ticket)
  def onResultOperationState(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: Duration){environment.operationStateTracker.onResult(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit)}
  def getResultOperationStateFor(ticket: TrackingTicket)(implicit atMost: Duration) = environment.operationStateTracker.getResultFor(ticket)

  def messageWithPayload[T <: AnyRef](payload: T, metaData: Map[String,String] = Map.empty) = environment.messageWithPayload(payload, metaData)
  
}