package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.BasicEventHeader
import riftwarp._

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