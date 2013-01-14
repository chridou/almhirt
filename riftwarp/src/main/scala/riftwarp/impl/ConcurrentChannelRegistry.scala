package riftwarp.impl

import scalaz.std.option
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.components.ChannelRegistry
import riftwarp._

class ConcurrentChannelRegistry extends ChannelRegistry {
  private val channels = new _root_.java.util.concurrent.ConcurrentHashMap[String, RiftChannel](64)
  private val httpChannels = new _root_.java.util.concurrent.ConcurrentHashMap[String, RiftHttpChannel](128)

  def memoizeChannel(channel: RiftChannel) {
    (channel.channelType :: channel.moreLookUpSymbols).map(_.toLowerCase()).foreach(x => channels.put(x, channel))
    channel match {
      case http: RiftHttpChannel => 
        List(http.httpContentType, http.httpContentTypeExt).map(_.toLowerCase()).foreach(x => httpChannels.put(x, http))
      case _ => ()
    }
  }
  def getChannel(ident: String): AlmValidation[RiftChannel] =
    channels.get(ident.toLowerCase()) match {
      case null => ElementNotFoundProblem("No channel found for '%s'".format(ident)).failure
      case channel => channel.success
    }

  def lookUpFromHttpContentType(contentType: String): AlmValidation[RiftHttpChannel] =
    httpChannels.get(contentType.toLowerCase()) match {
      case null => ElementNotFoundProblem("No channel found for content type '%s'".format(contentType)).failure
      case channel => channel.success
    }
}

object ConcurrentChannelRegistry {
  def apply() = new ConcurrentChannelRegistry
}