package almhirt.components

import akka.actor._
import almhirt.testkit._
import almhirt.testkit.components.ExecutionStateTrackerSpecsTemplate

class ExecutionStateTrackerSpec
  extends ExecutionStateTrackerSpecsTemplate(ActorSystem("ExecutionStateTrackerSpec", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesInMemoryExecutionTracker {
  
  override val sleepMillisAfterFireAndForget = Some(20)

}