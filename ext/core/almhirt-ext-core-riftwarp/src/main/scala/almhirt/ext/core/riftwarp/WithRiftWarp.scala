package almhirt.ext.core.riftwarp

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration.impl._
import almhirt.environment.configuration._
import _root_.riftwarp.RiftWarp
import almhirt.ext.core.riftwarp.serialization.RiftWarpUtilityFuns

trait WithRiftWarp extends AlmhirtBootstrapper {
  override def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    super.createCoreComponents(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { superCleanUp =>
      val riftWarp = RiftWarp.concurrentWithDefaults()
      RiftWarpUtilityFuns.addRiftWarpRegistrations(riftWarp)
      theServiceRegistry.registerService[RiftWarp](riftWarp)
      superCleanUp.success
    }
}