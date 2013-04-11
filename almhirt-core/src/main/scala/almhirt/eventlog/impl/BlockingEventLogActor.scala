package almhirt.eventlog.impl

import scalaz.std._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.eventlog._
import almhirt.util._
import almhirt.eventlog.SyncEventStorage

class BlockingEventLogActor(eventStorage: SyncEventStorage, predicate: Event => Boolean, theAlmhirt: Almhirt) extends Actor {
  private def publishProblem(problem: Problem) {
    theAlmhirt.publishProblemWithSender(problem, self.path.name)
  }

  private def launderEvent(event: Event): Event =
    event match {
      case OperationStateEvent(header, InProcess(ticket, FullComandInfo(cmd), timestamp)) =>
        OperationStateEvent(header, InProcess(ticket, HeadCommandInfo(cmd), timestamp))
      case x =>
        x
    }

  def receive: Receive = {
    case cmd: EventLogCmd =>
      cmd match {
        case LogEventQry(event, correlationId) =>
          eventStorage.consume(launderEvent(event))
        case GetAllEventsQry(chunkSize, correlationId) =>
          val res = eventStorage.getAllEvents
          res.onFailure(publishProblem)
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
        case GetEventQry(eventId, chunkSize, correlationId) =>
          val res = eventStorage.getEventById(eventId)
          res.onFailure(publishProblem)
          sender ! EventRsp(res, correlationId)
        case GetEventsFromQry(from, chunkSize, correlationId) =>
          val res = eventStorage.getAllEventsFrom(from)
          res.onFailure(publishProblem)
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
        case GetEventsUntilQry(until, chunkSize, correlationId) =>
          val res = eventStorage.getAllEventsUntil(until)
          res.onFailure(publishProblem)
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
        case GetEventsFromUntilQry(from, until, chunkSize, correlationId) =>
          val res = eventStorage.getAllEventsFromUntil(from, until)
          res.onFailure(publishProblem)
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
      }
  }
}
