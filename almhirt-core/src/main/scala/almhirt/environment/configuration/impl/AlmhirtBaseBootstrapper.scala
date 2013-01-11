package almhirt.environment.configuration.impl

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.environment._
import almhirt.core.impl.SimpleConcurrentServiceRegistry
import almhirt.domain._
import almhirt.commanding._
import almhirt.messaging._
import almhirt.util._
import almhirt.core.ServiceRegistry
import almhirt.environment.configuration._
import com.typesafe.config.Config
import ch.qos.logback.classic.Logger

class AlmhirtBaseBootstrapper(val config: Config) extends AlmhirtBootstrapper {
  def createAlmhirtSystem(): AlmValidation[(AlmhirtSystem, CleanUpAction)] =
    AlmhirtSystem(config).map(sys => (sys, sys.dispose))

  def createServiceRegistry(system: AlmhirtSystem, startUpLogger: Logger): (Option[ServiceRegistry], CleanUpAction) = {
    (Some(new SimpleConcurrentServiceRegistry), () => ())
  }

  def createChannels(theServiceRegistry: Option[ServiceRegistry], startUpLogger: Logger)(implicit system: AlmhirtSystem): AlmValidation[CleanUpAction] = {
    theServiceRegistry match {
      case Some(sr) =>
        implicit val atMost = system.shortDuration
        implicit val executionContext = system.executionContext
        val messageHub = MessageHub("MessageHub")
        val channels =
          (for {
            commandChannel <- messageHub.createMessageChannel[CommandEnvelope]("CommandChannel")
            operationStateChannel <- messageHub.createMessageChannel[OperationState]("OperationStateChannel")
            domainEventsChannel <- messageHub.createMessageChannel[DomainEvent]("DomainEventsChannel")
            problemsChannel <- messageHub.createMessageChannel[Problem]("ProblemsChannel")
          } yield (
            new CommandChannelWrapper(commandChannel),
            new OperationStateChannelWrapper(operationStateChannel),
            new DomainEventsChannelWrapper(domainEventsChannel),
            new ProblemChannelWrapper(problemsChannel))).awaitResult

        channels.map { x =>
          sr.registerService[MessageHub](messageHub)
          sr.registerService[CommandChannel](x._1)
          sr.registerService[OperationStateChannel](x._2)
          sr.registerService[DomainEventsChannel](x._3)
          sr.registerService[ProblemChannel](x._4)
          (() => { x._1.close(); x._1.close(); x._2.close(); x._3.close(); messageHub.close(); })
        }
      case _ => (() => ()).success
    }
  }

  def createAlmhirt(theServiceRegistry: Option[ServiceRegistry], startUpLogger: Logger)(implicit theSystem: AlmhirtSystem): AlmValidation[(Almhirt, CleanUpAction)] = {
    theServiceRegistry match {
      case Some(sr) =>
        for {
          messageHub <- sr.getService[MessageHub]
        } yield (new Almhirt {
          val system = theSystem
          
          def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: FiniteDuration, m: Manifest[TPayload]) = messageHub.createMessageChannel(name)

          def reportProblem(prob: Problem) { broadcast(prob) }
          def reportOperationState(opState: OperationState) { broadcast(opState) }
          def broadcastDomainEvent(event: DomainEvent) { broadcast(event) }
          def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { messageHub.actor ! BroadcastMessageCmd(createMessage(payload, metaData)) }

          val serviceRegistry = theServiceRegistry

          def executionContext = system.executionContext
          def shortDuration = system.shortDuration
          def mediumDuration = system.mediumDuration
          def longDuration = system.longDuration

          def getDateTime = system.getDateTime
          def getUuid = system.getUuid
          
        }, () => ())
      case None => scalaz.Failure(UnspecifiedProblem("Cannot create almhirt without a service registry"))
    }
  }

  def createCoreComponents(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] =
    (() => ()).success
    
  def initializeCoreComponents(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] =
    (() => ()).success

  def registerRepositories(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] =
    (() => ()).success
    
  def registerCommandHandlers(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] =
    (() => ()).success

  def registerAndInitializeMoreComponents(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] =
    (() => ()).success
    
  def prepareGateways(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] =
    (() => ()).success

  def registerAndInitializeAuxServices(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] =
    (() => ()).success

  def cleanUpTemps(implicit almhirt: Almhirt, startUpLogger: Logger): AlmValidation[Unit] =
    ().success
    
}