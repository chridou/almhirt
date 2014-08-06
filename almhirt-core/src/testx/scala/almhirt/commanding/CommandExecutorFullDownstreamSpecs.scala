package almhirt.commanding

import akka.actor._
import almhirt.testkit._
import almhirt.testkit.commanding.CommandExecutorFullDownstreamSpecsTemplate

class CommandExecutorFullDownstreamSpecs extends CommandExecutorFullDownstreamSpecsTemplate(ActorSystem("CommandExecutorFullDownstreamSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithConfiguration
  with CreatesCellSourceForTestAggregateRoots
  with CreatesInMemoryDomainEventLog {
  override def config = TestConfiguration.default
}