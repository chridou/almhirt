package almhirt.environment.configuration.impl

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
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
  override def createAlmhirt(actorSystem: ActorSystem, hasFuturesExecutionContext: HasExecutionContext, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)] = {
    super.createAlmhirt(actorSystem, hasFuturesExecutionContext, theServiceRegistry, startUpLogger).map {
      case (theAlmhirt, cleanUp) =>
        (new AlmhirtForTesting {
          override val config = theAlmhirt.config
          override val actorSystem = theAlmhirt.actorSystem
          override val executionContext = theAlmhirt.executionContext
          override def getServiceByType(clazz: Class[_ <: AnyRef]) = theAlmhirt.getServiceByType(clazz)

          override val serviceRegistry = theServiceRegistry
          override def executeCommand(cmdEnv: CommandEnvelope) { theAlmhirt.broadcast(cmdEnv) }

          override def reportProblem(prob: Problem) { theAlmhirt.reportProblem(prob) }
          override def reportOperationState(opState: OperationState) { theAlmhirt.reportOperationState(opState) }
          override def broadcastDomainEvent(event: DomainEvent) { theAlmhirt.broadcastDomainEvent(event) }
          override def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { theAlmhirt.broadcast(payload, metaData) }

          override def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: FiniteDuration, m: ClassTag[TPayload]) = theAlmhirt.createMessageChannel(name)
          override val durations = theAlmhirt.durations

          override def repositories = theAlmhirt.getService[HasRepositories].forceResult
          override def hasCommandHandlers = theAlmhirt.getService[HasCommandHandlers].forceResult
          override def eventLog = theAlmhirt.getService[DomainEventLog].forceResult
          override def operationStateTracker = theAlmhirt.getService[OperationStateTracker].forceResult

          override def log = theAlmhirt.log
        }, cleanUp)
    }
  }
}