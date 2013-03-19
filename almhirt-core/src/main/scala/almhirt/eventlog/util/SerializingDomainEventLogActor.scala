package almhirt.eventlog.util

import java.util.{UUID => JUUID}
import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation
import scalaz.std._
import org.joda.time.DateTime
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.almakka.AlmActorLogging

class BlockingDomainEventLogActor(storesEvents: SyncDomainEventStorage, theAlmhirt: Almhirt) extends Actor with CanLogProblems with AlmActorLogging {
  def receive: Receive = {
    case LogEventsQry(events, executionIdent) =>
      val res = storesEvents.storeManyEvents(events)
      sender ! LoggedDomainEventsRsp(res._1, res._2, executionIdent)

    case GetAllDomainEventsQry(chunkSize, execIdent) =>
      val res = storesEvents.getAllEvents
      sender ! AllDomainEventsRsp(DomainEventsChunk(0, true, res), execIdent)
    case GetDomainEventsQry(aggId, chunkSize, execIdent) =>
      val res = storesEvents.getAllEventsFor(aggId)
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
    case GetDomainEventsFromQry(aggId, from, chunkSize, execIdent) =>
      val res = storesEvents.getAllEventsForFrom(aggId, from)
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
    case GetDomainEventsFromToQry(aggId, from, to, chunkSize, execIdent) =>
      val res = storesEvents.getAllEventsForFromTo(aggId, from, to)
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
  }
}
