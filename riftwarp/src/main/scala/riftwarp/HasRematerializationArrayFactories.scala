package riftwarp

trait HasRematerializationArrayFactories {
  def addArrayFactory(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension], isChannelDefault: Boolean = false): Unit
  def addArrayFactoryAsDefault(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension]) = addArrayFactory(arrayFactory, true)
  def tryGetArrayFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]): Option[RematerializationArrayFactory[TDimension]]
}