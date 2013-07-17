package almhirt.domain.caching.impl

import java.util.{ UUID => JUUID }
import akka.actor._

trait SupervisioningActorCellSource { actor: Actor =>
  import almhirt.domain.caching.AggregateRootCellSource.DoesNotExistNotification
  protected def createProps(aggregateRootId: JUUID, forArType: Class[_], onDoesNotExist: () => Unit): Props

  final protected def createCell(aggregateRootId: JUUID, forArType: Class[_]): ActorRef =
    context.actorOf(createProps(aggregateRootId, forArType, () => self ! DoesNotExistNotification(aggregateRootId)))
}