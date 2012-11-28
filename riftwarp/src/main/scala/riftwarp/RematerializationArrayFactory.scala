package riftwarp

import almhirt.common._

trait RematerializationArrayFactory[TDimension <: RiftDimension] {
  /**
   * Xml, Json, etc
   */
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createRematerializationArray(from: TDimension)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[RematerializationArray]
  def createRematerializationArrayRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories) = createRematerializationArray(from.asInstanceOf[TDimension])
}
