package almhirt

import akka.util.Duration
import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._

trait Almhirt extends AlmhirtEnvironmentOps with HasServices with Disposable {
  def environment: AlmhirtEnvironment

  def reportProblem(prob: Problem) { environment.reportProblem(prob) }
  def reportOperationState(opState: OperationState) { environment.reportOperationState(opState) }
  def executeCommand(cmdEnv: CommandEnvelope) { environment.executeCommand(cmdEnv) }
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String]) { environment.broadcast(payload, metaData) }
  def getDateTime = environment.getDateTime
  def getUuid = environment.getUuid
  def getRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmValidation[AggregateRootRepository[AR, TEvent]] =
    environment.getRepository

  def queryOperationStateFor(ticket: String)(implicit atMost: Duration) = environment.operationStateTracker.queryStateFor(ticket)
  def onResultOperationState(ticket: String, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: Duration){environment.operationStateTracker.onResult(ticket: String, callback: AlmValidation[ResultOperationState] => Unit)}
  def getResultOperationStateFor(ticket: String)(implicit atMost: Duration) = environment.operationStateTracker.getResultFor(ticket)

}