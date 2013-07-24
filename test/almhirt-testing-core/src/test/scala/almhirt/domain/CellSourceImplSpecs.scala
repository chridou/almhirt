package almhirt.domain

import akka.actor._
import almhirt.testkit._
import almhirt.testkit.domain.CellSourceSpecs
import almhirt.domain.impl.AggregateRootCellImpl

class CellSourceImplSpecs extends CellSourceSpecs(ActorSystem("AggregateRootCellSystem", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesInMemoryEventLog {
  val defaultDuration = scala.concurrent.duration.FiniteDuration(1, "s")
  override def createCellForAR1(testId: Int, managedAggregateRootId: java.util.UUID, eventLog: ActorRef): ActorRef = 
  	this.system.actorOf(Props(new AggregateRootCellImpl[AR1, AR1Event](
      managedAggregateRootId, AR1.rebuildFromHistory, eventLog, () => ())(this.theAlmhirt)), "Ar1Cell_" + testId)
}