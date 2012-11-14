package almhirt.riftwarp

trait HasRematerializationArrayFactories {
  def addArrayFactory[R <: RematerializationArrayFactory[_], From <: AnyRef](arrayFactory: RematerializationArrayFactory[From])(implicit m: Manifest[From])
  def tryGetArrayFactory[From <: AnyRef](forChannel: RiftChannel)(implicit m: Manifest[From]): Option[RematerializationArrayFactory[From]]
}