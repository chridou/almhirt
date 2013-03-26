package almhirt.environment.configuration.bootstrappers

import scala.concurrent.duration.Duration
import akka.event.LoggingAdapter
import akka.pattern._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.environment._
import almhirt.messaging._
import almhirt.util._
import almhirt.parts.CommandExecutor
import almhirt.commanding.CommandEnvelope

trait CreatesAndRegistersCommandExecutor extends CreatesCoreComponentsBootstrapperPhase { self: HasStandardChannels with HasRepositoryRegistry with HasCommandHandlerRegistry with HasServiceRegistry =>
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      implicit val atMost = Duration(1, "s")
      implicit val executionContext = theAlmhirt.executionContext
      implicit val hasExecutionContext = theAlmhirt
      inTryCatch {
        startUpLogger.info(s"Create CommandExecutor")
        val cmdExecutor = CommandExecutor(self.commandHandlerRegistry, self.repositoryRegistry)
        startUpLogger.info(s"Register CommandExecutor as listener to CommandChannel")
        val cmdExecutorRegistration =
          (self.commandChannel.actor ? SubscribeQry(MessagingSubscription.forActor[CommandEnvelope](cmdExecutor.actor)))(atMost)
            .mapTo[SubscriptionRsp]
            .map(_.registration)
            .toAlmFuture
            .awaitResult
            .forceResult
        startUpLogger.info(s"Register CommandExecutor")
        self.serviceRegistry.registerService[CommandExecutor](cmdExecutor)
        BootstrapperPhaseSuccess(CleanUpAction(() => cmdExecutorRegistration.dispose, "CommandExecutor"))
      }.toBootstrapperPhaseResult
    }
}