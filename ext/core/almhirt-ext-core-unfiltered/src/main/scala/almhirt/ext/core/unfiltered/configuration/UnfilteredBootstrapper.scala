package almhirt.ext.core.unfiltered.configuration

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered.ForwardsCommandsFromHttpRequest
import unfiltered.netty._
import almhirt.ext.core.unfiltered.impl.HttpCommandEndpoint

trait UnfilteredBootstrapper extends PreparesGatewaysBootstrapperPhase { self: HasConfig with HasServiceRegistry with UnfilteredBootstrapperPlansCollector =>

  override def prepareGateways(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.prepareGateways(theAlmhirt, startUpLogger).andThen {
      (for {
        httpCommandEndpoint <- createHttpCommandEndpoint(theAlmhirt, startUpLogger)
        plans <- {
          self.serviceRegistry.registerService[ForwardsCommandsFromHttpRequest](httpCommandEndpoint)
          self.collectPlans(theAlmhirt, startUpLogger)
        }
        restCleanUp <- createRestServer(theAlmhirt, startUpLogger, plans)
      } yield BootstrapperPhaseSuccess(restCleanUp)).toBootstrapperPhaseResult
    }

  private def createRestServer(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter, plans: List[unfiltered.netty.async.Plan]): AlmValidation[CleanUpAction] = {
    self.serviceRegistry.getService[HasConfig].flatMap(hasConfig =>
      ConfigHelper.http.getConfig(hasConfig.config).flatMap { httpConfig =>
        val port = ConfigHelper.http.port(httpConfig).fold(fail => 8080, succ => succ)
        val maxContentLength = ConfigHelper.http.maxContentLength(httpConfig).fold(fail => 1024 * 1024, succ => succ)
        startUpLogger.info(s"Http gateway will be created on port $port with a maximum request size of $maxContentLength bytes")
        almhirt.almvalidation.funs.inTryCatch {
          val http = Http(port).chunked(maxContentLength)
          val httpWithPlans = plans.foldLeft(http) { case (http, plan) => http.plan(plan) }
          val startedHttp = httpWithPlans.start
          CleanUpAction(() => {
            startUpLogger.info("Stopping http endpoint")
            startedHttp.stop
          }, "Unfiltered")
        }
      })
  }

  private def createHttpCommandEndpoint(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[ForwardsCommandsFromHttpRequest] =
    self.serviceRegistry.getService[HasConfig].flatMap(hasConfig =>
      self.serviceRegistry.getService[RiftWarp].map { riftWarp =>
        val settings = RiftHttpFunsSettings(
          riftWarp,
          false,
          almhirt.http.impl.JustForTestingProblemLaundry,
          prob => theAlmhirt.publishProblem(prob),
          RiftChannel.Json,
          new RiftHttpContentTypeWithoutPrefixOps(riftWarp.channels))
        startUpLogger.info("Create HttpCommandEndpoint")
        val maxSyncCallDuration =
          (ConfigHelper.http.getConfig(hasConfig.config).flatMap(ConfigHelper.http.maxSyncCommandDuration(_))).fold(
            fail => {
              startUpLogger.info(s"""MaxSyncCommandDuration could not be determined: ${fail.message}. Defaulting to long duration(${theAlmhirt.durations.longDuration.toString})""")
              theAlmhirt.durations.longDuration
            },
            succ => {
              startUpLogger.info(s"""MaxSyncCommandDuration has been set to ${succ.toString}""")
              succ
            })
        new HttpCommandEndpoint(() => theAlmhirt.getService[CommandEndpoint], settings, theAlmhirt, maxSyncCallDuration)
      })
}