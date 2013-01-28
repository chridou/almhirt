package almhirt.environment.configuration.impl

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.environment._
import almhirt.environment.configuration.AlmhirtBootstrapper
import almhirt.util._
import almhirt.messaging._
import almhirt.parts._
import almhirt.eventlog.DomainEventLog
import almhirt.commanding.CommandEnvelope
import almhirt.environment.configuration.SystemHelper
import com.typesafe.config.Config
import almhirt.environment.configuration.CleanUpAction
import almhirt.eventlog.impl.DomainEventLogActorHull
import almhirt.environment.configuration.ConfigHelper
import almhirt.environment.configuration.ConfigPaths
import almhirt.util.impl.OperationStateTrackerActorHull
import almhirt.core.ServiceRegistry
import almhirt.core.Almhirt
import almhirt.core.HasConfig

trait BootstrapperDefaultCoreComponents extends AlmhirtBootstrapper { self: HasConfig =>
  private var trackerRegistration: RegistrationHolder = null
  private var repos: HasRepositories = null
  private var cmdHandlerRegistry: HasCommandHandlers = null
  private var cmdExecutor: CommandExecutor = null
  private var cmdExecutorRegistration: RegistrationHolder = null

  override def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = {
    super.createCoreComponents(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { superCleanUp =>
        import akka.pattern._
        implicit val atMost = FiniteDuration(5, "s")
        implicit val anAlmhirt = theAlmhirt
        implicit val executionContext = theAlmhirt.executionContext

        inTryCatch {
          ConfigHelper.getSubConfig(config)(ConfigPaths.operationState).foreach { _ =>
            val tracker = SystemHelper.createOperationStateTrackerFromFactory.forceResult
            trackerRegistration =
              theAlmhirt.getService[OperationStateChannel].flatMap(channel =>
                (channel.actor ? SubscribeQry(MessagingSubscription.forActor[OperationState](tracker)))(atMost)
                  .mapTo[SubscriptionRsp]
                  .map(_.registration)
                  .toAlmFuture
                  .awaitResult)
                .forceResult
            theServiceRegistry.registerService[OperationStateTracker](almhirt.util.impl.OperationStateTrackerActorHull(tracker))
          }

          repos = HasRepositories()
          theServiceRegistry.registerService[HasRepositories](repos)

          cmdHandlerRegistry = HasCommandHandlers()
          theServiceRegistry.registerService[HasCommandHandlers](cmdHandlerRegistry)

          cmdExecutor = CommandExecutor(cmdHandlerRegistry, repos).forceResult
          cmdExecutorRegistration =
            theAlmhirt.getService[CommandChannel].flatMap(channel =>
              (channel.actor ? SubscribeQry(MessagingSubscription.forActor[CommandEnvelope](cmdExecutor.actor)))(atMost)
                .mapTo[SubscriptionRsp]
                .map(_.registration)
                .toAlmFuture
                .awaitResult)
              .forceResult
          theServiceRegistry.registerService[CommandExecutor](cmdExecutor)
          ConfigHelper.getSubConfig(config)(ConfigPaths.eventlog).foreach { _ =>
            val eventLogActor = SystemHelper.createEventLogFromFactory.forceResult
            theServiceRegistry.registerService[DomainEventLog](DomainEventLogActorHull(eventLogActor, config))
          }
          ConfigHelper.getSubConfig(config)(ConfigPaths.commandEndpoint).foreach { _ =>
            val endpoint = SystemHelper.createCommandEndpointFromFactory.forceResult
            theServiceRegistry.registerService[CommandEndpoint](endpoint)
          }

          (() => {
            cmdExecutorRegistration.dispose
            trackerRegistration.dispose
            superCleanUp();
          })
        }
    }
  }
}