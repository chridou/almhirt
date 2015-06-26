package almhirt.snapshots

import almhirt.common._
import almhirt.aggregates.AggregateRoot

trait SnapshotMarshaller[T] {
  def marshal(what: AggregateRoot): AlmValidation[T]
  def unmarshal(from: T): AlmValidation[AggregateRoot]
}
