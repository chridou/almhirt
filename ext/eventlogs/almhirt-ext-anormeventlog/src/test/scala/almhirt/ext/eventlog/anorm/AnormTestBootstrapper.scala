package almhirt.ext.eventlog.anorm

import com.typesafe.config.Config
import almhirt.environment.configuration.impl.AlmhirtTestingBootstrapper
import almhirt.environment._
import almhirt.common._
import riftwarp.RiftWarp

class AnormTestBootstrapper(config: Config) extends AlmhirtTestingBootstrapper(config) {
  override def registerComponents(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] = {
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

    almhirt.registerService[RiftWarp](riftwarp)
    super.registerComponents(almhirt, context, system)
  }
}