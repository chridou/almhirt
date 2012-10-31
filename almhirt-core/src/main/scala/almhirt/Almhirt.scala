package almhirt

import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._

trait Almhirt extends AlmhirtEnvironmentOps with HasServices{
  def environment: AlmhirtEnvironment
  
  def reportProblem(prob: Problem) { environment.reportProblem(prob) }
  def reportOperationState(opState: OperationState) { environment.reportOperationState(opState) }
  def executeCommand(cmdEnv: CommandEnvelope) { environment.executeCommand(cmdEnv) }
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String]) { environment.broadcast(payload, metaData) }
  def getDateTime = environment.getDateTime
  def getUuid = environment.getUuid
  def getRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmValidation[AggregateRootRepository[AR, TEvent]] =
    environment.getRepository
}