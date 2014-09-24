package almhirt.eventlog

import java.util.{ UUID ⇒ JUUID }
import org.joda.time.LocalDateTime
import akka.actor._
import almhirt.common._
import play.api.libs.iteratee.Enumerator

object EventLog {
  sealed trait EventLogMessage

  final case class LogEvent(event: Event) extends EventLogMessage
  sealed trait LogEventResponse extends EventLogMessage { def eventId: EventId }
  final case class EventLogged(eventId: EventId) extends EventLogMessage
  final case class EventNotLogged(eventId: EventId, problem: Problem) extends EventLogMessage

  final case class FindEvent(eventId: EventId) extends EventLogMessage
  sealed trait FindEventResponse extends EventLogMessage 
  final case class FoundEvent(eventId: EventId, event: Option[Event]) extends FindEventResponse
  final case class FindEventFailed(eventId: EventId, problem: Problem) extends FindEventResponse
  
  
  case object FetchAllEvents extends EventLogMessage
  final case class FetchEventsFrom(from: LocalDateTime) extends EventLogMessage
  final case class FetchEventsAfter(after: LocalDateTime) extends EventLogMessage
  final case class FetchEventsTo(to: LocalDateTime) extends EventLogMessage
  final case class FetchEventsUntil(until: LocalDateTime) extends EventLogMessage
  final case class FetchEventsFromTo(from: LocalDateTime, to: LocalDateTime) extends EventLogMessage
  final case class FetchEventsFromUntil(from: LocalDateTime, until: LocalDateTime) extends EventLogMessage
  final case class FetchEventsAfterTo(after: LocalDateTime, to: LocalDateTime) extends EventLogMessage
  final case class FetchEventsAfterUntil(after: LocalDateTime, until: LocalDateTime) extends EventLogMessage


  sealed trait FetchEventsResponse extends EventLogMessage
  final case class FetchedEvents(events: Enumerator[Event]) extends FetchEventsResponse
  final case class FetchEventsFailed(problem: Problem) extends FetchEventsResponse

  val actorname = "event-log"
  def logicalPath(actorname: String): String = s"/user/almhirt/components/event-logs/$actorname"
}
