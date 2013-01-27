package almhirt.core

import com.typesafe.config.Config

trait HasConfig {
  def config: Config
}