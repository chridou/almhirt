package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.commanding._
import riftwarp._

object AggregateRootRefDecomposer extends Decomposer[AggregateRootRef] {
  val riftDescriptor = RiftDescriptor(classOf[AggregateRootRef])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: AggregateRootRef, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addUuid("id", what.id)
      .addLong("version", what.version).ok
  }
}

object AggregateRootRefRecomposer extends Recomposer[AggregateRootRef] {
  val riftDescriptor = RiftDescriptor(classOf[AggregateRootRef])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[AggregateRootRef] = {
    val id = from.getUuid("id").toAgg
    val version = from.getLong("version").toAgg
    (id |@| version)(AggregateRootRef.apply)
  }
}