package almhirt.core.serialization

import scalaz._, Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.riftwarp._
import almhirt.commanding._

class AggregateRootRefDecomposer extends Decomposer[AggregateRootRef] {
  val typeDescriptor = TypeDescriptor(classOf[AggregateRootRef], 1)
  def decompose[TDimension <: RiftDimension](what: AggregateRootRef)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addLong("version", what.version))
  }
}

class AggregateRootRefRecomposer extends Recomposer[AggregateRootRef] {
  val typeDescriptor = TypeDescriptor(classOf[AggregateRootRef], 1)
  def recompose(from: RematerializationArray): AlmValidation[AggregateRootRef] = {
    val id = from.getUuid("id").toAgg
    val version = from.getLong("version").toAgg
    (id |@| version)(AggregateRootRef.apply)
  }
}