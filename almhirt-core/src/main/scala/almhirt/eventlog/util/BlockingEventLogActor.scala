package almhirt.eventlog.util

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import scalaz.std._
import org.joda.time.DateTime
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.eventlog._
import almhirt.almakka.AlmActorLogging

class BlockingEventLogActor(eventStorage: SyncEventStorage, predicate: Event => Boolean, theAlmhirt: Almhirt) extends Actor {
  private def publishProblem(problem: Problem) {
    theAlmhirt.publishProblemWithSender(problem, self.path.name)
  }

  def receive: Receive = {
    case cmd: EventLogCmd =>
      cmd match {
        case LogEventQry(event, correlationId) =>
          if (predicate(event)) {
            if (event.header.sender != Some(self.path.name)) {
              val res = eventStorage.storeEvent(event)
              res.onFailure(publishProblem)
              sender ! LoggedEventRsp(res, correlationId)
            } else {
              sender ! LoggedEventRsp(event.success, correlationId)
            }
          } else {
            sender ! LoggedEventRsp(event.success, correlationId)
          }
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
