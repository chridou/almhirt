package almhirt.domain.impl

import scala.concurrent.Future
import scala.reflect.ClassTag
import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import akka.dispatch._
import almhirt.core._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.domain._
import almhirt.eventlog.DomainEventLog
import almhirt.environment._
import almhirt.util._
import almhirt.common.AlmFuture

abstract class AggregateRootRepositoryActorHull[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](val actor: ActorRef)(implicit sys: HasExecutionContext with HasDurations, arTag: ClassTag[AR]) extends AggregateRootRepository[AR, Event] with CanValidateAggregateRootsAgainstEvents[AR, Event] {
  implicit private def duration = sys.defaultDuration
  implicit private def futureContext = sys.executionContext

  def get(id: java.util.UUID): AlmFuture[AR] =
    (actor ? GetAggregateRootQry(id))(duration)
      .mapToAlmFutureOver((rsp:GetAggregateRootRsp) => rsp.ar.flatMap(ar => almhirt.almvalidation.funs.almCast[AR](ar)))

  def store(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]): Unit =
    (actor ! StoreAggregateRootCmd(ar, uncommittedEvents, ticket))
}