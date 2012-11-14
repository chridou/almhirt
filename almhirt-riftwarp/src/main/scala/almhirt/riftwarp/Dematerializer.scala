package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer extends DematerializationFunnel {
  def dematerializeRaw: AnyRef
}

trait Dematerializer[T <: AnyRef] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
  def dematerialize: AlmValidation[T]
  def dematerializeRaw: AnyRef = dematerialize.map(_.asInstanceOf[AnyRef])

}

trait DematerializesToString extends Dematerializer[String] {
  type DematerializesTo = String
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
}

trait DematerializesToByteArray extends Dematerializer[Array[Byte]] {
  type DematerializesTo = Array[Byte]
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
}

trait DematerializesToMap extends Dematerializer[Map[String, Any]] {
  type DematerializesTo = Map[String, Any]
  /**
   * Xml, Json, etc
   */
  val channelType = RiftMap
}
