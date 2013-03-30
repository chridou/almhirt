package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }

sealed trait SnapshotRow {
  type Repr
  def id: JUUID
  def version: Long
  def arType: String
  def channel: String
  def payload: Repr
}

final case class TextSnapshotRowRow(id: JUUID, version: Long, arType: String, channel: String, payload: String) extends SnapshotRow {
  type Repr = String
}
final case class BinarySnapshotRowRow(id: JUUID, version: Long, arType: String, channel: String, payload: Array[Byte]) extends SnapshotRow {
  type Repr = Array[Byte]
}

final case class BlobRow(id: JUUID, data: Array[Byte])