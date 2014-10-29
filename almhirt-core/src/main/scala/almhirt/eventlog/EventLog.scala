package almhirt.eventlog

import java.util.{ UUID ⇒ JUUID }
import org.joda.time.LocalDateTime
import akka.actor._
import almhirt.common._
import play.api.libs.iteratee.Enumerator

object EventLog {
  sealed trait EventLogMessage

  final case class LogEvent(event: Event, acknowledge: Boolean) extends EventLogMessage
  sealed trait LogEventResponse extends EventLogMessage { def eventId: EventId }
  final case class EventLogged(eventId: EventId) extends LogEventResponse
  final case class EventNotLogged(eventId: EventId, problem: Problem) extends LogEventResponse

  final case class FindEvent(eventId: EventId) extends EventLogMessage
  sealed trait FindEventResponse extends EventLogMessage
  final case class FoundEvent(eventId: EventId, event: Option[Event]) extends FindEventResponse
  final case class FindEventFailed(eventId: EventId, problem: Problem) extends FindEventResponse

  final case class FetchEvents(range: LocalDateTimeRange, traverse: TraverseWindow) extends EventLogMessage
  import LocalDateTimeRange._
  object FetchEventsFrom {
    def apply(from: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(From(from).endless, traverse)
  }

  object FetchEventsParts {
    def unapply(what: EventLogMessage): Option[(LocalDateTimeRange.RangeStart, LocalDateTimeRange.RangeEnd, TraverseWindow.LowerBound, TraverseWindow.Length)] =
      what match {
        case FetchEvents(LocalDateTimeRange(a, b), TraverseWindow(c, d)) ⇒ Some((a, b, c, d))
        case _ ⇒ None
      }
  }

  object FetchEventsAfter {
    def apply(after: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(After(after).endless, traverse)
  }
  object FetchEventsTo {
    def apply(to: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(BeginningOfTime.to(to), traverse)
  }
  object FetchEventsUntil {
    def apply(until: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(BeginningOfTime.until(until), traverse)
  }
  object FetchEventsFromTo {
    def apply(from: LocalDateTime, to: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(From(from).to(to), traverse)
  }
  object FetchEventsFromUntil {
    def apply(from: LocalDateTime, until: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(From(from).until(until), traverse)
  }
  object FetchEventsAfterTo {
    def apply(after: LocalDateTime, to: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(After(after).to(to), traverse)
  }
  object FetchEventsAfterUntil {
    def apply(after: LocalDateTime, until: LocalDateTime, traverse: TraverseWindow): FetchEvents =
      FetchEvents(After(after).until(until), traverse)
  }

  sealed trait FetchEventsResponse extends EventLogMessage
  final case class FetchedEvents(events: Enumerator[Event]) extends FetchEventsResponse
  final case class FetchEventsFailed(problem: Problem) extends FetchEventsResponse

  val actorname = "event-log"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.eventLogs(root) / actorname 
}
