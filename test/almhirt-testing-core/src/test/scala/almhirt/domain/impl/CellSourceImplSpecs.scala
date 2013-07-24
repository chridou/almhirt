package almhirt.domain.impl

import akka.actor._
import almhirt.testkit._
import almhirt.testkit.domain.CellSourceSpecs
import almhirt.core.HasAlmhirt

class CellSourceImplSpecs extends CellSourceSpecs(ActorSystem("AggregateRootCellSystem", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesInMemoryEventLog {
  val defaultDuration = scala.concurrent.duration.FiniteDuration(1, "s")
  override def createCellForAR1(testId: Int, eventLog: ActorRef): ActorRef = 
  	this.system.actorOf(Props(new AggregateRootCellImpl[AR1, AR1Event](
      this.managedAggregateRootId, AR1.rebuildFromHistory, eventLog, () => ())(this.theAlmhirt)), "Ar1Cell_" + testId)
}