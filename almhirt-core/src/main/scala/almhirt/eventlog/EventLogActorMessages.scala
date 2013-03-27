package almhirt.eventlog

import java.util.UUID
import org.joda.time.DateTime
import almhirt.common._
import almhirt.core.Event

sealed trait EventLogMessage

sealed trait EventLogCmd extends EventLogMessage
final case class LogEventQry(event: Event, correlationId: Option[UUID] = None) extends EventLogCmd
final case class GetAllEventsQry(chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
final case class GetEventQry(eventId: UUID, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
final case class GetEventsFromQry(from: DateTime, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
final case class GetEventsUntilQry(until: DateTime, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
final case class GetEventsFromUntilQry(from: DateTime, until: DateTime, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd

sealed trait EventLogRsp extends EventLogMessage
final case class EventsRsp(chunk: EventsChunk, correlationId: Option[UUID]) extends EventLogRsp
final case class EventRsp(result: AlmValidation[Event], correlationId: Option[UUID]) extends EventLogRsp
final case class LoggedEventRsp(result: AlmValidation[Event], correlationId: Option[UUID]) extends EventLogRsp
