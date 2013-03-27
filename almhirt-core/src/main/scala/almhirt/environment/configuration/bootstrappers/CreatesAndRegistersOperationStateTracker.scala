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

trait CreatesAndRegistersOperationStateTracker extends CreatesCoreComponentsBootstrapperPhase { self: HasStandardChannels with HasServiceRegistry with HasConfig =>
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      implicit val atMost = Duration(1, "s")
      implicit val executionContext = theAlmhirt.executionContext
      implicit val hasExecutionContext = theAlmhirt
      inTryCatch {
        startUpLogger.info(s"""Create operation state tracker from config section "${ConfigPaths.operationState}"""")
        ConfigHelper.tryGetNotDisabledSubConfig(config, ConfigPaths.operationState) match {
          case Some(subConf) =>
            val tracker = SystemHelper.createOperationStateTrackerFromFactory(theAlmhirt).forceResult
            val trackerRegistartion =
              (self.operationStateChannel.actor ? SubscribeQry(MessagingSubscription.forActor[OperationState](tracker)))(atMost)
                .mapTo[SubscriptionRsp]
                .map(_.registration)
                .toAlmFuture
                .awaitResult
                .forceResult
            startUpLogger.info(s"Register operation state tracker")
            self.serviceRegistry.registerService[OperationStateTracker](almhirt.util.impl.OperationStateTrackerActorHull(tracker))
            BootstrapperPhaseSuccess(CleanUpAction(() => trackerRegistartion.dispose, "OperationStateTracker"))
          case None =>
            startUpLogger.warning("""Tried to initialize an operation state tracker, but it has no config section or is explicitly disabled with "disabled=true" in its config section.""")
            BootstrapperPhaseSuccess()
        }
      }.toBootstrapperPhaseResult
    }
}