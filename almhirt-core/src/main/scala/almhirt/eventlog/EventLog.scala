package almhirt.eventlog

import java.util.{ UUID => JUUID }
import org.joda.time.LocalDateTime
import akka.actor._
import almhirt.common._

object EventLog {
  sealed trait EventLogMessage

  final case class StoreEvent(event: Event) extends EventLogMessage

  final case class GetEvent(eventId: JUUID) extends EventLogMessage
  final case class GetEventsFrom(from: LocalDateTime) extends EventLogMessage
  final case class GetEventsAfter(after: LocalDateTime) extends EventLogMessage
  final case class GetEventsTo(to: LocalDateTime) extends EventLogMessage
  final case class GetEventsUntil(until: LocalDateTime) extends EventLogMessage
  final case class GetEventsFromTo(from: LocalDateTime, to: LocalDateTime) extends EventLogMessage
  final case class GetEventsFromUntil(from: LocalDateTime, until: LocalDateTime) extends EventLogMessage
  final case class GetEventsAfterTo(after: LocalDateTime, to: LocalDateTime) extends EventLogMessage
  final case class GetEventsAfterUntil(after: LocalDateTime, until: LocalDateTime) extends EventLogMessage

  sealed trait SingleEventQueryResult extends EventLogMessage
  final case class QueriedEvent(eventId: JUUID, event: Option[Event]) extends SingleEventQueryResult
  final case class EventQueryFailed(eventId: JUUID, problem: Problem) extends SingleEventQueryResult

  sealed trait FetchedEventsPart extends EventLogMessage {
    def index: Int
    def isLast: Boolean
  }

  final case class EventsChunk(
    /**
     * Starts with Zero
     */
    index: Int,
    isLast: Boolean,
    events: Seq[Event]) extends FetchedEventsPart

  final case class DomainEventsChunkFailure(
    /**
     * Starts with Zero
     */
    index: Int,
    problem: Problem) extends FetchedEventsPart {
    override def isLast = true
  }
}

trait EventLog { actor: Actor =>
  protected def receiveEventLogMsg: Receive
}