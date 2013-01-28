package almhirt.ext.core.riftwarp

import com.typesafe.config.Config
import almhirt.environment.configuration.impl._

class RiftWarpDefaultBootstrapper(config: Config) extends AlmhirtBaseBootstrapper(config) with RiftWarpBootstrapper