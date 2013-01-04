package riftwarp.impl

import scalaz.std.option
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.components.ChannelRegistry
import riftwarp.RiftChannel

class ConcurrentChannelRegistry extends ChannelRegistry {
  private val channels = new _root_.java.util.concurrent.ConcurrentHashMap[String, RiftChannel](64)
  private val httpChannels = new _root_.java.util.concurrent.ConcurrentHashMap[String, RiftChannel](128)

  def memoizeChannel(channel: RiftChannel) {
    channels.put(channel.channelType.toLowerCase(), channel)
    (List(channel.httpContentType, channel.httpContentTypeExt).flatten ++ channel.moreLookUpSymbols)
      .map(x => x.toLowerCase())
      .foreach(x => httpChannels.put(x, channel))
  }
  def getChannel(ident: String): AlmValidation[RiftChannel] =
    channels.get(ident.toLowerCase()) match {
      case null => ElementNotFoundProblem("No channel found for '%s'".format(ident)).failure
      case channel => channel.success
    }

  def lookUpFromHttpContentType(contentType: String): AlmValidation[RiftChannel] =
    channels.get(contentType.toLowerCase()) match {
      case null => ElementNotFoundProblem("No channel found for content type '%s'".format(contentType)).failure
      case channel => channel.success
    }
}

object ConcurrentChannelRegistry {
  def apply() = new ConcurrentChannelRegistry
}