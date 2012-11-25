package almhirt.riftwarp

import almhirt.common._

trait RematerializationArrayFactory[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor] {
  /**
   * Xml, Json, etc
   */
  def descriptor: RiftFullDescriptor
  def createRematerializationArray(from: TDimension)(implicit hasRecomposers: HasRecomposers, hasRematerializersForHKTs: HasRematerializersForHKTs): AlmValidation[RematerializationArray]
  def createRematerializationArrayRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers, hasRematerializersForHKTs: HasRematerializersForHKTs) = createRematerializationArray(from.asInstanceOf[TDimension])
}
