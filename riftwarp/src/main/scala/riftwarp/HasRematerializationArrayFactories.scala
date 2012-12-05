package riftwarp

trait HasRematerializationArrayFactories {
  def addArrayFactory(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension], isChannelDefault: Boolean = false): Unit
  def addArrayFactoryAsDefault(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension]) = addArrayFactory(arrayFactory, true)
  def tryGetArrayFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None): Option[RematerializationArrayFactory[RiftDimension]]
  def tryGetArrayFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]) =
    tryGetArrayFactoryByType(mD.erasure.asInstanceOf[Class[_ <: RiftDimension]])(channel, toolGroup)
}