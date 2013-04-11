package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.{ EventHeader, BasicEventHeader }
import riftwarp._
import almhirt.domain.{ DomainEventHeader, AggregateRootRef }

object BasicEventHeaderDecomposer extends Decomposer[BasicEventHeader] {
  val riftDescriptor = RiftDescriptor(classOf[BasicEventHeader])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BasicEventHeader, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addUuid("id", what.id)
      .addDateTime("timestamp", what.timestamp).ok
  }
}

object BasicEventHeaderRecomposer extends Recomposer[BasicEventHeader] {
  val riftDescriptor = RiftDescriptor(classOf[BasicEventHeader])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[BasicEventHeader] = {
    val id = from.getUuid("id").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| timestamp)(BasicEventHeader.apply)
  }
}

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

object EventHeaderDecomposer extends Decomposer[EventHeader] {
  val riftDescriptor = RiftDescriptor(classOf[EventHeader])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: EventHeader, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case header: BasicEventHeader => into.includeDirect(header, BasicEventHeaderDecomposer)
      case header: DomainEventHeader => into.includeDirect(header, DomainEventHeaderDecomposer)
    }
  }
}

object EventHeaderRecomposer extends DivertingRecomposer[EventHeader] {
  val riftDescriptor = RiftDescriptor(classOf[EventHeader])
  val alternativeRiftDescriptors = Nil
  val recomposers = Map(
    BasicEventHeaderRecomposer.riftDescriptor -> BasicEventHeaderRecomposer,
    DomainEventHeaderRecomposer.riftDescriptor -> DomainEventHeaderRecomposer)
  val divert = recomposers.lift
}
