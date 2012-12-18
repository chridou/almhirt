package almhirt.ext.core.riftwarp

import com.typesafe.config.Config
import almhirt.environment.configuration.impl.AlmhirtDefaultBootStrapper

class RiftWarpDefaultBootstrapper(config: Config) extends AlmhirtDefaultBootStrapper(config) with RiftWarpBootstrapper