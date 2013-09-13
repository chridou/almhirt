package almhirt.components.impl

import java.util.{ UUID => JUUID }
import akka.actor._
import java.util.{UUID => JUUID}

trait SupervisioningActorCellSource { actor: Actor =>
  import almhirt.components.AggregateRootCellSource.DoesNotExistNotification
  protected def createProps(aggregateRootId: JUUID, forArType: Class[_], onDoesNotExist: () => Unit): Props

  final protected def createCell(aggregateRootId: JUUID, forArType: Class[_]): ActorRef = 
    context.actorOf(createProps(aggregateRootId, forArType, () => self ! DoesNotExistNotification(aggregateRootId)), aggregateRootId.toString())
}