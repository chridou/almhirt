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

  override def registerComponents(implicit almhirt: Almhirt): AlmValidation[CleanUpAction] = {
    almhirt.serviceRegistry match {
      case Some(sr) =>
        import akka.pattern._
        implicit val atMost = FiniteDuration(5, "s")
        implicit val executionContext = almhirt.executionContext

        inTryCatch {
          tracker = OperationStateTracker().forceResult
          trackerRegistration =
            almhirt.getService[OperationStateChannel].flatMap(channel =>
              (channel.actor ? SubscribeQry(MessagingSubscription.forActor[OperationState](tracker.actor)))(atMost)
                .mapTo[SubscriptionRsp]
                .map(_.registration)
                .toAlmFuture
                .awaitResult)
              .forceResult
          sr.registerService[OperationStateTracker](tracker)

          repos = HasRepositories().forceResult
          sr.registerService[HasRepositories](repos)

          cmdHandlerRegistry = HasCommandHandlers()
          sr.registerService[HasCommandHandlers](cmdHandlerRegistry)

          cmdExecutor = CommandExecutor(cmdHandlerRegistry, repos).forceResult
          cmdExecutorRegistration =
            almhirt.getService[CommandChannel].flatMap(channel =>
              (channel.actor ? SubscribeQry(MessagingSubscription.forActor[CommandEnvelope](cmdExecutor.actor)))(atMost)
                .mapTo[SubscriptionRsp]
                .map(_.registration)
                .toAlmFuture
                .awaitResult)
              .forceResult
          sr.registerService[CommandExecutor](cmdExecutor)
          eventLog = SystemHelper.createEventLogFromFactory.forceResult
          sr.registerService[DomainEventLog](eventLog)
          (() => {
            cmdExecutorRegistration.dispose
            trackerRegistration.dispose; tracker.dispose
            eventLog.close
          })
        }.flatMap(cleanUp => super.registerComponents(almhirt).map(superCleanUp => () => { cleanUp(); superCleanUp() }))
      case None => scalaz.Failure(UnspecifiedProblem("Cannot register services without a service registry"))
    }
  }
}