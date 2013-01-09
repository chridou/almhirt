package almhirt.environment.configuration.impl

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
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

class AlmhirtDefaultBootStrapper(config: Config) extends AlmhirtBaseBootstrapper(config) {
  private var trackerRegistration: RegistrationHolder = null
  private var repos: HasRepositories = null
  private var cmdHandlerRegistry: HasCommandHandlers = null
  private var cmdExecutor: CommandExecutor = null
  private var cmdExecutorRegistration: RegistrationHolder = null

  override def createCoreComponents(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction] = {
    val config = theAlmhirt.system.config
    theAlmhirt.serviceRegistry match {
      case Some(sr) =>
        import akka.pattern._
        implicit val atMost = FiniteDuration(5, "s")
        implicit val executionContext = theAlmhirt.executionContext

        inTryCatch {
          ConfigHelper.ifSubConfigExists(config)(ConfigPaths.operationState) { _ =>
            val tracker = SystemHelper.createOperationStateTrackerFromFactory.forceResult
            trackerRegistration =
              theAlmhirt.getService[OperationStateChannel].flatMap(channel =>
                (channel.actor ? SubscribeQry(MessagingSubscription.forActor[OperationState](tracker)))(atMost)
                  .mapTo[SubscriptionRsp]
                  .map(_.registration)
                  .toAlmFuture
                  .awaitResult)
                .forceResult
            sr.registerService[OperationStateTracker](almhirt.util.impl.OperationStateTrackerActorHull(tracker))
          }

          repos = HasRepositories().forceResult
          sr.registerService[HasRepositories](repos)

          cmdHandlerRegistry = HasCommandHandlers()
          sr.registerService[HasCommandHandlers](cmdHandlerRegistry)

          cmdExecutor = CommandExecutor(cmdHandlerRegistry, repos).forceResult
          cmdExecutorRegistration =
            theAlmhirt.getService[CommandChannel].flatMap(channel =>
              (channel.actor ? SubscribeQry(MessagingSubscription.forActor[CommandEnvelope](cmdExecutor.actor)))(atMost)
                .mapTo[SubscriptionRsp]
                .map(_.registration)
                .toAlmFuture
                .awaitResult)
              .forceResult
          sr.registerService[CommandExecutor](cmdExecutor)
          ConfigHelper.ifSubConfigExists(config)(ConfigPaths.eventlog) { _ =>
            val eventLogActor = SystemHelper.createEventLogFromFactory.forceResult
            sr.registerService[DomainEventLog](DomainEventLogActorHull(eventLogActor))
          }
          ConfigHelper.ifSubConfigExists(config)(ConfigPaths.commandEndpoint) { _ =>
            val endpoint = SystemHelper.createCommandEndpointFromFactory.forceResult
            sr.registerService[CommandEndpoint](endpoint)
          }

          (() => {
            cmdExecutorRegistration.dispose
            trackerRegistration.dispose
          })
        }.flatMap(cleanUp => super.createCoreComponents(theAlmhirt).map(superCleanUp => () => { cleanUp(); superCleanUp() }))
      case None => scalaz.Failure(UnspecifiedProblem("Cannot register services without a service registry"))
    }
  }
}