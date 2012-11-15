package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer extends DematerializationFunnel {
  def dematerializeRaw: AlmValidation[AnyRef]
}

trait Dematerializer[T <: AnyRef] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
  def dematerialize: AlmValidation[T]
  def dematerializeRaw: AlmValidation[AnyRef] = dematerialize.map(_.asInstanceOf[AnyRef])

}

trait DematerializesToString extends Dematerializer[String] {
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
}

trait DematerializesToCord extends Dematerializer[scalaz.Cord] {
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
}

trait DematerializesToByteArray extends Dematerializer[Array[Byte]] {
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
}

trait DematerializesToMap extends Dematerializer[Map[String, Any]] {
  /**
   * Xml, Json, etc
   */
  val channelType = RiftMap
}
