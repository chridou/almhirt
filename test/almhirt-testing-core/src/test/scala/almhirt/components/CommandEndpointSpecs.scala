package almhirt.components

import almhirt.testkit.AlmhirtFromAkkaTestKitWithoutConfiguration
import almhirt.testkit.components.CommandEndpointSpecsTemplate
import almhirt.testkit._
import akka.actor._

class CommandEndpointSpecs
  extends CommandEndpointSpecsTemplate(ActorSystem("CommandEndpointSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesDefaultCommandEndpoint
  with CreatesCreatesInMemoryExecutionTracker {
}