package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }

sealed trait SnapshotRow {
  type Repr
  def arId: JUUID
  def arVersion: Long
  def arType: String
  def channel: String
  def payload: Repr
}

final case class TextSnapshotRow(arId: JUUID, arVersion: Long, arType: String, channel: String, payload: String) extends SnapshotRow {
  type Repr = String
}
final case class BinarySnapshotRow(arId: JUUID, arVersion: Long, arType: String, channel: String, payload: Array[Byte]) extends SnapshotRow {
  type Repr = Array[Byte]
}

