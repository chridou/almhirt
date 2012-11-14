package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait Dematerializer extends DematerializationFunnel {
  type DematerializesTo <: AnyRef
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
  def dematerialize: AlmValidation[DematerializesTo]
  def dematerializeRaw: AnyRef = dematerialize.map(_.asInstanceOf[AnyRef])
}

trait DematerializesToString extends Dematerializer {
  type DematerializesTo = String
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
  def dematerialize: AlmValidation[DematerializesTo]
}

trait DematerializesToByteArray extends Dematerializer {
  type DematerializesTo = Array[Byte]
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
  def dematerialize: AlmValidation[DematerializesTo]
}

trait DematerializesToMap extends Dematerializer {
  type DematerializesTo = Map[String, Any]
  /**
   * Xml, Json, etc
   */
  val channelType = RiftMap
  def dematerialize: AlmValidation[DematerializesTo]
}
