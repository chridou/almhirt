package riftwarp

import almhirt.common._

trait RematerializationArrayFactory[TDimension <: RiftDimension] {
  /**
   * Xml, Json, etc
   */
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createRematerializationArray(from: TDimension)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[RematerializationArray]
  def createRematerializationArrayRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects) = createRematerializationArray(from.asInstanceOf[TDimension])
}
