package almhirt.core.riftwarp.test

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration._
import com.typesafe.config.Config
import _root_.riftwarp.RiftWarp
import almhirt.ext.core.riftwarp.RiftWarpBootstrapper

trait WithTestDecomposersAndWarpUnpackersBootstrapper extends CreatesCoreComponentsBootstrapperPhase { self: HasServiceRegistry =>
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      startUpLogger.debug("Register RiftWarp-Decomposers and -WarpUnpackers for testing.")
      self.serviceRegistry.getService[RiftWarp].map { rw =>
        rw.packers.addTyped(TestPersonCreatedPacker)
        rw.unpackers.addTyped(TestPersonCreatedWarpUnpacker)
        rw.packers.addTyped(TestPersonNameChangedPacker)
        rw.unpackers.addTyped(TestPersonNameChangedWarpUnpacker)
        rw.packers.addTyped(TestPersonAddressAquiredPacker)
        rw.unpackers.addTyped(TestPersonAddressAquiredWarpUnpacker)
        rw.packers.addTyped(TestPersonMovedPacker)
        rw.unpackers.addTyped(TestPersonMovedWarpUnpacker)
        rw.packers.addTyped(TestPersonUnhandledEventPacker)
        rw.unpackers.addTyped(TestPersonUnhandledEventWarpUnpacker)
        BootstrapperPhaseSuccess()
      }.toBootstrapperPhaseResult
    }
}
