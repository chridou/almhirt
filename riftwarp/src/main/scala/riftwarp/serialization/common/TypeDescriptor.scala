package riftwarp.serialization.common

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

object RiftDescriptorDecomposer extends Decomposer[RiftDescriptor] {
  val riftDescriptor = RiftDescriptor(classOf[RiftDescriptor])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: RiftDescriptor, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addString("identifier", what.identifier).addOptionalInt("version", what.version).ok
  }
}

object RiftDescriptorRecomposer extends Recomposer[RiftDescriptor] {
  val riftDescriptor = RiftDescriptor(classOf[RiftDescriptor])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[RiftDescriptor] = {
    val identifier = from.getString("identifier").toAgg
    val version = from.tryGetInt("version").toAgg
    (identifier |@| version)(RiftDescriptor.apply)
  }
}