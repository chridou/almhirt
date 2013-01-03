package almhirt.environment.configuration.impl

import scala.concurrent.duration.FiniteDuration
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.common._
import almhirt.commanding.CommandEnvelope
import almhirt.util.OperationState
import almhirt.domain.DomainEvent
import almhirt.environment.configuration.AlmhirtBootstrapper
import almhirt.parts.HasRepositories
import almhirt.parts.HasCommandHandlers
import almhirt.eventlog.DomainEventLog
import almhirt.util.OperationStateTracker
import almhirt.core.ServiceRegistry
import almhirt.environment.configuration.CleanUpAction
import com.typesafe.config.Config

class AlmhirtTestingBootstrapper(config: Config) extends AlmhirtDefaultBootStrapper(config) {
  override def createAlmhirt(theServiceRegistry: Option[ServiceRegistry])(implicit theSystem: AlmhirtSystem): AlmValidation[(Almhirt, CleanUpAction)] = {
    super.createAlmhirt(theServiceRegistry).map {
      case (almhirt, cleanUp) =>
        (new AlmhirtForTesting {
          def system = theSystem

          def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: FiniteDuration, m: Manifest[TPayload]) = almhirt.createMessageChannel(name)

          def executeCommand(cmdEnv: CommandEnvelope) { almhirt.broadcast(cmdEnv) }

          def reportProblem(prob: Problem) { almhirt.reportProblem(prob) }
          def reportOperationState(opState: OperationState) { almhirt.reportOperationState(opState) }
          def broadcastDomainEvent(event: DomainEvent) { almhirt.broadcastDomainEvent(event) }
          def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { almhirt.broadcast(payload, metaData) }

          def executionContext = almhirt.executionContext
          def shortDuration = almhirt.shortDuration
          def mediumDuration = almhirt.mediumDuration
          def longDuration = almhirt.longDuration

          def serviceRegistry = almhirt.serviceRegistry

          def getDateTime = almhirt.getDateTime
          def getUuid = almhirt.getUuid

          def repositories = almhirt.getService[HasRepositories].forceResult
          def hasCommandHandlers = almhirt.getService[HasCommandHandlers].forceResult
          def eventLog = almhirt.getService[DomainEventLog].forceResult
          def operationStateTracker = almhirt.getService[OperationStateTracker].forceResult
        }, cleanUp)
    }
  }
}