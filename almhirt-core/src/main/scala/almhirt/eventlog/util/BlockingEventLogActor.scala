package almhirt.eventlog.util

import java.util.{ UUID => JUUID }
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
import almhirt.eventlog._
import almhirt.almakka.AlmActorLogging

class BlockingEventLogActor(eventStorage: SyncEventStorage, theAlmhirt: Almhirt) extends Actor with CanLogProblems with AlmActorLogging {
  def receive: Receive = {
    case cmd: EventLogCmd =>
      println(cmd)
      cmd match {
        case LogEventQry(event, correlationId) =>
          val res = eventStorage.storeEvent(event)
          sender ! LoggedEventRsp(res, correlationId)

        case GetAllEventsQry(chunkSize, correlationId) =>
          val res = eventStorage.getAllEvents
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
        case GetEventQry(eventId, chunkSize, correlationId) =>
          val res = eventStorage.getEventById(eventId)
          sender ! EventRsp(res, correlationId)
        case GetEventsFromQry(from, chunkSize, correlationId) =>
          val res = eventStorage.getAllEventsFrom(from)
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
        case GetEventsUntilQry(until, chunkSize, correlationId) =>
          val res = eventStorage.getAllEventsUntil(until)
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
        case GetEventsFromUntilQry(from, until, chunkSize, correlationId) =>
          val res = eventStorage.getAllEventsFromUntil(from, until)
          sender ! EventsRsp(EventsChunk(0, true, res), correlationId)
      }
  }
}
