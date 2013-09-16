package almhirt.testkit

import java.util.{UUID => JUUID}
import akka.actor._
import scala.concurrent.duration.FiniteDuration
import almhirt.components.impl.AggregateRootCellSourceImpl
import almhirt.domain.AggregateRootCell
import almhirt.testkit.domain._
import almhirt.core.HasAlmhirt

trait CreatesCellSource {
  def createCellSource(testId: Int, eventlog: ActorRef): ActorRef
}

trait CreatesCellSourceForTestAggregateRoots extends CreatesCellSource { self: HasAlmhirt =>
  def createCellSource(testId: Int, eventlog: ActorRef): ActorRef = { 
    val ar1Factory = AggregateRootCell.propsFactoryRaw[AR1, AR1Event](AR1.rebuildFromHistory _, eventlog, FiniteDuration(5, "s"), theAlmhirt)
    val ar2Factory = AggregateRootCell.propsFactoryRaw[AR2, AR2Event](AR2.rebuildFromHistory _, eventlog, FiniteDuration(5, "s"), theAlmhirt)
    val propsFactories: Map[Class[_], (JUUID, () => Unit) => Props] =
      Map(
        (classOf[AR1], ar1Factory),
        (classOf[AR2], ar2Factory))
    val props = Props(new AggregateRootCellSourceImpl(propsFactories.lift, theAlmhirt))
    this.theAlmhirt.actorSystem.actorOf(props, "CellSource_" + testId.toString())
  }  
}