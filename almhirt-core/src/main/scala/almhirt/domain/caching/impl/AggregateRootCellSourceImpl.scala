package almhirt.domain.caching.impl

import java.util.{ UUID => JUUID }
import akka.actor._

class AggregateRootCellSourceImpl(cellPropsFactories: Class[_] => Option[(JUUID, () => Unit) => Props])
  extends AggregateRootCellSourceTemplate with Actor with ActorLogging {
  override protected def createProps(aggregateRootId: JUUID, forArType: Class[_], notifiyOnDoesNotExist: () => Unit): Props =
    cellPropsFactories(forArType) match {
      case Some(factory) => factory(aggregateRootId, notifiyOnDoesNotExist)
      case None => throw new Exception(s"""No factory to create props for aggregate root of type "${forArType.getName()} found."""")
    }
  
  override def receive: Receive = receiveAggregateRootCellSourceMessage
}