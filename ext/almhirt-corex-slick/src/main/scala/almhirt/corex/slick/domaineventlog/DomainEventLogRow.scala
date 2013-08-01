package almhirt.corex.slick.domaineventlog

import java.util.{UUID => JUUID}

sealed trait DomainEventLogRow {
  type Repr
  def id: JUUID
  def aggId: JUUID
  def aggVersion: Long
  def channel: String
  def payload: Repr
}
final case class TextDomainEventLogRow(id: JUUID, aggId: JUUID, aggVersion: Long, channel: String, payload: String) extends DomainEventLogRow {
  type Repr = String
}
final case class BinaryDomainEventLogRow(id: JUUID, aggId: JUUID, aggVersion: Long, channel: String, payload: Array[Byte]) extends DomainEventLogRow {
  type Repr = Array[Byte]
}
