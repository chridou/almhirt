package almhirt.environment

import com.typesafe.config.Config

trait HasConfig {
  def config: Config
}