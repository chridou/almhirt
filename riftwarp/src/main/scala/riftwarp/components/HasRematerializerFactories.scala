package riftwarp.components

import riftwarp._

trait HasRematerializerFactories {
  def addRematerializerFactory(arrayFactory: RematerializerFactory[_ <: RiftDimension], isChannelDefault: Boolean = false): Unit
  def addRematerializerFactoryAsDefault(arrayFactory: RematerializerFactory[_ <: RiftDimension]) = addRematerializerFactory(arrayFactory, true)
  def tryGetRematerializerFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None): Option[RematerializerFactory[RiftDimension]]
  def tryGetRematerializerFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]) =
    tryGetRematerializerFactoryByType(mD.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]])(channel, toolGroup)
}