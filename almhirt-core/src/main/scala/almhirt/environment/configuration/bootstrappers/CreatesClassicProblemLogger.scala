package almhirt.environment.configuration.bootstrappers

import scala.concurrent.duration.Duration
import akka.event.LoggingAdapter
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.environment._
import almhirt.messaging._
import akka.actor.Props

trait CreatesClassicProblemLogger extends CreatesCoreComponentsBootstrapperPhase { self: HasStandardChannels with HasConfig =>
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      implicit val atMost = Duration(1, "s")
      implicit val executionContext = theAlmhirt.executionContext
      implicit val hasExecutionContext = theAlmhirt
      inTryCatch {
        ConfigHelper.tryGetNotDisabledSubConfig(config, ConfigPaths.problems).map { subConf =>
          startUpLogger.info("Create ProblemLogger")
          val minSeverity =
            ConfigHelper.problems.minSeverity(subConf).fold(
              fail => {
                startUpLogger.warning(s"Could not determine minSeverity: ${fail.message}. Using Minor as minSeverity")
                Minor
              },
              succ => succ)
          val actorName = ConfigHelper.problems.getActorName(subConf)
          val problemLogger = theAlmhirt.actorSystem.actorOf(Props(new almhirt.util.impl.ProblemLogger(minSeverity)), actorName)
          val problemloggerRegistration =
            (problemChannel.actor ? SubscribeQry(MessagingSubscription.forActor[Problem](problemLogger)))(atMost)
              .mapTo[SubscriptionRsp]
              .map(_.registration)
              .toAlmFuture
              .awaitResult
              .forceResult
          startUpLogger.info(s"ProblemLogger has path ${problemLogger.path.toString()}")
          BootstrapperPhaseSuccess(CleanUpAction(() => problemloggerRegistration.dispose, "ClassicProblemLogger"))
        }.getOrElse(BootstrapperPhaseSuccess())
      }.toBootstrapperPhaseResult
    }
}