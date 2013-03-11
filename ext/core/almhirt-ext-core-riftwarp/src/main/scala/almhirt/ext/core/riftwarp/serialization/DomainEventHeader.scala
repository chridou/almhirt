package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.{ DomainEventHeader, AggregateRootRef }
import riftwarp._

object DomainEventHeaderDecomposer extends Decomposer[DomainEventHeader] {
  val riftDescriptor = RiftDescriptor(classOf[DomainEventHeader])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: DomainEventHeader, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addUuid("id", what.id)
      .addUuid("aggId", what.aggRef.id)
      .addLong("aggVersion", what.aggRef.version)
      .addDateTime("timestamp", what.timestamp).ok
  }
}

object DomainEventHeaderRecomposer extends Recomposer[DomainEventHeader] {
  val riftDescriptor = RiftDescriptor(classOf[DomainEventHeader])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[DomainEventHeader] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| aggVersion |@| timestamp)((id, aggId, aggVersion, timestamp) =>
      DomainEventHeader(id, AggregateRootRef(aggId, aggVersion), timestamp))
  }
}
