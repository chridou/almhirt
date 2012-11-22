package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeDematerializerRegistry extends HasDematerializers {
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[RiftChannelDescriptor, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[RiftChannelDescriptor, collection.mutable.HashMap[String, AnyRef]]()

  def addDematerializer[D <: Dematerializer[_], To <: RiftTypedDimension[_]](dematerializer: Dematerializer[To], asChannelDefault: Boolean)(implicit m: Manifest[To]) {
    val identifier = m.erasure.getName

    if (!toolregistry.contains(dematerializer.descriptor.toolGroup))
      toolregistry += (dematerializer.descriptor.toolGroup -> HashMap[RiftChannelDescriptor, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(dematerializer.descriptor.toolGroup)
    if (!tooltypeentry.contains(dematerializer.descriptor.channelType))
      tooltypeentry += (dematerializer.descriptor.channelType -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(dematerializer.descriptor.channelType)
    channelEntry += (identifier -> dematerializer.asInstanceOf[AnyRef])

    if (!channelregistry.contains(dematerializer.descriptor.channelType))
      channelregistry += (dematerializer.descriptor.channelType -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(dematerializer.descriptor.channelType)
    if(asChannelDefault || !channeltypeentry.contains(identifier))
      channeltypeentry += (identifier -> dematerializer)
  }

  def tryGetDematerializer[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[To]] = {
    val identifier = m.erasure.getName
    (warpType match {
      case ct: RiftChannelDescriptor =>
        for {
          entry <- channelregistry.get(ct)
          dematerializer <- entry.get(identifier)
        } yield dematerializer.asInstanceOf[Dematerializer[To]]
      case fd: RiftFullDescriptor =>
        for {
          toolentries <- toolregistry.get(fd.toolGroup)
          channelEntries <- toolentries.get(fd.channelType)
          dematerializer <- channelEntries.get(identifier)
        } yield dematerializer.asInstanceOf[Dematerializer[To]]
    })
  }
}