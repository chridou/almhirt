package almhirt.domain.components

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.domain.IsAggregateRoot
import scala.collection.mutable._

class GrowOnlyAggregateRootCacheActor extends Actor {
  val cache: Map[JUUID, IsAggregateRoot] = Map.empty
  override def receive: Receive = {
    case ev: AggregateRootCacheReq =>
      ev match {
        case GetCachedAggregateRootQry(id) => sender ! AggregateRootFromCacheRsp(cache.get(id), id)
        case CacheAggregateRootCmd(ar) => cache.put(ar.id, ar)
        case RemoveAggregateRootFromCacheCmd(id) => cache.remove(id)
        case ContainsCachedAggregateRootQry(id) => sender ! ContainsCachedAggregateRootRsp(cache.contains(id), id)
      }
  }
}