package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeRematerializationArrayFactoryRegistry extends HasRematerializationArrayFactories{
  private val registry = collection.mutable.HashMap[RiftChannel, collection.mutable.HashMap[String, AnyRef]]()
  
  def addArrayFactory[R <: RematerializationArrayFactory[_], From <: AnyRef](arrayFactory: RematerializationArrayFactory[From])(implicit m: Manifest[From]) {
    val identifier = m.erasure.getName
    if(!registry.contains(arrayFactory.channelType)) 
      registry += (arrayFactory.channelType -> collection.mutable.HashMap[String, AnyRef]())
    val entry = registry(arrayFactory.channelType)
    entry += (identifier -> arrayFactory)
  }
  def tryGetArrayFactory[From <: AnyRef](forChannel: RiftChannel)(implicit m: Manifest[From]): Option[RematerializationArrayFactory[From]] = {
    val identifier = m.erasure.getName
    for{
      entry <- registry.get(forChannel)
      dematerializer <- entry.get(identifier)
    } yield (dematerializer.asInstanceOf[RematerializationArrayFactory[From]])
  }
}