package almhirt.domain

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.util.ExecutionStyle
import almhirt.util.NeedResponseExectionStyle

trait AggregateRootRepositoryMessages

sealed trait AggregateRootRepositoryCmd extends AggregateRootRepositoryMessages
case class GetAggregateRootQry(aggId: JUUID) extends AggregateRootRepositoryCmd
case class StoreAggregateRootCmd(ar: IsAggregateRoot, uncommittedEvents: IndexedSeq[DomainEvent], style: ExecutionStyle) extends AggregateRootRepositoryCmd

sealed trait AggregateRootRepositoryRsp extends AggregateRootRepositoryMessages
case class GetAggregateRootRsp(quriedId: JUUID, ar: AlmValidation[IsAggregateRoot]) extends AggregateRootRepositoryRsp
case class StoreAggregateRootRsp(ar: AlmValidation[IsAggregateRoot], stylePostBack: NeedResponseExectionStyle) extends AggregateRootRepositoryRsp
