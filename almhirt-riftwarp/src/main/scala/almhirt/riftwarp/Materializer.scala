package almhirt.riftwarp

import almhirt.common._

trait Materializer {
  type MaterializesFrom <: AnyRef
  /**
   * Xml, Json, etc
   */
  def channelType: String
  def materialize(from: MaterializesFrom): AlmValidation[RematerializationArray]
  def materializeRaw(from: AnyRef) = materialize(from.asInstanceOf[MaterializesFrom])
}

trait MaterializesFromMap {
  type MaterializesFrom = Map[String, Any]
  /**
   * Xml, Json, etc
   */
  def channelType: String
  def materialize(from: MaterializesFrom): AlmValidation[RematerializationArray]
  def materializeRaw(from: AnyRef) = materialize(from.asInstanceOf[MaterializesFrom])
}