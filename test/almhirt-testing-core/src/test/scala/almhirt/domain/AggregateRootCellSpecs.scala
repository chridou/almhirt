package almhirt.domain

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.testkit._
import almhirt.testkit.domain.AggregateRootCellSpecsTemplate

class AggregateRootCellSpecs extends AggregateRootCellSpecsTemplate(ActorSystem("CellSourceImplSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesInMemoryDomainEventLog {
  override val defaultDuration = scala.concurrent.duration.FiniteDuration(1, "s")
  override def createCellForAR1(testId: Int, managedAggregateRootId: java.util.UUID, eventLog: ActorRef): ActorRef = {
    val propsFactory = AggregateRootCell.propsFactoryRaw[AR1, AR1Event](AR1.rebuildFromHistory _, eventLog, FiniteDuration(5, "s"), theAlmhirt)
    this.system.actorOf(propsFactory(managedAggregateRootId, () => ()), "Ar1Cell_" + testId)
  }
}