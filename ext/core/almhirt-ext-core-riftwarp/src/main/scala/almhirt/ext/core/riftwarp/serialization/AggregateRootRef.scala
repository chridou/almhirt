package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._

class AggregateRootRefDecomposer extends Decomposer[AggregateRootRef] {
  val riftDescriptor = RiftDescriptor(classOf[AggregateRootRef], 1)
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: AggregateRootRef)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addLong("version", what.version))
  }
}

class AggregateRootRefRecomposer extends Recomposer[AggregateRootRef] {
  val riftDescriptor = RiftDescriptor(classOf[AggregateRootRef], 1)
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[AggregateRootRef] = {
    val id = from.getUuid("id").toAgg
    val version = from.getLong("version").toAgg
    (id |@| version)(AggregateRootRef.apply)
  }
}