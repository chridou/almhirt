package almhirt.environment.configuration.bootstrappers

import akka.event.LoggingAdapter
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.parts.HasRepositories

trait CreatesAndRegistersHasRepositories extends CreatesCoreComponentsBootstrapperPhase with HasRepositoryRegistry { self: HasServiceRegistry =>
  override def repositoryRegistry: HasRepositories = myRepositoryRegistry
  private var myRepositoryRegistry: HasRepositories = null
  
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      startUpLogger.info(s"Create HasRepositories")
      myRepositoryRegistry = HasRepositories()
      startUpLogger.info(s"Register HasRepositories")
      self.serviceRegistry.registerService[HasRepositories](myRepositoryRegistry)
      BootstrapperPhaseSuccess()
    }
}
