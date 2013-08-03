package almhirt.corex.slick.eventlog

import java.util.{UUID => JUUID}
import org.joda.time.LocalDateTime

sealed trait EventLogRow {
  type Repr
  def id: JUUID
  def timestamp: java.sql.Timestamp
  def channel: String
  def payload: Repr
}

final case class TextEventLogRow(id: JUUID, timestamp: java.sql.Timestamp, channel: String, payload: String) extends EventLogRow {
  type Repr = String
}

final case class BinaryEventLogRow(id: JUUID, timestamp: java.sql.Timestamp, channel: String, payload: Array[Byte]) extends EventLogRow {
  type Repr = Array[Byte]
}