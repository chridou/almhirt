package almhirt.core.riftwarp.test

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration.impl._
import almhirt.environment.configuration._
import com.typesafe.config.Config
import _root_.riftwarp.RiftWarp
import almhirt.ext.core.riftwarp.RiftWarpBootstrapper

trait WithTestDecomposersAndRecomposersBootstrapper extends CreatesCoreComponentsBootstrapperPhase { self: HasServiceRegistry =>
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      startUpLogger.debug("Register RiftWarp-Decomposers and -Recomposers for testing.")
      self.serviceRegistry.getService[RiftWarp].map { riftWarp =>
        riftWarp.barracks.addDecomposer(new TestPersonCreatedDecomposer)
        riftWarp.barracks.addRecomposer(new TestPersonCreatedRecomposer)
        riftWarp.barracks.addDecomposer(new TestPersonNameChangedDecomposer)
        riftWarp.barracks.addRecomposer(new TestPersonNameChangedRecomposer)
        riftWarp.barracks.addDecomposer(new TestPersonAddressAquiredDecomposer)
        riftWarp.barracks.addRecomposer(new TestPersonAddressAquiredRecomposer)
        riftWarp.barracks.addDecomposer(new TestPersonMovedDecomposer)
        riftWarp.barracks.addRecomposer(new TestPersonMovedRecomposer)
        riftWarp.barracks.addDecomposer(new TestPersonUnhandledEventDecomposer)
        riftWarp.barracks.addRecomposer(new TestPersonUnhandledEventRecomposer)
        BootstrapperPhaseSuccess()
      }.toBootstrapperPhaseResult
    }
}
