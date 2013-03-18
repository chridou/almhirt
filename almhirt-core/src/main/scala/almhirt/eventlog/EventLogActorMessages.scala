package almhirt.eventlog

import java.util.UUID
import org.joda.time.DateTime
import almhirt.common._
import almhirt.core.Event

sealed trait EventLogCmd
case class LogEvent(event: Event, correlationId: Option[UUID] = None) extends EventLogCmd
case class GetAllEventsQry(chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
case class GetEventQry(eventId: UUID, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
case class GetEventsFromQry(from: DateTime, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
case class GetEventsUntilQry(until: DateTime, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd
case class GetEventsFromUntilQry(from: DateTime, until: DateTime, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends EventLogCmd

sealed trait EventLogRsp
case class EventsRsp(chunk: EventsChunk, correlationId: Option[UUID]) extends EventLogRsp
case class LoggedDomainEventRsp(result: AlmValidation[Event], correlationId: Option[UUID]) extends EventLogRsp
