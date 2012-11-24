package almhirt.riftwarp

import almhirt.common._

trait RematerializationArrayFactory[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor] {
  /**
   * Xml, Json, etc
   */
  def descriptor: RiftFullDescriptor
  def createRematerializationArray(from: TDimension)(implicit hasRecomposers: HasRecomposers): AlmValidation[RematerializationArray[TDimension, TChannel]]
  def createRematerializationArrayRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers) = createRematerializationArray(from.asInstanceOf[TDimension])
}
