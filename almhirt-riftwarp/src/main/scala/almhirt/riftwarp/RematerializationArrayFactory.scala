package almhirt.riftwarp

import almhirt.common._

trait RematerializationArrayFactory[From <: AnyRef] {
  /**
   * Xml, Json, etc
   */
  def descriptor: RiftFullDescriptor
  def createRematerializationArray(from: From)(implicit hasRecomposers: HasRecomposers): AlmValidation[RematerializationArray]
  def createRematerializationArrayRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers) = createRematerializationArray(from.asInstanceOf[From])
}

trait FromMapRematerializationArrayFactory extends RematerializationArrayFactory[Map[String, Any]] {
}