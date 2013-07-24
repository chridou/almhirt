package almhirt.testkit

import java.util.{UUID => JUUID}
import akka.actor._
import almhirt.domain.caching.impl.AggregateRootCellSourceImpl
import almhirt.domain.impl.AggregateRootCellImpl
import almhirt.testkit.domain._
import almhirt.core.HasAlmhirt

trait CreatesCellSource {
  def createCellSource(specId: Int): ActorRef
}

trait CreatesCellSourceForTestAggregateRoots { self: CreatesEventLog with HasAlmhirt =>
  def createCellSource(specId: Int): ActorRef = { 
    val eventLog = this.createEventLog(specId)
    val propsFactories: Map[Class[_], (JUUID, () => Unit) => Props] =
      Map(
        (classOf[AR1], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[AR1, AR1Event](arId, AR1.rebuildFromHistory, eventLog, notifyDoesNotExist))),
        (classOf[AR1], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[AR2, AR2Event](arId, AR2.rebuildFromHistory, eventLog, notifyDoesNotExist))))
    val props = Props(new AggregateRootCellSourceImpl(propsFactories.lift))
    this.theAlmhirt.actorSystem.actorOf(props, "CellSource_" + specId.toString())
  }  
}