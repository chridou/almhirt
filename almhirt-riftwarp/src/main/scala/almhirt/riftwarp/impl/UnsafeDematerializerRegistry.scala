package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeDematerializerRegistry extends HasDematerializers {
  private val registry = collection.mutable.HashMap[RiftChannel, collection.mutable.HashMap[String, AnyRef]]()
  
  def addDematerializer[D <: Dematerializer[_], To <: AnyRef](dematerializer: Dematerializer[To])(implicit m: Manifest[To]) {
    val identifier = m.erasure.getName
    if(!registry.contains(dematerializer.channelType)) 
      registry += (dematerializer.channelType -> collection.mutable.HashMap[String, AnyRef]())
    val entry = registry(dematerializer.channelType)
    entry += (identifier -> dematerializer)
  }
  def tryGetDematerializer[To <: AnyRef](forChannel: RiftChannel)(implicit m: Manifest[To]): Option[Dematerializer[To]] = {
    val identifier = m.erasure.getName
    for{
      entry <- registry.get(forChannel)
      dematerializer <- entry.get(identifier)
    } yield (dematerializer.asInstanceOf[Dematerializer[To]])
  }
}