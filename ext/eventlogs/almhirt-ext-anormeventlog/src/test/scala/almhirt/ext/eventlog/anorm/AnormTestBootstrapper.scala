package almhirt.ext.eventlog.anorm

import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core.ServiceRegistry
import almhirt.environment._
import almhirt.environment.configuration.CleanUpAction
import almhirt.environment.configuration.impl.AlmhirtTestingBootstrapper
import riftwarp.RiftWarp
import com.typesafe.config.Config

class AnormTestBootstrapper(config: Config) extends AlmhirtTestingBootstrapper(config) {
  override def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = {
    val riftwarp = RiftWarp.concurrentWithDefaults
    val barracks = riftwarp.barracks

    barracks.addDecomposer(new TestPersonCreatedDecomposer)
    barracks.addDecomposer(new TestPersonNameChangedDecomposer)
    barracks.addDecomposer(new TestPersonAddressAquiredDecomposer)
    barracks.addDecomposer(new TestPersonMovedDecomposer)
    barracks.addDecomposer(new TestPersonUnhandledEventDecomposer)

    barracks.addRecomposer(new TestPersonCreatedRecomposer)
    barracks.addRecomposer(new TestPersonNameChangedRecomposer)
    barracks.addRecomposer(new TestPersonAddressAquiredRecomposer)
    barracks.addRecomposer(new TestPersonMovedRecomposer)
    barracks.addRecomposer(new TestPersonUnhandledEventRecomposer)

    theServiceRegistry.registerService[RiftWarp](riftwarp)
    super.createCoreComponents(theAlmhirt, theServiceRegistry, startUpLogger)
  }
}