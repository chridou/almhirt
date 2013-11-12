package almhirt.components.impl

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.domain.AggregateRootCellStateSink

trait SupervisioningActorCellSource { actor: Actor =>
  import almhirt.components.AggregateRootCellSource.CellStateNotification
  protected def createProps(aggregateRootId: JUUID, forArType: Class[_], aggregateRootCellStateSink: AggregateRootCellStateSink): Props

  final protected def createCell(aggregateRootId: JUUID, forArType: Class[_]): ActorRef = 
    context.actorOf(createProps(aggregateRootId, forArType, cellState => self ! CellStateNotification(aggregateRootId, cellState)), aggregateRootId.toString())
}