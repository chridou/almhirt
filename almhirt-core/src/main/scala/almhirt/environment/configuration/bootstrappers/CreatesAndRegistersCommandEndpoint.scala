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
        ConfigHelper.tryGetNotDisabledSubConfig(config, ConfigPaths.commandEndpoint).foreach { _ =>
          startUpLogger.info(s"Create CommandEndpoint")
          val endpoint = SystemHelper.createCommandEndpointFromFactory(theAlmhirt).forceResult
          startUpLogger.info(s"Register CommandEndpoint")
          self.serviceRegistry.registerService[CommandEndpoint](endpoint)
        }
        BootstrapperPhaseSuccess()
      }.toBootstrapperPhaseResult
    }
}