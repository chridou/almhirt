package almhirt.riftwarp

import almhirt.common._

trait RematerializationArrayFactory[From <: AnyRef] {
  /**
   * Xml, Json, etc
   */
  def channelType: RiftChannel
  def createRematerializationArray(from: From)(implicit hasRecomposers: HasRecomposers): RematerializationArray
  def createRematerializationArrayRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers) = createRematerializationArray(from.asInstanceOf[From])
}

trait FromMapRematerializationArrayFactory extends RematerializationArrayFactory[Map[String, Any]] {
  /**
   * Xml, Json, etc
   */
  val channelType = RiftMap
  def createRematerializationArray(from: Map[String, Any])(implicit hasRecomposers: HasRecomposers): RematerializationArray
}