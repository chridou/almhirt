package almhirt.ext.core.unfiltered.configuration

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered.ForwardsCommandsFromHttpRequest
import unfiltered.netty._
import riftwarp._
import riftwarp.http.RiftWarpHttpFuns.RiftHttpFunsSettings
import riftwarp.http.RiftHttpContentTypeWithoutPrefixOps
import almhirt.ext.core.unfiltered.impl.HttpCommandEndpoint

trait UnfilteredBootstrapper extends AlmhirtBootstrapper {

  override def prepareGateways(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    for {
      superCleanUp <- super.prepareGateways(theAlmhirt, theServiceRegistry, startUpLogger)
      httpCommandEndpoint <- createHttpCommandEndpoint(theAlmhirt, theServiceRegistry, startUpLogger)
      plans <- {
        theServiceRegistry.registerService[ForwardsCommandsFromHttpRequest](httpCommandEndpoint)
        collectPlans(theAlmhirt, theServiceRegistry, startUpLogger)
      }
      restCleanUp <- createRestServer(theAlmhirt, theServiceRegistry, startUpLogger, plans)
    } yield () => { superCleanUp(); restCleanUp() }

  def collectPlans(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[List[unfiltered.netty.async.Plan]] =
    Nil.success

  private def createRestServer(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter, plans: List[unfiltered.netty.async.Plan]): AlmValidation[CleanUpAction] = {
    theServiceRegistry.getService[HasConfig].flatMap(hasConfig =>
      ConfigHelper.http.getConfig(hasConfig.config).flatMap { httpConfig =>
        val port = ConfigHelper.http.port(httpConfig).fold(fail => 8080, succ => succ)
        val maxContentLength = ConfigHelper.http.maxContentLength(httpConfig).fold(fail => 1024 * 1024, succ => succ)
        startUpLogger.info(s"Http gateway will be created on port $port with a maximum request size of $maxContentLength bytes")
        almhirt.almvalidation.funs.inTryCatch {
          val http = Http(port).chunked(maxContentLength)
          val httpWithPlans = plans.foldLeft(http) { case (http, plan) => http.plan(plan) }
          val startedHttp = httpWithPlans.start
          () => {
            startUpLogger.info("Stopping http endpoint")
            startedHttp.stop
          }
        }
      })
  }

  private def createHttpCommandEndpoint(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[ForwardsCommandsFromHttpRequest] =
    theServiceRegistry.getService[HasConfig].flatMap(hasConfig =>
      theServiceRegistry.getService[RiftWarp].map { riftWarp =>
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