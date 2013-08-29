package almhirt.testkit

import java.util.{UUID => JUUID}
import akka.actor._
import almhirt.components.impl.AggregateRootCellSourceImpl
import almhirt.domain.impl.AggregateRootCellImpl
import almhirt.testkit.domain._
import almhirt.core.HasAlmhirt

trait CreatesCellSource {
  def createCellSource(testId: Int, eventlog: ActorRef): ActorRef
}

trait CreatesCellSourceForTestAggregateRoots extends CreatesCellSource { self: HasAlmhirt =>

  def createCellSource(testId: Int, eventlog: ActorRef): ActorRef = { 
    val propsFactories: Map[Class[_], (JUUID, () => Unit) => Props] =
      Map(
        (classOf[AR1], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[AR1, AR1Event](arId, AR1.rebuildFromHistory, eventlog, notifyDoesNotExist))),
        (classOf[AR2], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[AR2, AR2Event](arId, AR2.rebuildFromHistory, eventlog, notifyDoesNotExist))))
    val props = Props(new AggregateRootCellSourceImpl(propsFactories.lift, theAlmhirt))
    this.theAlmhirt.actorSystem.actorOf(props, "CellSource_" + testId.toString())
  }  
}