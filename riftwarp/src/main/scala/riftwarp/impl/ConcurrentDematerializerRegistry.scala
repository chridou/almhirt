package riftwarp.impl

import _root_.java.util.concurrent.ConcurrentHashMap
import riftwarp._
import riftwarp.components._

class ConcurrentWarpSequencerRegistry extends HasWarpSequencers {
  private val toolregistry = new ConcurrentHashMap[ToolGroup, ConcurrentHashMap[RiftChannel, ConcurrentHashMap[String, AnyRef]]](16)
  private val channelregistry = new ConcurrentHashMap[RiftChannel, ConcurrentHashMap[String, AnyRef]](16)

  def addWarpSequencerFactory(factory: WarpSequencerFactory[_ <: RiftDimension], asChannelDefault: Boolean) {
    synchronized {
      val dimensionIdent = factory.tDimension.getName()

      if (!toolregistry.containsKey(factory.toolGroup))
        toolregistry.put(factory.toolGroup, new ConcurrentHashMap[RiftChannel, ConcurrentHashMap[String, AnyRef]](16))
      val tooltypeentry = toolregistry.get(factory.toolGroup)
      if (!tooltypeentry.containsKey(factory.channel))
        tooltypeentry.put(factory.channel, new ConcurrentHashMap[String, AnyRef](16))
      val channelEntry = tooltypeentry.get(factory.channel)
      channelEntry.put(dimensionIdent, factory.asInstanceOf[AnyRef])

      if (!channelregistry.containsKey(factory.channel))
        channelregistry.put(factory.channel, new ConcurrentHashMap[String, AnyRef](32))
      val channeltypeentry = channelregistry.get(factory.channel)
      if (asChannelDefault || !channeltypeentry.contains(dimensionIdent))
        channeltypeentry.put(dimensionIdent, factory)
    }
  }

  def tryGetWarpSequencerFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = {
    val dimensionIdent = tDimension.getName
    toolGroup match {
      case None =>
        channelregistry.get(channel) match {
          case null => None
          case entry => entry.get(dimensionIdent) match {
            case null => None
            case x => Some(x.asInstanceOf[WarpSequencerFactory[RiftDimension]])
          }
        }
      case Some(toolGroup) =>
        toolregistry.get(toolGroup) match {
          case null => None
          case toolentries => toolentries.get(channel) match {
            case null => None
            case channelEntries => channelEntries.get(dimensionIdent) match {
              case null => None
              case x => Some(x.asInstanceOf[WarpSequencerFactory[RiftDimension]])
            }
          }
        }
    }
  }
}