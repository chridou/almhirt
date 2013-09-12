package almhirt.domain

import akka.actor._
import almhirt.testkit._
import almhirt.domain.impl.AggregateRootCellImpl
import almhirt.testkit.domain.AggregateRootCellSpecsTemplate

class AggregateRootCellSpecs extends AggregateRootCellSpecsTemplate(ActorSystem("CellSourceImplSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesInMemoryDomainEventLog {
  override val defaultDuration = scala.concurrent.duration.FiniteDuration(1, "s")
  override def createCellForAR1(testId: Int, managedAggregateRootId: java.util.UUID, eventLog: ActorRef): ActorRef = 
  	this.system.actorOf(Props(new AggregateRootCellImpl[AR1, AR1Event](
      managedAggregateRootId, AR1.rebuildFromHistory, eventLog, () => (), 2000, 2000)(this.theAlmhirt)), "Ar1Cell_" + testId)
}