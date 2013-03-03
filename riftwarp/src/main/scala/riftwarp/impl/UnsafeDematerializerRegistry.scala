package riftwarp.impl

import riftwarp._
import riftwarp.components._

class UnsafeWarpSequencerRegistry extends HasWarpSequencers {
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[RiftChannel, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[RiftChannel, collection.mutable.HashMap[String, AnyRef]]()

  def addWarpSequencerFactory(factory: WarpSequencerFactory[_ <: RiftDimension], asChannelDefault: Boolean) {
    val dimensionIdent = factory.tDimension.getName()

    if (!toolregistry.contains(factory.toolGroup))
      toolregistry += (factory.toolGroup -> HashMap[RiftChannel, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(factory.toolGroup)
    if (!tooltypeentry.contains(factory.channel))
      tooltypeentry += (factory.channel -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(factory.channel)
    channelEntry += (dimensionIdent -> factory.asInstanceOf[AnyRef])

    if (!channelregistry.contains(factory.channel))
      channelregistry += (factory.channel -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(factory.channel)
    if (asChannelDefault || !channeltypeentry.contains(dimensionIdent))
      channeltypeentry += (dimensionIdent -> factory)
  }

  def tryGetWarpSequencerFactoryByType(tDimemsion: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = {
    val dimensionIdent = tDimemsion.getName
    (toolGroup match {
      case None =>
        for {
          entry <- channelregistry.get(channel)
          dematerializer <- entry.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[WarpSequencerFactory[RiftDimension]]
      case Some(toolGroup) =>
        for {
          toolentries <- toolregistry.get(toolGroup)
          channelEntries <- toolentries.get(channel)
          dematerializer <- channelEntries.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[WarpSequencerFactory[RiftDimension]]
    })
  }
}