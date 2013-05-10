package almhirt.ext.core.unfiltered.configuration

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.http._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.util._
import almhirt.ext.http.unfiltered.UnfilteredRequestResponse
import almhirt.ext.core.unfiltered.UnfilteredHttpCommandEndpoint
import almhirt.util.http.HttpCommandEndpoint

trait UnfilteredBootstrapper extends PreparesGatewaysBootstrapperPhase { self: HasConfig with HasServiceRegistry with UnfilteredBootstrapperPlansCollector =>

  override def prepareGateways(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.prepareGateways(theAlmhirt, startUpLogger).andThen {
      (for {
        httpCommandEndpoint <- createHttpCommandEndpoint(theAlmhirt, startUpLogger) 
        plans <- {
          self.serviceRegistry.registerService[UnfilteredHttpCommandEndpoint](createUnfilteredHttpCommandEndpoint(httpCommandEndpoint, theAlmhirt, startUpLogger))
          self.collectPlans(theAlmhirt, startUpLogger)
        }
        restCleanUp <- createRestServer(theAlmhirt, startUpLogger, plans)
      } yield BootstrapperPhaseSuccess(restCleanUp)).toBootstrapperPhaseResult
    }

  private def createRestServer(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter, plans: List[unfiltered.netty.async.Plan]): AlmValidation[CleanUpAction] = {
    ConfigHelper.http.getConfig(self.config).flatMap { httpConfig =>
      val port = ConfigHelper.http.port(httpConfig).fold(fail => 8080, succ => succ)
      val maxContentLength = ConfigHelper.http.maxContentLength(httpConfig).fold(fail => 1024 * 1024, succ => succ)
      startUpLogger.info(s"Http gateway will be created on port $port with a maximum request size of $maxContentLength bytes")
      almhirt.almvalidation.funs.inTryCatch {
        val http = unfiltered.netty.Http(port).chunked(maxContentLength)
        val httpWithPlans = plans.foldLeft(http) { case (http, plan) => http.plan(plan) }
        val startedHttp = httpWithPlans.start
        CleanUpAction(() => {
          startUpLogger.info("Stopping http endpoint")
          startedHttp.stop
        }, "Unfiltered")
      }
    }
  }

  private def createUnfilteredHttpCommandEndpoint(httpCommandEndpoint: HttpCommandEndpoint[unfiltered.request.HttpRequest[Any]], theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): UnfilteredHttpCommandEndpoint = 
      new UnfilteredHttpCommandEndpoint(httpCommandEndpoint, UnfilteredRequestResponse, theAlmhirt.createProblemConsumer(), theAlmhirt)

  private def createHttpCommandEndpoint(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[HttpCommandEndpoint[unfiltered.request.HttpRequest[Any]]] = {
    startUpLogger.info("Create HttpCommandEndpoint")
    for {
      hasMarshallers <- self.serviceRegistry.getService[HasHttpMarshallers]
      hasUnmarshallers <- self.serviceRegistry.getService[HasHttpUnmarshallers]
      commandUM <- hasUnmarshallers.getUnmarschaller[Command]
      ticketM <- hasMarshallers.getMarschaller[TrackingTicket]
      cwmrdUM <- hasUnmarshallers.getUnmarschaller[CommandWithMaxResponseDuration]
      rosM <- hasMarshallers.getMarschaller[ResultOperationState]
      httpInstances <- self.serviceRegistry.getService[HttpInstances]
      commandEndpoint <- self.serviceRegistry.getService[CommandEndpoint]
    } yield new HttpCommandEndpoint[unfiltered.request.HttpRequest[Any]](commandEndpoint)(
        commandUM, ticketM, cwmrdUM, rosM, httpInstances, UnfilteredRequestResponse, theAlmhirt.createProblemConsumer(), theAlmhirt)
  }
}

