package almhirt.riftwarp

trait HasRematerializationArrayFactories {
  def addArrayFactory[R <: RematerializationArrayFactory[_], From <: AnyRef](arrayFactory: RematerializationArrayFactory[From], asChannelDefault: Boolean = false)(implicit m: Manifest[From])
  def addArrayFactoryAsDefault[R <: RematerializationArrayFactory[_], From <: AnyRef](arrayFactory: RematerializationArrayFactory[From])(implicit m: Manifest[From]) = addArrayFactory(arrayFactory, true)
  def tryGetArrayFactory[From <: AnyRef](warpType: RiftDescriptor)(implicit m: Manifest[From]): Option[RematerializationArrayFactory[From]]
}