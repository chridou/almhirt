package almhirt.environment.configuration.impl

import scala.reflect.ClassTag
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.event._
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

class AlmhirtBaseBootstrapper(override val config: Config) extends AlmhirtBootstrapper with HasConfig {
  override def createActorSystem(startUpLogger: LoggingAdapter): AlmValidation[ActorSystem] = {
    val sysName = ConfigHelper.getString(config)("almhirt.systemname").getOrElse("almhirt")
    ActorSystem(sysName, config).success
  }

  override def createServiceRegistry(system: ActorSystem, startUpLogger: LoggingAdapter): (ServiceRegistry, CleanUpAction) = {
    (new SimpleConcurrentServiceRegistry, () => ())
  }

  override def createFuturesExecutionContext(actorSystem: ActorSystem, startUpLogger: LoggingAdapter): AlmValidation[(HasExecutionContext, CleanUpAction)] = {
    val dispatcherPath = ConfigHelper.lookupDispatcherConfigPath(config)(ConfigPaths.futures).toOption
    val dispatcher = ConfigHelper.lookUpDispatcher(actorSystem)(dispatcherPath)
    (HasExecutionContext(dispatcher), () => ()).success
  }

  override def initializeMessaging(foundations: MessagingFoundations, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = {
    implicit val dur = Duration(1, "s")
    implicit val hec = foundations
    val messageHubDispatcherName = ConfigHelper.lookupDispatcherConfigPath(config)(ConfigPaths.messagehub).toOption
    val messageChannelsDispatcherName = ConfigHelper.lookupDispatcherConfigPath(config)(ConfigPaths.messagechannels).toOption
    val messageHub = MessageHub("MessageHub", messageHubDispatcherName, messageChannelsDispatcherName)(foundations)
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
      theServiceRegistry.registerService[MessageHub](messageHub)
      theServiceRegistry.registerService[CommandChannel](x._1)
      theServiceRegistry.registerService[OperationStateChannel](x._2)
      theServiceRegistry.registerService[DomainEventsChannel](x._3)
      theServiceRegistry.registerService[ProblemChannel](x._4)
      (() => { x._1.close(); x._1.close(); x._2.close(); x._3.close(); messageHub.close(); })
    }
  }

  override def createAlmhirt(theActorSystem: ActorSystem, hasFuturesExecutionContext: HasExecutionContext, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)] = {
    for {
      messageHub <- theServiceRegistry.getService[MessageHub]
    } yield (new Almhirt {
      override val config = AlmhirtBaseBootstrapper.this.config
      override val actorSystem = theActorSystem
      override val executionContext = hasFuturesExecutionContext.executionContext
      override def getServiceByType(clazz: Class[_ <: AnyRef]) = theServiceRegistry.getServiceByType(clazz)
      
      override def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: FiniteDuration, m: ClassTag[TPayload]) = messageHub.createMessageChannel(name)

      override def reportProblem(prob: Problem) { broadcast(prob) }
      override def reportOperationState(opState: OperationState) { broadcast(opState) }
      override def broadcastDomainEvent(event: DomainEvent) { broadcast(event) }
      override def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { messageHub.actor ! BroadcastMessageCmd(createMessage(payload, metaData)) }

      override val durations = Durations(config)

      override val log = Logging(actorSystem, classOf[Almhirt])

    }, () => { theActorSystem.shutdown(); theActorSystem.awaitTermination() })
  }

  override def createCoreComponents(almhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def initializeCoreComponents(almhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerRepositories(almhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerCommandHandlers(almhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerAndInitializeMoreComponents(almhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def prepareGateways(almhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerAndInitializeAuxServices(almhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def cleanUpTemps(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[Unit] =
    ().success

}