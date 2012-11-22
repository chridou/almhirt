package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer extends DematerializationFunnel {
  def descriptor: RiftFullDescriptor
  def dematerializeRaw: AlmValidation[AnyRef]
}

trait Dematerializer[T <: RiftDimension] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def dematerialize: AlmValidation[T]
  def dematerializeRaw: AlmValidation[AnyRef] = dematerialize.map(_.asInstanceOf[AnyRef])

}

trait DematerializesToString extends Dematerializer[DimensionString] {
}

trait DematerializesToCord extends Dematerializer[DimensionCord] {
}

trait DematerializesToByteArray extends Dematerializer[DimensionBinary] {
}

trait DematerializesToRawMap extends Dematerializer[DimensionRawMap] {
}
