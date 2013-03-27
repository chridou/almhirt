package almhirt.environment.configuration.bootstrappers

import akka.event.LoggingAdapter
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.util.CommandEndpoint

trait CreatesAndRegistersCommandEndpoint extends CreatesCoreComponentsBootstrapperPhase { self: HasServiceRegistry with HasConfig =>
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      inTryCatch {
        startUpLogger.info(s"""Create CommandEndpoint from config section "${ConfigPaths.commandEndpoint}"""")
        ConfigHelper.tryGetNotDisabledSubConfig(config, ConfigPaths.commandEndpoint) match {
          case Some(subconf) =>
            val endpoint = SystemHelper.createCommandEndpointFromFactory(theAlmhirt).forceResult
            startUpLogger.info(s"Register CommandEndpoint")
            self.serviceRegistry.registerService[CommandEndpoint](endpoint)
            BootstrapperPhaseSuccess()
          case None =>
            startUpLogger.warning("""Tried to initialize a command endpoint, but it has no config section or is explicitly disabled with "disabled=true" in its config section.""")
            BootstrapperPhaseSuccess()
        }
      }.toBootstrapperPhaseResult
    }
}