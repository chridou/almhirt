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

class AlmhirtDefaultBootStrapper(config: Config) extends AlmhirtBaseBootstrapper(config) {
  private var tracker: OperationStateTracker = null
  private var trackerRegistration: RegistrationHolder = null
  private var repos: HasRepositories = null
  private var cmdHandlerRegistry: HasCommandHandlers = null
  private var cmdExecutor: CommandExecutor = null
  private var cmdExecutorRegistration: RegistrationHolder = null
  private var eventLog: DomainEventLog = null

  override def registerComponents(almhirt: Almhirt): AlmValidation[CleanUpAction] = {
    import akka.pattern._
    implicit val atMost = FiniteDuration(5, "s")
    implicit val executionContext = almhirt.executionContext

    inTryCatch {
      tracker = OperationStateTracker()(almhirt, system).forceResult
      trackerRegistration =
        (context.operationStateChannel.actor ? SubscribeQry(MessagingSubscription.forActor[OperationState](tracker.actor)))(atMost)
          .mapTo[SubscriptionRsp]
          .map(_.registration)
          .toAlmFuture
          .awaitResult
          .forceResult
      almhirt.registerService[OperationStateTracker](tracker)

      repos = HasRepositories().forceResult
      almhirt.registerService[HasRepositories](repos)

      cmdHandlerRegistry = HasCommandHandlers()
      almhirt.registerService[HasCommandHandlers](cmdHandlerRegistry)

      cmdExecutor = CommandExecutor(cmdHandlerRegistry, repos)(context, system).forceResult
      cmdExecutorRegistration = (context.commandChannel.actor ? SubscribeQry(MessagingSubscription.forActor[CommandEnvelope](cmdExecutor.actor)))(atMost)
        .mapTo[SubscriptionRsp]
        .map(_.registration)
        .toAlmFuture
        .awaitResult
        .forceResult
      almhirt.registerService[CommandExecutor](cmdExecutor)
      eventLog = SystemHelper.createEventLogFromFactory(almhirt, system).forceResult
      almhirt.registerService[DomainEventLog](eventLog)
      ().success
    }.flatMap(_ => super.registerComponents(almhirt, context, system))
  }


}