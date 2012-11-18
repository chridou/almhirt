package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer extends DematerializationFunnel {
  def descriptor: RiftFullDescriptor
  def dematerializeRaw: AlmValidation[AnyRef]
}

trait Dematerializer[T <: AnyRef] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def dematerialize: AlmValidation[T]
  def dematerializeRaw: AlmValidation[AnyRef] = dematerialize.map(_.asInstanceOf[AnyRef])

}

trait DematerializesToString extends Dematerializer[String] {
}

trait DematerializesToCord extends Dematerializer[scalaz.Cord] {
}

trait DematerializesToByteArray extends Dematerializer[Array[Byte]] {
}

trait DematerializesToMap extends Dematerializer[Map[String, Any]] {
}
