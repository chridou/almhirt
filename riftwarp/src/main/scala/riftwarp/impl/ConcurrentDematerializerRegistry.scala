package riftwarp.impl

import _root_.java.util.concurrent.ConcurrentHashMap
import riftwarp._

class ConcurrentDematerializerRegistry extends HasDematerializers {
  private val toolregistry = new ConcurrentHashMap[ToolGroup, ConcurrentHashMap[RiftChannel, ConcurrentHashMap[String, AnyRef]]](16)
  private val channelregistry = new ConcurrentHashMap[RiftChannel, ConcurrentHashMap[String, AnyRef]](16)

  def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean) {
    synchronized {
      val dimensionIdent = factory.tDimension.getName()

      if (!toolregistry.contains(factory.toolGroup))
        toolregistry.put(factory.toolGroup, new ConcurrentHashMap[RiftChannel, ConcurrentHashMap[String, AnyRef]](16))
      val tooltypeentry = toolregistry.get(factory.toolGroup)
      if (!tooltypeentry.contains(factory.channel))
        tooltypeentry.put(factory.channel, new ConcurrentHashMap[String, AnyRef](16))
      val channelEntry = tooltypeentry.get(factory.channel)
      channelEntry.put(dimensionIdent, factory.asInstanceOf[AnyRef])

      if (!channelregistry.contains(factory.channel))
        channelregistry.put(factory.channel, new ConcurrentHashMap[String, AnyRef](32))
      val channeltypeentry = channelregistry.get(factory.channel)
      if (asChannelDefault || !channeltypeentry.contains(dimensionIdent))
        channeltypeentry.put(dimensionIdent, factory)
    }
  }

  def tryGetDematerializerFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = {
    val dimensionIdent = tDimension.getName
    toolGroup match {
      case None =>
        channelregistry.get(channel) match {
          case null => None
          case entry => entry.get(dimensionIdent) match {
            case null => None
            case x => Some(x.asInstanceOf[DematerializerFactory[RiftDimension]])
          }
        }
      case Some(toolGroup) =>
        toolregistry.get(toolGroup) match {
          case null => None
          case toolentries => toolentries.get(channel) match {
            case null => None
            case channelEntries => channelEntries.get(dimensionIdent) match {
              case null => None
              case x => Some(x.asInstanceOf[DematerializerFactory[RiftDimension]])
            }
          }
        }
    }
  }
}