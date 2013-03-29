package almhirt.domain.components

import akka.actor._
import almhirt.common._
import almhirt.core._

class DevNullAggregateRootCacheActor extends Actor {
  override def receive: Receive = {
    case ev: AggregateRootCacheReq =>
      ev match {
        case GetCachedAggregateRootQry(id) => sender ! AggregateRootFromCacheRsp(None, id)
        case CacheAggregateRootCmd(ar) => ()
        case RemoveAggregateRootFromCacheCmd(id) => ()
        case ContainsCachedAggregateRootQry(id) => sender ! ContainsCachedAggregateRootRsp(false, id)
      }
  }
}