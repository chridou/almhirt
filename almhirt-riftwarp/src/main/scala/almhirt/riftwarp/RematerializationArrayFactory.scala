package almhirt.riftwarp

import almhirt.common._

trait RematerializationArrayFactory[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannel] {
  /**
   * Xml, Json, etc
   */
  def toolGroup: ToolGroup
  def createRematerializationArray(from: TDimension)(implicit hasRecomposers: HasRecomposers, hasRematerializersForHKTs: HasRematerializersForHKTs): AlmValidation[RematerializationArray]
  def createRematerializationArrayRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers, hasRematerializersForHKTs: HasRematerializersForHKTs) = createRematerializationArray(from.asInstanceOf[TDimension])
}
