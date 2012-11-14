package almhirt.riftwarp

import almhirt.common._

trait Rematerializer {
  type MaterializesFrom <: AnyRef
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
  def materialize(from: MaterializesFrom): AlmValidation[RematerializationArray]
  def materializeRaw(from: AnyRef) = materialize(from.asInstanceOf[MaterializesFrom])
}

trait RematerializesFromMap {
  type MaterializesFrom = Map[String, Any]
  /**
   * Xml, Json, etc
   */
  val channelType = RiftMap
  def materialize(from: MaterializesFrom): AlmValidation[RematerializationArray]
  def materializeRaw(from: AnyRef) = materialize(from.asInstanceOf[MaterializesFrom])
}