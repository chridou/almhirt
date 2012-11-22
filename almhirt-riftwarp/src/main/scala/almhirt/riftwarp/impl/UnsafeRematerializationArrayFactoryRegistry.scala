package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeRematerializationArrayFactoryRegistry extends HasRematerializationArrayFactories{
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[RiftChannelDescriptor, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[RiftChannelDescriptor, collection.mutable.HashMap[String, AnyRef]]()

  def addArrayFactory[D <: RematerializationArrayFactory[_], From <: RiftTypedDimension[_]](rematArray: RematerializationArrayFactory[From], asChannelDefault: Boolean)(implicit m: Manifest[From]) {
    val identifier = m.erasure.getName

    if (!toolregistry.contains(rematArray.descriptor.toolGroup))
      toolregistry += (rematArray.descriptor.toolGroup -> HashMap[RiftChannelDescriptor, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(rematArray.descriptor.toolGroup)
    if (!tooltypeentry.contains(rematArray.descriptor.channelType))
      tooltypeentry += (rematArray.descriptor.channelType -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(rematArray.descriptor.channelType)
    channelEntry += (identifier -> rematArray.asInstanceOf[AnyRef])

    if (!channelregistry.contains(rematArray.descriptor.channelType))
      channelregistry += (rematArray.descriptor.channelType -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(rematArray.descriptor.channelType)
    if(asChannelDefault || !channeltypeentry.contains(identifier))
      channeltypeentry += (identifier -> rematArray)
  }

  def tryGetArrayFactory[From <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[From]): Option[RematerializationArrayFactory[From]] = {
    val identifier = m.erasure.getName
    (warpType match {
      case ct: RiftChannelDescriptor =>
        for {
          entry <- channelregistry.get(ct)
          dematerializer <- entry.get(identifier)
        } yield dematerializer.asInstanceOf[RematerializationArrayFactory[From]]
      case fd: RiftFullDescriptor =>
        for {
          toolentries <- toolregistry.get(fd.toolGroup)
          channelEntries <- toolentries.get(fd.channelType)
          dematerializer <- channelEntries.get(identifier)
        } yield dematerializer.asInstanceOf[RematerializationArrayFactory[From]]
    })
  }
}