package almhirt.environment.configuration

import almhirt.parts.HasCommandHandlers

trait HasCommandHandlerRegistry {
  def commandHandlerRegistry: HasCommandHandlers
}