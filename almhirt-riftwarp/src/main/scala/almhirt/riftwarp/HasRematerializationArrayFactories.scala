package almhirt.riftwarp

trait HasRematerializationArrayFactories {
  def addArrayFactory[R <: RematerializationArrayFactory[TDimension, TChannel], TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](arrayFactory: RematerializationArrayFactory[TDimension, TChannel], isChannelDefault: Boolean = false)(implicit mD: Manifest[TDimension], mC: Manifest[TChannel]): Unit
  def addArrayFactoryAsDefault[R <: RematerializationArrayFactory[TDimension, TChannel], TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](arrayFactory: RematerializationArrayFactory[TDimension, TChannel])(implicit mD: Manifest[TDimension], mC: Manifest[TChannel]) = addArrayFactory(arrayFactory, true)
  def tryGetArrayFactory[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](implicit mD: Manifest[TDimension], mC: Manifest[TChannel]): Option[RematerializationArrayFactory[TDimension, TChannel]]
}