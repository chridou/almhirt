package riftwarp.impl

import riftwarp._

class UnsafeRematerializationArrayFactoryRegistry extends HasRematerializationArrayFactories {
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[RiftChannel, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[RiftChannel, collection.mutable.HashMap[String, AnyRef]]()

  def addArrayFactory(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension], isChannelDefault: Boolean = false) {
    val identDim = arrayFactory.tDimension.getName()
 
    if (!toolregistry.contains(arrayFactory.toolGroup))
      toolregistry += (arrayFactory.toolGroup -> HashMap[RiftChannel, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(arrayFactory.toolGroup)
    if (!tooltypeentry.contains(arrayFactory.channel))
      tooltypeentry += (arrayFactory.channel -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(arrayFactory.channel)
    channelEntry += (identDim -> arrayFactory.asInstanceOf[AnyRef])

    if (!channelregistry.contains(arrayFactory.channel))
      channelregistry += (arrayFactory.channel -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(arrayFactory.channel)
    if (isChannelDefault || !channeltypeentry.contains(identDim))
      channeltypeentry += (identDim -> arrayFactory)
  }

  def tryGetArrayFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None): Option[RematerializationArrayFactory[RiftDimension]] = {
    val dimensionIdent = tDimension.getName
    (toolGroup match {
      case None =>
        for {
          entry <- channelregistry.get(channel)
          dematerializer <- entry.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[RematerializationArrayFactory[RiftDimension]]
      case Some(toolGroup) =>
        for {
          toolentries <- toolregistry.get(toolGroup)
          channelEntries <- toolentries.get(channel)
          dematerializer <- channelEntries.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[RematerializationArrayFactory[RiftDimension]]
    })
  }
}