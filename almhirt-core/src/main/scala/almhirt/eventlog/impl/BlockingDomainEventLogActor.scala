package almhirt.eventlog.impl
import scalaz.std._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.eventlog._
import almhirt.almakka.AlmActorLogging
import almhirt.eventlog.SyncDomainEventStorage

class BlockingDomainEventLogActor(domainEventStorage: SyncDomainEventStorage, theAlmhirt: Almhirt) extends Actor with CanLogProblems with AlmActorLogging {
  def receive: Receive = {
    case event: DomainEventLogCmd =>
      event match {
        case LogDomainEventsQry(events, executionIdent) =>
          val res = domainEventStorage.storeEvents(events)
          sender ! LoggedDomainEventsRsp(res._1, res._2, executionIdent)
        case GetDomainEventByIdQry(id, correlationId) =>
          val res = domainEventStorage.getEventById(id)
          sender ! DomainEventByIdRsp(res, correlationId)
        case GetAllDomainEventsQry(chunkSize, execIdent) =>
          val res = domainEventStorage.getAllEvents
          sender ! AllDomainEventsRsp(DomainEventsChunk(0, true, res), execIdent)
        case GetDomainEventsQry(aggId, chunkSize, execIdent) =>
          val res = domainEventStorage.getAllEventsFor(aggId)
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
        case GetDomainEventsFromQry(aggId, from, chunkSize, execIdent) =>
          val res = domainEventStorage.getAllEventsForFrom(aggId, from)
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
        case GetDomainEventsToQry(aggId, to, chunkSize, execIdent) =>
          val res = domainEventStorage.getAllEventsForTo(aggId, to)
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
        case GetDomainEventsFromToQry(aggId, from, to, chunkSize, execIdent) =>
          val res = domainEventStorage.getAllEventsForFromTo(aggId, from, to)
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
      }
  }
}
