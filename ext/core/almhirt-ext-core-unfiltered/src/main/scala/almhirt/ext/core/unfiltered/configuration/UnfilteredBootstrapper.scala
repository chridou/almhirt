package almhirt.ext.core.unfiltered.configuration

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration.AlmhirtBootstrapper
import almhirt.environment.configuration.CleanUpAction
import almhirt.environment.configuration.ConfigHelper
import unfiltered.netty._

trait UnfilteredBootstrapper extends AlmhirtBootstrapper {
  override def prepareGateways(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    super.prepareGateways(theAlmhirt, theServiceRegistry, startUpLogger).flatMap(superCleanUp =>
      theServiceRegistry.getService[HasConfig].flatMap(hasConfig =>
        ConfigHelper.http.getConfig(hasConfig.config).flatMap(httpConfig =>
          collectPlans(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { plans =>
            val port = ConfigHelper.http.port(httpConfig).fold(fail => 8080, succ => succ)
            val maxContentLength = ConfigHelper.http.maxContentLength(httpConfig).fold(fail => 1024 * 1024, succ => succ)
            startUpLogger.info(s"Http gateway will be created on port $port with a maximum request size of $maxContentLength bytes")
            almhirt.almvalidation.funs.inTryCatch {
              val http = Http(port).chunked(maxContentLength)
              val httpWithPlans = plans.foldLeft(http){case (http, plan) => http.plan(plan)}
              val started = httpWithPlans.start
              () => { 
                superCleanUp()
                startUpLogger.info("Stopping http endpoint")
                started.stop }
            }
          })))

  def collectPlans(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[List[unfiltered.netty.async.Plan]] =
    Nil.success
}