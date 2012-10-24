package almhirt.commanding

import almhirt._
import almhirt.domain._
import almhirt.context._

trait HandlesCommand {
  def commandType: Class[_ <: DomainCommand]
  def handle(com: DomainCommand, env: AlmhirtEnvironment, context: AlmhirtContext): Unit
}

