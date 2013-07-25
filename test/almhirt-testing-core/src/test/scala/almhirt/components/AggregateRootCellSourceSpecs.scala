package almhirt.components

import akka.actor._
import almhirt.testkit._
import almhirt.testkit.components.AggregateRootCellSourceSpecsTemplate

class AggregateRootCellSourceSpecs
  extends AggregateRootCellSourceSpecsTemplate(ActorSystem("CellSourceImplSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesCellSourceForTestAggregateRoots
  with CreatesInMemoryEventLog {

}