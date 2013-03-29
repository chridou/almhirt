package almhirt.domain.components

import java.util.{UUID => JUUID}
import almhirt.common._
import almhirt.domain.IsAggregateRoot

sealed trait AggregateRootCacheMessage

sealed trait AggregateRootCacheReq extends AggregateRootCacheMessage
final case class GetCachedAggregateRootQry(id: JUUID) extends AggregateRootCacheReq
final case class CacheAggregateRootCmd(ar: IsAggregateRoot) extends AggregateRootCacheReq
final case class RemoveAggregateRootFromCacheCmd(id: JUUID) extends AggregateRootCacheReq
final case class ContainsCachedAggregateRootQry(id: JUUID) extends AggregateRootCacheReq

sealed trait AggregateRootCacheRsp extends AggregateRootCacheMessage
final case class AggregateRootFromCacheRsp(ar: Option[IsAggregateRoot], queriedId: JUUID)
final case class ContainsCachedAggregateRootRsp(isContained: Boolean, queriedId: JUUID)
