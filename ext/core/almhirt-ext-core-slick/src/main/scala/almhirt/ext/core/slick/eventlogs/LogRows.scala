package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime

sealed trait EventLogRow {
  type Repr
}
final case class TextEventLogRow(id: JUUID, timestamp: DateTime, eventtype: String, channel: String, payload: String) extends EventLogRow {
  type Repr = String
}
final case class BinaryEventLogRow(id: JUUID, timestamp: DateTime, eventtype: String, channel: String, payload: Array[Byte]) extends EventLogRow {
  type Repr = Array[Byte]
}

sealed trait DomainEventLogRow {
  type Repr
}
final case class TextDomainEventLogRow(id: JUUID, aggId: JUUID, aggVersion: Long, timestamp: DateTime, eventtype: String, channel: String, payload: String) extends DomainEventLogRow {
  type Repr = String
}
final case class BinaryDomainEventLogRow(id: JUUID, aggId: JUUID, aggVersion: Long, timestamp: DateTime, eventtype: String, channel: String, payload: Array[Byte]) extends DomainEventLogRow {
  type Repr = Array[Byte]
}

final case class BlobRow(id: JUUID, data: Array[Byte])