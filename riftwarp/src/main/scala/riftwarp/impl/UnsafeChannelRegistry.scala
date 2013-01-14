package riftwarp.impl

import scalaz.std.option
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.components.ChannelRegistry
import riftwarp.RiftChannel
import riftwarp.RiftHttpChannel
import riftwarp.RiftHttpChannel

class UnsafeChannelRegistry extends ChannelRegistry {
  private val channels = collection.mutable.HashMap[String, RiftChannel]()
  private val httpChannels = collection.mutable.HashMap[String, RiftHttpChannel]()

  def memoizeChannel(channel: RiftChannel) {
    (channel.channelType :: channel.moreLookUpSymbols).map(_.toLowerCase()).foreach(x => channels += (x -> channel))
    channel match {
      case http: RiftHttpChannel => 
        List(http.httpContentType, http.httpContentTypeExt).map(_.toLowerCase()).foreach(x => httpChannels += (x -> http))
      case _ => ()
    }
  }

  def getChannel(ident: String): AlmValidation[RiftChannel] =
    option.cata(channels.get(ident.toLowerCase()))(
      found => found.success,
      ElementNotFoundProblem("No channel found for '%s'".format(ident)).failure)

  def lookUpFromHttpContentType(contentType: String): AlmValidation[RiftHttpChannel] =
    option.cata(httpChannels.get(contentType.toLowerCase))(
      found => found.success,
      ElementNotFoundProblem("No channel found for content type '%s'".format(contentType)).failure)
}

object UnsafeChannelRegistry {
  def apply() = new UnsafeChannelRegistry
}