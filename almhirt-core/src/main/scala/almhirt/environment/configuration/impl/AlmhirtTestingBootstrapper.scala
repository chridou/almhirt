package almhirt.environment.configuration.impl

import com.typesafe.config.Config
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

class AlmhirtTestingBootstrapper(config: Config) extends AlmhirtDefaultBootStrapper(config) {

  override def createAlmhirt(aContext: AlmhirtContext, aSystem: AlmhirtSystem): AlmValidation[Almhirt] = {
    super.createAlmhirt(aContext, aSystem).map(almhirt =>
      new AlmhirtForTesting {
        def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: akka.util.Duration, m: Manifest[TPayload]) = almhirt.createMessageChannel(name)

        def executeCommand(cmdEnv: CommandEnvelope) { almhirt.postCommand(cmdEnv) }

        def reportProblem(prob: Problem) { almhirt.reportProblem(prob) }
        def reportOperationState(opState: OperationState) { almhirt.reportOperationState(opState) }
        def broadcastDomainEvent(event: DomainEvent) { almhirt.broadcastDomainEvent(event) }
        def postCommand(comEnvelope: CommandEnvelope) { almhirt.postCommand(comEnvelope) }
        def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { almhirt.broadcast(payload, metaData) }
        def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = almhirt.createMessage(payload, metaData)

        def registerServiceByType(clazz: Class[_ <: AnyRef], service: AnyRef) { almhirt.registerServiceByType(clazz, service) }
        def getServiceByType(clazz: Class[_ <: AnyRef]) = almhirt.getServiceByType(clazz)

        def futureDispatcher = aSystem.futureDispatcher
        def shortDuration = aSystem.shortDuration
        def mediumDuration = aSystem.mediumDuration
        def longDuration = aSystem.longDuration

        def getDateTime = aSystem.getDateTime
        def getUuid = aSystem.getUuid

        def close() { AlmhirtBootstrapper.runShutDownSequence(AlmhirtTestingBootstrapper.this) }

        def system = aSystem
        def context = aContext
        def repositories = almhirt.getService[HasRepositories].forceResult
        def hasCommandHandlers = almhirt.getService[HasCommandHandlers].forceResult
        def eventLog = almhirt.getService[DomainEventLog].forceResult
        def operationStateTracker = almhirt.getService[OperationStateTracker].forceResult
      })
  }

}